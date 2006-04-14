// Environment code for project jasonTeamSimLocal.mas2j

import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jasonteam.Location;
import jasonteam.WorldModel;
import jasonteam.WorldView;

import java.util.logging.Level;
import java.util.logging.Logger;

public class simLocalEnv extends jason.environment.Environment {

	private Logger logger = Logger.getLogger("jasonTeamSimLocal.mas2j."+simLocalEnv.class.getName());

    WorldModel model;
    WorldView view;
    
    Term up    = Term.parse("do(up)");
    Term down  = Term.parse("do(down)");
    Term right = Term.parse("do(right)");
    Term left  = Term.parse("do(left)");
    Term skip  = Term.parse("do(skip)");
    Term pick  = Term.parse("do(pick)");
    Term drop  = Term.parse("do(drop)");
	
    static private final int UP = 0;
    static private final int DOWN = 1;
    static private final int RIGHT = 2;
    static private final int LEFT = 3;
    
    
    public simLocalEnv() {
        world2();
        view = WorldView.create(model);
        
		addPercept(Literal.parseLiteral("gsize(teste,"+model.getWidth()+","+model.getHeight()+")"));
        addPercept(Literal.parseLiteral("depot(teste,"+model.getDepot().x+","+model.getDepot().y+")"));
        
        updateAgsPercept();
	}
    
    /** no gold/no obstacle world */
    private void world1() {
        model = WorldModel.create(25,25,4);
        model.setDepot(5,7);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
    }

    /** world with gold, no obstacle */
    private void world2() {
        world1();
        model.add(WorldModel.GOLD, 20,20);
        model.add(WorldModel.GOLD, 20,15);
        //model.add(WorldModel.GOLD, 15,20);
        //model.add(WorldModel.GOLD, 1,1);
    }
   
    private void updateAgsPercept() {
        for (int i=0; i<model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }
    private void updateAgPercept(int ag) {
        updateAgPercept("miner"+(ag+1), ag);
    }
    private void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        addPercept(agName, Literal.parseLiteral("pos("+l.x+","+l.y+")"));

        // what's arount
        updateAgPercept(agName,l.x-1,l.y-1);
        updateAgPercept(agName,l.x-1,l.y);
        updateAgPercept(agName,l.x-1,l.y+1);
        updateAgPercept(agName,l.x,l.y-1);
        updateAgPercept(agName,l.x,l.y);
        updateAgPercept(agName,l.x,l.y+1);
        updateAgPercept(agName,l.x+1,l.y-1);
        updateAgPercept(agName,l.x+1,l.y);
        updateAgPercept(agName,l.x+1,l.y+1);
    }
    private void updateAgPercept(String agName, int x, int y) {
        if (x < 0 || x >= model.getWidth() || y < 0 || y >= model.getHeight()) return;
        if (model.hasObject(WorldModel.OBSTACLE,x,y)) {
            addPercept(agName, Literal.parseLiteral("cell("+x+","+y+",obstacle)"));
        } else {
            if (model.hasObject(WorldModel.EMPTY,x,y)) {
                addPercept(agName, Literal.parseLiteral("cell("+x+","+y+",empty)"));
            }
            if (model.hasObject(WorldModel.GOLD,x,y)) {
                addPercept(agName, Literal.parseLiteral("cell("+x+","+y+",gold)"));
            }
            if (model.hasObject(WorldModel.ENEMY,x,y)) {
                addPercept(agName, Literal.parseLiteral("cell("+x+","+y+",enemy)"));
            }
            if (model.hasObject(WorldModel.ALLY,x,y)) {
                addPercept(agName, Literal.parseLiteral("cell("+x+","+y+",ally)"));
            }
        }
    }
    
    
    /** Actions **/
    
    
    private boolean move(int dir, int ag) {
        Location l = model.getAgPos(ag);
        model.remove(WorldModel.ALLY, l.x, l.y);
        switch (dir) {
        case UP: 
            if (model.isFree(l.x, l.y-1)) {
                model.setAgPos(ag, l.x, l.y-1);
            }
            break;
        case DOWN:
            if (model.isFree(l.x, l.y+1)) {
                model.setAgPos(ag, l.x, l.y+1);
            }
            break;
        case RIGHT:
            if (model.isFree(l.x+1, l.y)) {
                model.setAgPos(ag, l.x+1, l.y);
            }
            break;
        case LEFT:
            if (model.isFree(l.x-1, l.y)) {
                model.setAgPos(ag, l.x-1, l.y);
            }
            break;
        }
        l = model.getAgPos(ag);
        model.add(WorldModel.ALLY, l.x, l.y);
        updateAgPercept(ag);
        view.update();
        return true;
    }

    private boolean pick(int ag) {
        Location l = model.getAgPos(ag);
        if (model.hasObject(WorldModel.GOLD,l.x,l.y)) {
            model.remove(WorldModel.GOLD,l.x,l.y);
            model.setAgCarryingGold(ag);
            updateAgPercept(ag);
            return true;
        }
        return false;
    }
    
    private boolean drop(int ag) {
        Location l = model.getAgPos(ag);
        if (model.isCarryingGold(ag)) {
            if (l.equals(model.getDepot())) {
                logger.info("Agent "+ag+" carried a gold to depot!");
            } else {
                model.add(WorldModel.GOLD,l.x,l.y);
            }
            model.setAgNotCarryingGold(ag);
            updateAgPercept(ag);
            return true;
        }
        return false;
    }

    public boolean executeAction(String ag, Term action) {
        try {
            int agId = (Integer.parseInt(ag.substring(5))) -1;
        
            if (action.equals(up))           { return move(UP, agId);
            } else if (action.equals(down))  { return move(DOWN, agId);
            } else if (action.equals(right)) { return move(RIGHT, agId);
            } else if (action.equals(left))  { return move(LEFT, agId);
            } else if (action.equals(skip))  { return true;
            } else if (action.equals(pick))  { return pick(agId);
            } else if (action.equals(drop))  { return drop(agId);
            } else {
                logger.info("executing: "+action+", but not implemented!");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing "+action+" for "+ag, e);
        }
		return false;
	}
}

