import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.logging.Logger;

public class houseEnv extends Environment {

    // common literals
	public static final Literal of  = Literal.parseLiteral("open(fridge)");
    public static final Literal clf = Literal.parseLiteral("close(fridge)");
	public static final Literal gb  = Literal.parseLiteral("get(beer)");
	public static final Literal hb  = Literal.parseLiteral("hand_in(beer)");
	public static final Literal sb  = Literal.parseLiteral("sip(beer)");
	public static final Literal hob = Literal.parseLiteral("has(owner,beer)");

	public static final Literal af = Literal.parseLiteral("at(robot,fridge)");
	public static final Literal ao = Literal.parseLiteral("at(robot,owner)");
	
	static Logger logger = Logger.getLogger(houseEnv.class.getName());

    HouseModel model;
    HouseView  view;
    
	public houseEnv() {
	    model = new HouseModel();
        view  = new HouseView(model);
        model.setView(view);
        
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
    

	public boolean executeAction(String ag, Term action) {
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

    
    /** class that implements the Model of Domestic Robot application */
    class HouseModel extends GridWorldModel {
        
        public static final int GSize = 7;
        public static final int NSips = 10;

        public static final int FRIDGE = 16;
        public static final int OWNER  = 32;

        boolean fridgeOpen   = false; // whether the fridge is open
        boolean carryingBeer = false; // whether the robot is carrying berr
        int beer   = 0; // how many sip the owner did
        int avBeer = 2; // how many beers are available

        Location lFridge = new Location(0,0);
        Location lOwner  = new Location(GSize-1,GSize-1);

        private HouseModel() {
            super(GSize, GSize, 1); // only one agent moves

            // initial location of robot
            setAgPos(0, GSize/2, GSize/2);
            
            // initial location of fridge and owner
            add(FRIDGE, lFridge);
            add(OWNER, lOwner);
        }

        void openFridge() {
            if (!fridgeOpen) {
                fridgeOpen = true;                 
            }
        }

        void closeFridge() {
            if (fridgeOpen) {
                fridgeOpen = false;                 
            }
        }  

        void moveTowards(Location dest) {
            Location r1 = getAgPos(0);
            if (r1.x < dest.x)
                r1.x++;
            else if (r1.x > dest.x)
                r1.x--;
            if (r1.y < dest.y)
                r1.y++;
            else if (r1.y > dest.y)
                r1.y--;
            setAgPos(0, r1);
        }
        
        void getBeer() {
            if (fridgeOpen && avBeer > 0 && !carryingBeer) {
                avBeer--;
                carryingBeer = true;
            }
        }
        
        void handInBeer() {
            beer = NSips;
            carryingBeer = false;
        }
        
        void sipBeer() {
            beer--;
        }
     
        void addBeer(int n) {
            avBeer = avBeer + n;
        }
    }
    
    /** class that implements the View of Domestic Robot application */
    class HouseView extends GridWorldView {

        public HouseView(HouseModel model) {
            super(model, "Domestic Robot", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            g.setColor(Color.black);
            switch (object) {
                case HouseModel.FRIDGE: drawString(g, x, y, defaultFont, "Fridge");  break;
                case HouseModel.OWNER: drawString(g, x, y, defaultFont, "Owner");  break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            super.drawAgent(g, x, y, Color.yellow, -1);
            g.setColor(Color.black);
            super.drawString(g, x, y, defaultFont, "Robot");
        }
    }
}
