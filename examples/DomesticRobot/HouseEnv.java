import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;

import java.util.logging.Logger;

public class HouseEnv extends Environment {

    // common literals
	public static final Literal of  = Literal.parseLiteral("open(fridge)");
    public static final Literal clf = Literal.parseLiteral("close(fridge)");
	public static final Literal gb  = Literal.parseLiteral("get(beer)");
	public static final Literal hb  = Literal.parseLiteral("hand_in(beer)");
	public static final Literal sb  = Literal.parseLiteral("sip(beer)");
	public static final Literal hob = Literal.parseLiteral("has(owner,beer)");

	public static final Literal af = Literal.parseLiteral("at(robot,fridge)");
	public static final Literal ao = Literal.parseLiteral("at(robot,owner)");
	
	static Logger logger = Logger.getLogger(HouseEnv.class.getName());

    // model and view of the grid
    HouseModel model;
    HouseView  view;
    
    @Override
    public void init(String[] args) {
	    model = new HouseModel();
        
        if (args.length == 1 && args[0].equals("gui")) { 
            view  = new HouseView(model);
            model.setView(view);
        }
        
        updatePercepts();
    }
	
    /** creates the agents perceptions based on the HouseModel */
    void updatePercepts() {
        // clear the perceptps of the agents
        clearPercepts("robot");
        clearPercepts("owner");
        
        Location lRobot = model.getAgPos(0);
        // add their locations
        if(lRobot.equals(model.lFridge)) {
            addPercept("robot", af);
        }
        if(lRobot.equals(model.lOwner)) {
            addPercept("robot", ao);
        }
        
        // add beer "status"
        if (model.fridgeOpen) {
            addPercept("robot", Literal.parseLiteral("stock(beer,"+model.avBeer+")"));
        }
        if (model.beer > 0) {
            addPercept("robot", hob);
            addPercept("owner", hob);
        }
    }
    

    @Override
	public boolean executeAction(String ag, Structure action) {
		logger.fine("Agent "+ag+" doing "+action+" in the environment");
		if (action.equals(of)) {
            model.openFridge();
            
        } else if (action.equals(clf)) {
            model.closeFridge();
            
		} else if (action.getFunctor().equals("move_towards")) {
			String l = action.getTerm(0).toString();
            Location dest = null;
			if (l.equals("fridge")) {
				dest = model.lFridge;
			} else if (l.equals("owner")) {
                dest = model.lOwner;
			}
			model.moveTowards(dest);
			
		} else if (action.equals(gb)) {
            model.getBeer();
            
		} else if (action.equals(hb)) {
            model.handInBeer();
            
		} else if (action.equals(sb)) {
            model.sipBeer();
            
		} else if (action.getFunctor().equals("deliver")) {
			model.addBeer(Integer.parseInt(action.getTerm(1).toString()));
            
		} else {
		    logger.info("Failed to execute action "+action);
            return false;
        }

		updatePercepts();
        try {
            Thread.sleep(150);
        } catch (Exception e) {}
		return true;
	}

}
