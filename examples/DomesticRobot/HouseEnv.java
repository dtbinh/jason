import jason.asSyntax.*;
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

    HouseModel model; // the model of the grid
    
    @Override
    public void init(String[] args) {
        model = new HouseModel();
        
        if (args.length == 1 && args[0].equals("gui")) { 
            HouseView view  = new HouseView(model);
            model.setView(view);
        }
        
        updatePercepts();
    }
	
    /** creates the agents perceptions based on the HouseModel */
    void updatePercepts() {
        // clear the perceptps of the agents
        clearPercepts("robot");
        clearPercepts("owner");
        
        // get the robot location
        Location lRobot = model.getAgPos(0);

        // add agents' locations in perceptions
        if (lRobot.equals(model.lFridge)) {
            addPercept("robot", af);
        }
        if (lRobot.equals(model.lOwner)) {
            addPercept("robot", ao);
        }
        
        // add beer "status" in perception
        if (model.fridgeOpen) {
            addPercept("robot", Literal.parseLiteral("stock(beer,"+model.availableBeers+")"));
        }
        if (model.sipCount > 0) {
            addPercept("robot", hob);
            addPercept("owner", hob);
        }
    }
    

    @Override
	public boolean executeAction(String ag, Structure action) {
		logger.fine("Agent "+ag+" doing "+action+" in the environment");
        boolean result = false;
		if (action.equals(of)) { // of = open(fridge)
            result = model.openFridge();
            
        } else if (action.equals(clf)) { // clf = close(fridge)
            result = model.closeFridge();
            
		} else if (action.getFunctor().equals("move_towards")) {
			String l = action.getTerm(0).toString();
            Location dest = null;
			if (l.equals("fridge")) {
				dest = model.lFridge;
			} else if (l.equals("owner")) {
                dest = model.lOwner;
			}

			try {
				result = model.moveTowards(dest);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (action.equals(gb)) {
            result = model.getBeer();
            
		} else if (action.equals(hb)) {
            result = model.handInBeer();
            
		} else if (action.equals(sb)) {
            result = model.sipBeer();
            
		} else if (action.getFunctor().equals("deliver")) {
			result = model.addBeer(Integer.parseInt(action.getTerm(1).toString()));
            
		} else {
		    logger.info("Failed to execute action "+action);
        }

        if (result) {
            updatePercepts();
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }
		return result;
	}
}
