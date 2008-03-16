package arch;

import jason.JasonException;
import jason.asSemantics.Message;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import env.ACViewer;
import env.WorldModel;
import env.WorldView;

/** 
 *  Common arch for both local and contest architectures.
 *  
 *   @author Jomi
 */
public class CowboyArch extends IdentifyCrashed {

	LocalWorldModel model = null;
	WorldView       view  = null;
	
	String     simId = null;
	int	       myId  = -1;
	boolean    gui   = false;
	boolean    playing = false;
	
	String   massimBackDir = null;
	ACViewer acView        = null;
	
	int        cycle  = 0;
	
	protected Logger logger = Logger.getLogger(CowboyArch.class.getName());

	public static Atom aOBSTACLE = new Atom("obstacle");
	public static Atom aENEMY    = new Atom("enemy");
	public static Atom aALLY     = new Atom("ally");
	public static Atom aEMPTY    = new Atom("empty");
	
	
	@Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
		super.initAg(agClass, bbPars, asSrc, stts);
	    gui = "yes".equals(stts.getUserParameter("gui"));
	    if ("yes".equals(stts.getUserParameter("write_model"))) {
        	new WriteModelThread().start();
        }
        // create the viewer for contest simulator
	    massimBackDir = stts.getUserParameter("ac_sim_back_dir");
        if (massimBackDir != null && massimBackDir.startsWith("\"")) 
            massimBackDir = massimBackDir.substring(1,massimBackDir.length()-1);
	}
	
	@Override
	public void stopAg() {
		if (view != null)   view.dispose();
		if (acView != null) acView.finish();
		super.stopAg();
	}
	
	@Override
	public boolean isCrashed() {
	    return playing && super.isCrashed();
	}
	
    void setSimId(String id) {
        simId = id;
    }

	public int getMyId() {
		if (myId < 0) {
			myId = getAgId(getAgName());
		}
		return myId;
	}

	public LocalWorldModel getModel() {
		return model;
	}
	
	public ACViewer getACViewer() {
	    return acView;
	}

    /** The perception of the grid size is removed from the percepts list 
        and "directly" added as a belief */
    void gsizePerceived(int w, int h, String opponent) {
        model = new LocalWorldModel(w, h);
        getTS().getAg().addBel(Literal.parseLiteral("gsize("+w+","+h+")"));
        playing = true;

        // manage GUIs
        if (view != null)   view.dispose();
        if (acView != null) acView.finish();
        if (gui) { 
            view = new WorldView("Herding (view of cowboy "+(getMyId()+1)+") -- against "+opponent,model);
        }
        if (massimBackDir != null) { 
            acView = new ACViewer(massimBackDir, w, h);
            acView.setPriority(Thread.MIN_PRIORITY);
            acView.start();
        }
    }
    
    /** The perception of the corral location is removed from the percepts list 
        and "directly" added as a belief */
    void corralPerceived(Location upperLeft, Location downRight) {
        model.setCorral(upperLeft, downRight);
        if (acView != null) acView.getModel().setCorral(upperLeft, downRight);
        getTS().getAg().addBel(Literal.parseLiteral("corral("+upperLeft.x+","+upperLeft.y+","+downRight.x+","+downRight.y+")"));
    }

    /** The number of steps of the simulation is removed from the percepts list 
        and "directly" added as a belief */
    void stepsPerceived(int s) {
    	getTS().getAg().addBel(Literal.parseLiteral("steps("+s+")"));
        model.setMaxSteps(s);
    }

    /** The perception ratio is discovered */
	void perceptionRatioPerceived(int s) {
		if (s != model.getPerceptionRatio()) {
			model.setPerceptionRatio(s);
			getTS().getAg().addBel(Literal.parseLiteral("pratio("+s+")"));
		}
	}
    
	/** update the model with obstacle and share them with the team mates */
	void obstaclePerceived(int x, int y, Literal p) {
		if (! model.hasObject(WorldModel.OBSTACLE, x, y)) {
			model.add(WorldModel.OBSTACLE, x, y);
			if (acView != null) acView.addObject(WorldModel.OBSTACLE, x, y);
			Message m = new Message("tell", null, null, p);
			try {
				broadcast(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}

    Location lo1 = new Location(-1,-1), // last locations of the agent 
             lo2 = new Location(-1,-1), 
             lo3 = new Location(-1,-1), 
             lo4 = new Location(-1,-1),
             lo5 = new Location(-1,-1),
             lo6 = new Location(-1,-1);

	
	/** update the model with the agent location and share this information with team mates */
	void locationPerceived(int x, int y) {
		Location oldLoc = model.getAgPos(getMyId());
        if (oldLoc != null) {
            model.clearAgView(oldLoc); // clear golds and  enemies
        }
		if (oldLoc == null || !oldLoc.equals(new Location(x,y))) {
			try {
				model.setAgPos(getMyId(), x, y);
				if (acView != null) acView.getModel().setAgPos(getMyId(), x, y);
				model.incVisited(x, y);
			
				Message m = new Message("tell", null, null, "my_status("+x+","+y+")");
				broadcast(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
        lo6 = lo5;
        lo5 = lo4;
        lo4 = lo3;
        lo3 = lo2;
        lo2 = lo1;
        lo1 = new Location(x,y);

        if (isRobotFrozen()) {
        	try {
	        	logger.info("** Arch adding restart for "+getAgName());
        	    getTS().getC().create();
        		
	        	getTS().getAg().getBB().abolish(new Atom("restart").getPredicateIndicator());
	        	getTS().getAg().addBel(new Atom("restart"));
	        	lo2 = new Location(-1,-1); // to not restart again in the next cycle
	     
	        	//getTS().stopCycle();
        	} catch (Exception e) {
            	logger.info("Error in restart!"+ e);
        	}
        }
	}
	
	/** returns true if the agent do not move in the last 5 location perception */
	public boolean isRobotFrozen() {
		return lo1.equals(lo2) && lo2.equals(lo3) && lo3.equals(lo4) && lo4.equals(lo5) && lo5.equals(lo6);
	}
	
    public static Literal createCellPerception(int x, int y, Term obj) {
        Literal l = new Literal("cell");
        l.addTerm(new NumberTermImpl(x));
        l.addTerm(new NumberTermImpl(y));
        l.addTerm(obj); 
        return l;
    }

	
    void cowPerceived(int x, int y) {
    	model.add(WorldModel.COW, x, y);
    }

    // not used, the allies send messages with their location    
    //void allyPerceived(int x, int y) {
    //    model.add(WorldModel.AGENT, x, y);
    //}
    
    void enemyPerceived(int x, int y) {
        model.add(WorldModel.ENEMY, x, y); 
    }

    void simulationEndPerceived(String result) {
    	getTS().getAg().addBel(Literal.parseLiteral("end_of_simulation("+result+")"));
        playing = false;
    }
	
    void setCycle(int s) {
    	cycle = s;
		if (view != null) view.setCycle(cycle);
        //if (writeModelT != null) writeModelT.writeModel();
    }
    
    void setScore(int s) {
        model.setCowsBlue(s);
    }
    
    /** change broadcast to send messages to only my team mates */
    @Override
    public void broadcast(Message m) throws Exception {
    	String basename = getAgName().substring(0,getAgName().length()-1);
    	for (int i=1; i <= model.getAgsByTeam() ; i++) {
    	    String oname = basename+i;
    		if (!getAgName().equals(oname)) {
    			Message msg = new Message(m);
    			msg.setReceiver(oname);
    			try {
    			    sendMsg(msg);
    			} catch (JasonException e) {} // no problem, the agent still does not exists
    		}
    	}
    }
    
	@Override
    public void checkMail() {
	    try {
    		super.checkMail();
    
    		// remove messages related to obstacles and agent_position
    		// and update the model
    		Iterator<Message> im = getTS().getC().getMailBox().iterator();
    		while (im.hasNext()) {
    			Message m  = im.next();
    			String  ms = m.getPropCont().toString();
    			if (ms.startsWith("cell") && ms.endsWith("obstacle)") && model != null) {
    				Literal p = (Literal)m.getPropCont();
    				int x = (int)((NumberTerm)p.getTerm(0)).solve();
    				int y = (int)((NumberTerm)p.getTerm(1)).solve();
    				if (model.inGrid(x,y)) {
    					model.add(WorldModel.OBSTACLE, x, y);
    					if (acView != null) acView.addObject(WorldModel.OBSTACLE, x, y);
    				}
    				im.remove();
    				//getTS().getAg().getLogger().info("received obs="+p);
    				
    			} else if (ms.startsWith("my_status") && model != null) {
    			    // TODO: add perception cell(X,Y,ally(Name))
    			    
    				// update others location
    				Literal p = Literal.parseLiteral(m.getPropCont().toString());
    				int x = (int)((NumberTerm)p.getTerm(0)).solve();
    				int y = (int)((NumberTerm)p.getTerm(1)).solve();
    				if (model.inGrid(x,y)) {
    					try {
    						int agid = getAgId(m.getSender());
    						model.setAgPos(agid, x, y);
    						if (acView != null) acView.getModel().setAgPos(agid, x, y);
    						model.incVisited(x, y);
    						//getTS().getAg().getLogger().info("ag pos "+getMinerId(m.getSender())+" = "+x+","+y);
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    				}
    				im.remove(); 
    			}
    		}
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, "Error checking email!",e);
	    }
    }
	
    public static int getAgId(String agName) {
		return (Integer.parseInt(agName.substring(agName.length()-1))) - 1;    	
    }

	
	
	class WriteModelThread extends Thread {
		public void run() {
			String fileName = "world-state-"+getAgName()+".txt";
			try {
				PrintWriter out = new PrintWriter(fileName);
				while (isRunning()) {
					waitSomeTime();
					if (model != null && playing) {
						out.println("\n\n** Agent "+getAgName()+" in cycle "+cycle+"\n");
						//for (int i=0; i<model.getNbOfAgs(); i++) {
							// TODO: out.println("miner"+(i+1)+" is carrying "+model.getGoldsWithAg(i)+" gold(s), at "+model.getAgPos(i));
						//}
						out.println(model.toString());
						out.flush();
					}
				}
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		synchronized private void waitSomeTime() throws InterruptedException {
			wait(2000);
		}
	}
}
