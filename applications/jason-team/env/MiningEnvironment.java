package env;

import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.SteppedEnvironment;
import jason.environment.grid.Location;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of the local simulator for the competition scenario.
 * 
 * @author Jomi
 */
public class MiningEnvironment extends SteppedEnvironment {

    private Logger          logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + MiningEnvironment.class.getName());

    WorldModel              model;
    WorldView	            view;
    
    int                     simId    = 5; // type of environment
    int                     nbWorlds = 10;
    
    Random                  random   = new Random();

    Term                    up       = DefaultTerm.parse("do(up)");
    Term                    down     = DefaultTerm.parse("do(down)");
    Term                    right    = DefaultTerm.parse("do(right)");
    Term                    left     = DefaultTerm.parse("do(left)");
    Term                    skip     = DefaultTerm.parse("do(skip)");
    Term                    pick     = DefaultTerm.parse("do(pick)");
    Term                    drop     = DefaultTerm.parse("do(drop)");

    boolean                 hasGUI   = true;
    int                     windowSize = 800;
    
    int                     finishedAt = 0; // cycle where all golds was collected

    String                  redTeamName, blueTeamName;
    
    @Override
	public void init(String[] args) {
        setOverActionsPolicy(OverActionsPolicy.queue);

        // get the parameters
        setSleep(Integer.parseInt(args[1]));
        hasGUI = args[2].equals("yes"); 
        redTeamName  = args[3];
        blueTeamName = args[4];
        if (args.length > 5)
            windowSize = Integer.parseInt(args[5]);
        initWorld(Integer.parseInt(args[0]));
    }

    @Override
    protected void updateNumberOfAgents() {
        setNbAgs(model.getNbOfAgs());
    }
    
    @Override
	public void stop() {
		super.stop();
	}

    public int getSimId() {
        return simId;
    }
    
    
    @Override
    public boolean executeAction(String ag, Structure action) {
    	
        boolean result = false;
        int agId = -10;
        try {
            // get the agent id based on its name
            agId = getAgNbFromName(ag);
            
            // check failure
            if (!action.equals(drop) && random.nextDouble() < model.getAgFatigue(agId)) {
            	//logger.info("Action "+action+" from agent "+ag+" failed!");
                return true; // does nothing
            }
            
            if (action.equals(up)) {
                result = model.move(WorldModel.Move.UP, agId);
            } else if (action.equals(down)) {
                result = model.move(WorldModel.Move.DOWN, agId);
            } else if (action.equals(right)) {
                result = model.move(WorldModel.Move.RIGHT, agId);
            } else if (action.equals(left)) {
                result = model.move(WorldModel.Move.LEFT, agId);
            } else if (action.equals(skip)) {
            	logger.info("agent "+ag+" skips!");
                result = true;
            } else if (action.equals(pick)) {
                result = model.pick(agId);
            } else if (action.equals(drop)) {
                result = model.drop(agId);
            } else {
                logger.warning("executing: " + action + ", but not implemented!");
            }
            if (result) {
                if (model.isAllGoldsCollected() && finishedAt == 0 && model.getInitialNbGolds() > 0) {
                	finishedAt = getStep();
                	logger.info("** All golds collected in "+finishedAt+" cycles! Result (red x blue) = "+model.getGoldsInDepotRed() + " x "+model.getGoldsInDepotBlue());
					new Thread() {
						public void run() {
							try {
								getEnvironmentInfraTier().getRuntimeServices().stopMAS();
							} catch (Exception e) {}
						}
					}.start();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + ag + " (ag code:"+agId+")", e);
        }
        return true;
    }

    public int getAgNbFromName(String agName) {
    	if (agName.startsWith(redTeamName)) {
    		return (Integer.parseInt(agName.substring(redTeamName.length()))) - 1; 
    	}
    	if (agName.startsWith(blueTeamName)) {
    		return (Integer.parseInt(agName.substring(blueTeamName.length()))) + (model.agsByTeam - 1); 
    	}
    	logger.warning("There is no ID for agent named "+agName);
		return -1;    	
    }
    
    public String getAgNameFromID(int id) {
    	if (id < model.agsByTeam)
            return redTeamName + (id+1);
    	else 
            return blueTeamName + (id-(model.agsByTeam-1));
    }

    public void initWorld(int w) {
        simId = w;
    	try {
	        switch (w) {
	        case 1:  model = WorldFactory.world1(); break;
	        case 2:  model = WorldFactory.world2(); break;
	        case 3:  model = WorldFactory.world3(); break;
	        case 4:  model = WorldFactory.world4(); break;
	        case 5:  model = WorldFactory.world5(); break;
	        case 6:  model = WorldFactory.world6(); break;
	        case 7:  model = WorldFactory.world7(); break;
	        case 8:  model = WorldFactory.world8(); break;
	        case 9:  model = WorldFactory.world9(); break;
	        case 10: model = WorldFactory.world10(); break;
	        default:
	            logger.warning("Invalid index!");
	            return;
	        }
            
	        super.init(new String[] { "1000" } ); // set step timeout
	        updateNumberOfAgents();
            
            // add perception for all agents
            clearPercepts();
	        addPercept(Literal.parseLiteral("gsize(" + simId + "," + model.getWidth() + "," + model.getHeight() + ")"));
	        addPercept(Literal.parseLiteral("depot(" + simId + "," + model.getDepot().x + "," + model.getDepot().y + ")"));
			int msteps = model.getMaxSteps();
			if (msteps == 0) msteps = 100000;
	        addPercept(Literal.parseLiteral("steps(" + simId + "," + msteps + ")"));
            
	        updateAgsPercept();
            //informAgsEnvironmentChanged();
            
            if (hasGUI) {
                view = new WorldView(model, windowSize);
                view.setEnv(this);
            }
    	} catch (Exception e) {
    		logger.warning("Error creating world "+e);
    	}
    }
    public static Literal aCAP   = new Literal("container_has_space");

    public static Atom aOBSTACLE = new Atom("obstacle");
    public static Atom aGOLD     = new Atom("gold");
    public static Atom aENEMY    = new Atom("enemy");
    public static Atom aALLY     = new Atom("ally");

    @Override
    public void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    private void updateAgPercept(int ag) {
        updateAgPercept(getAgNameFromID(ag), ag);
    }

    private void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        Literal p = new Literal("pos");
        p.addTerm(new NumberTermImpl(l.x));
        p.addTerm(new NumberTermImpl(l.y));
        p.addTerm(new NumberTermImpl(getStep()));
        addPercept(agName, p);
        
        Literal cg = new Literal("carrying_gold");
        cg.addTerm(new NumberTermImpl(model.getGoldsWithAg(ag)));
        addPercept(agName, cg);
        
        if (model.mayCarryMoreGold(ag)) {
            addPercept(agName, aCAP);
        }

        // what's around
        updateAgPercept(agName, ag, l.x - 1, l.y - 1);
        updateAgPercept(agName, ag, l.x - 1, l.y);
        updateAgPercept(agName, ag, l.x - 1, l.y + 1);
        updateAgPercept(agName, ag, l.x, l.y - 1);
        updateAgPercept(agName, ag, l.x, l.y);
        updateAgPercept(agName, ag, l.x, l.y + 1);
        updateAgPercept(agName, ag, l.x + 1, l.y - 1);
        updateAgPercept(agName, ag, l.x + 1, l.y);
        updateAgPercept(agName, ag, l.x + 1, l.y + 1);
    }

        
    private void updateAgPercept(String agName, int agId, int x, int y) {
        if (random.nextDouble() < model.getAgFatigue(agId)) return; // perception omission
        if (model == null || !model.inGrid(x,y)) return;
        if (model.hasObject(WorldModel.OBSTACLE, x, y)) {
            addPercept(agName, createCellPerception(x, y, aOBSTACLE));
        } else {
            if (model.hasObject(WorldModel.GOLD, x, y)) {
                addPercept(agName, createCellPerception(x, y, aGOLD));
            }
            int otherag = model.getAgAtPos(x, y);
            if (otherag >= 0 && otherag != agId) {
                boolean agIsRed    = agId < model.agsByTeam; 
                boolean otherIsRed = otherag < model.agsByTeam; 
                if (agIsRed == otherIsRed) // ally
                    addPercept(agName, createCellPerception(x, y, aALLY));
                else
                    addPercept(agName, createCellPerception(x, y, aENEMY));
            }
        }
    }
    
    public static Literal createCellPerception(int x, int y, Atom obj) {
    	Literal l = new Literal("cell");
    	l.addTerm(new NumberTermImpl(x));
    	l.addTerm(new NumberTermImpl(y));
    	l.addTerm(obj); 
    	return l;
    }

    

    Integer nextWorld;
    
    public void startNewWorld(int w) {
        addPercept(Literal.parseLiteral("end_of_simulation(" + simId + ",0)"));
        if (view != null) view.setVisible(false);
        nextWorld = new Integer(w);
    }

    // some customisation for cycled environment

    @Override
    protected void stepStarted(int step) {
        if (view != null) view.setCycle(getStep());
    }

    private long sum = 0;

    @Override
    protected void stepFinished(int step, long time, boolean timeout) {
        if (step == 0) {
            sum = 0;
            return;
        }
        if (step >= model.getMaxSteps() && model.getMaxSteps() > 0) {
            logger.info("** Finished at the maximal number of steps! Red x Blue = "+model.getGoldsInDepotRed() + " x "+model.getGoldsInDepotBlue());
            try {
                getEnvironmentInfraTier().getRuntimeServices().stopMAS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        sum += time;
        logger.info("Cycle "+step+" finished in "+time+" ms, mean is "+(sum/step)+".");
        
        if (nextWorld != null) {
            initWorld(nextWorld.intValue());
            nextWorld = null;
        }
    }

}
