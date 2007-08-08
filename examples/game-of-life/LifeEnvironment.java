// Environment code for project game-of-life.mas2j

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collection;

public class LifeEnvironment extends jason.environment.Environment {

    //private Logger logger = Logger.getLogger("game-of-life.mas2j."+LifeEnvironment.class.getName());

    private LifeModel model;
    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        model = new LifeModel(Integer.parseInt(args[0]));
        model.setView(new LifeView(model));
        updateAgsPercept();
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        int ag = getAgIdBasedOnName(agName);
        if (action.getFunctor().equals("die")) {
            model.dead(ag);
        } else if (action.getFunctor().equals("live")) {
            model.alive(ag);
        }
        updateNeighbors(ag);
        return true;
    }

    int getAgIdBasedOnName(String agName) {
        return (Integer.parseInt(agName.substring(4))) - 1;
    }
    
    void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i, new ArrayList<String>());
        }
    }

    void updateNeighbors(int ag) {
        Collection<String> updated = new ArrayList<String>(9);
        
        Location l = model.getAgPos(ag);
        updateAgPercept(model.getAgId(l.x - 1, l.y - 1), updated);
        updateAgPercept(model.getAgId(l.x - 1, l.y), updated);
        updateAgPercept(model.getAgId(l.x - 1, l.y + 1), updated);
        updateAgPercept(model.getAgId(l.x, l.y - 1), updated);
        updateAgPercept(model.getAgId(l.x, l.y), updated);
        updateAgPercept(model.getAgId(l.x, l.y + 1), updated);
        updateAgPercept(model.getAgId(l.x + 1, l.y - 1), updated);
        updateAgPercept(model.getAgId(l.x + 1, l.y), updated);
        updateAgPercept(model.getAgId(l.x + 1, l.y + 1), updated);
        
        informAgsEnvironmentChanged(updated);
    }
    
    void updateAgPercept(int ag, Collection<String> updated) {
        if (ag < 0 || ag >= model.getNbOfAgs()) return;
        String name = "cell" + (ag + 1);
        updateAgPercept(name, ag);
        updated.add(name);
    }

    void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        /*
        Literal lpos = new Literal("pos");
        lpos.addTerm(new NumberTermImpl(l.x));
        lpos.addTerm(new NumberTermImpl(l.y));
        addPercept(agName, lpos);
        */

        // how many alive neighbors
        int alive = 0;
        if (model.isAlive(l.x - 1, l.y - 1)) alive++;
        if (model.isAlive(l.x - 1, l.y))     alive++;
        if (model.isAlive(l.x - 1, l.y + 1)) alive++;
        if (model.isAlive(l.x, l.y - 1))     alive++;
        if (model.isAlive(l.x, l.y + 1))     alive++;
        if (model.isAlive(l.x + 1, l.y - 1)) alive++;
        if (model.isAlive(l.x + 1, l.y))     alive++;
        if (model.isAlive(l.x + 1, l.y + 1)) alive++;
        Literal lAlive = new Literal("alive_neighbors");
        lAlive.addTerm(new NumberTermImpl(alive));
        addPercept(agName, lAlive);
    }
}

