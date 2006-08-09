import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.environment.Environment;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

public class houseEnv extends Environment {

	public static final int GSize = 7;
	public static final int Min = 0;
	public static final int Mid = 3;
	public static final int X = 0; 
	public static final int Y = 1;
	public static final int NSips = 10;

	public static final Literal of  = Literal.parseLiteral("open(fridge)");
    public static final Literal clf = Literal.parseLiteral("close(fridge)");
	public static final Literal gb  = Literal.parseLiteral("get(beer)");
	public static final Literal hb  = Literal.parseLiteral("hand_in(beer)");
	public static final Literal sb  = Literal.parseLiteral("sip(beer)");
	public static final Literal hob = Literal.parseLiteral("has(owner,beer)");

	public static final Literal af = Literal.parseLiteral("at(robot,fridge)");
	public static final Literal ao = Literal.parseLiteral("at(robot,owner)");
	
	int beer   = 0;
	int avBeer = 2;
	boolean fridgeOpen   = false;
	boolean carryingBeer = false;
	
	static Logger logger = Logger.getLogger(houseEnv.class.getName());

	// garbage places
	boolean[][] house = new boolean[GSize][GSize];

	int x, y;

	int[] cr = new int[2]; // pos of Robot
	int[] co = new int[2]; // pos of Owner
	int[] cf = new int[2]; // pos of Fridge

	Literal pos1 = null;

	houseGUI gui;
	
	public houseEnv() {
		cf[X] = Min;
		cf[Y] = Min;
		cr[X] = Mid;
		cr[Y] = Mid;
		co[X] = GSize-1;
		co[Y] = GSize-1;

		// Add initial percepts
		updatePercepts();
		
		gui = new houseGUI();
	}
	
	public void stop() {
		super.stop();
		gui.dispose();
	}

	public boolean executeAction(String ag, Term action) {
		logger.fine("Agent "+ag+" doing "+action+" in the environment");
		if (action.equals(of) && !fridgeOpen) {
			fridgeOpen = true;
        } else if (action.equals(clf) && fridgeOpen) {
            fridgeOpen = false;
            
		} else if (action.getFunctor().equals("move_towards")) {
			String l = action.getTerm(0).toString();
			if (l.equals("fridge")) {
				x = cf[X];
				y = cf[Y];
			} else if (l.equals("owner")) {
				x = co[X];
				y = co[Y];
			}

			if (cr[X] < x)
				cr[X]++;
			else if (cr[X] > x)
				cr[X]--;
            
			if (cr[Y] < y)
				cr[Y]++;
			else if (cr[Y] > y)
				cr[Y]--;
			
		} else if (action.equals(gb) && fridgeOpen && avBeer > 0 && !carryingBeer) {
			avBeer--;
			carryingBeer = true;
            
		} else if (action.equals(hb)) {
			beer = NSips;
			carryingBeer = false;
            
		} else if (action.equals(sb)) {
			beer--;
		} else if (action.getFunctor().equals("deliver")) {
			avBeer = avBeer + Integer.parseInt(action.getTerm(1).toString());
		} else {
		    logger.info("Failed to execute action "+action);
            return false;
        }

		updatePercepts();
		gui.paint();
		return true;
	}

	void updatePercepts() {
        // clear the perceptps of the agents
		clearPercepts("robot");
		clearPercepts("owner");
        
        // add their locations
		if(cr[X]==cf[X] && cr[Y]==cf[Y])
			addPercept("robot",af);
		if(cr[X]==co[X] && cr[Y]==co[Y])
			addPercept("robot",ao);
        
        // add beer "status"
		if (fridgeOpen) {
			addPercept("robot",Literal.parseLiteral("stock(beer,"+avBeer+")"));
		}
		if (beer > 0) {
			addPercept("robot",hob);
			addPercept("owner",hob);
		}
	}
	
    /** a very simple GUI */
	class houseGUI extends JFrame {
		JLabel[][] labels = new JLabel[GSize][GSize];
		houseGUI() {
			super("Domestic Robot");
			getContentPane().setLayout(new GridLayout(GSize, GSize));
			for (int i = 0; i < GSize; i++) {
				for (int j = 0; j < GSize; j++) {
					labels[i][j] = new JLabel();
					labels[i][j].setPreferredSize(new Dimension(60,60));
					labels[i][j].setHorizontalAlignment(JLabel.CENTER);
					labels[i][j].setBorder(new EtchedBorder());
					getContentPane().add(labels[i][j]);
				}
			}
			pack();
			setVisible(true);
			paint();
		}
		
		void paint() {
			for (int i = 0; i < GSize; i++) {
				for (int j = 0; j < GSize; j++) {
					labels[i][j].setText("");
				}
			}

            labels[cf[X]][cf[Y]].setText(labels[cf[X]][cf[Y]].getText()+"F");
			if (beer>0) {
				labels[co[X]][co[Y]].setText(labels[co[X]][co[Y]].getText()+"O-B");
			} else {
				labels[co[X]][co[Y]].setText(labels[co[X]][co[Y]].getText()+"O");
			}
            
			if (carryingBeer) {
				labels[cr[X]][cr[Y]].setText(labels[cr[X]][cr[Y]].getText()+"-R-B");
			} else {
				labels[cr[X]][cr[Y]].setText(labels[cr[X]][cr[Y]].getText()+"-R-");
			}
			
			try {
				Thread.sleep(150);
			} catch (Exception e) {}
		}
	}
}
