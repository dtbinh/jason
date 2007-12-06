import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

/**
 * Simple Vacuum cleaning environment
 *
 * @author Jomi
 *
 */
public class VCWorld extends Environment {

    /** world model */
    private boolean[][] dirty = { { true, true },    // all dirty
                                  { true, true } };
                                  
    private int vcx = 0; // the vacuum cleaner location
    private int vcy = 0;

	private boolean running = true;
	
    private HouseGUI gui = new HouseGUI();

    private Logger logger = Logger.getLogger("env."+VCWorld.class.getName());
    
    public VCWorld() {
        createPercept();
        gui.paint();
		
		// create a thread to add dirty
		new Thread() {
			public void run() {
				try {
					while (running) {
						// add ramdom dirty
						if (r.nextInt(100) < 20) { 
							dirty[r.nextInt(2)][r.nextInt(2)] = true;
							gui.paint();
							createPercept();
						}
						Thread.sleep(1000);
					}
				} catch (Exception e) {} 
			}
		}.start();
		
    }
        
    Random r = new Random();
    
    /** create the agents perceptions based on the world model */
    private void createPercept() {
        // remove previous perception
        clearPercepts();       
        
        // add dirty first, it has priority
        if (dirty[vcx][vcy]) {
            addPercept(Literal.parseLiteral("dirty"));
        } else {
            addPercept(Literal.parseLiteral("clean"));
        }
        if (vcx == 0 && vcy == 0) {
            addPercept(Literal.parseLiteral("pos(1)"));
        } else if (vcx == 1 && vcy == 0) {
            addPercept(Literal.parseLiteral("pos(2)"));
        } else if (vcx == 0 && vcy == 1) {
            addPercept(Literal.parseLiteral("pos(3)"));
        } else if (vcx == 1 && vcy == 1) {
            addPercept(Literal.parseLiteral("pos(4)"));
        }
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        // Change the world model based on action
        if (action.getFunctor().equals("suck")) {
            if (dirty[vcx][vcy]) {
                dirty[vcx][vcy] = false;
            }
        } else if (action.getFunctor().equals("left")) {
            if (vcx > 0) {
                vcx--;
            }
        } else if (action.getFunctor().equals("right")) {
            if (vcx < 1) {
                vcx++;
            }
        } else if (action.getFunctor().equals("up")) {
            if (vcy > 0) {
                vcy--;
            }
        } else if (action.getFunctor().equals("down")) {
            if (vcy < 1) {
                vcy++;
            }
        } else {
            logger.info("The action "+action+" is not implemented!");
            return false;
        }
        //logger.info("Agent at "+vcx+","+vcy);
        
        createPercept(); // update agents perception for the new world state      
        gui.paint();
        try { Thread.sleep(500);}  catch (Exception e) {}
        return true;
    }
    
    @Override
    public void stop() {
		running = false;
		super.stop();
		gui.setVisible(false);
	}
    
    
    /* a simple GUI */ 
	class HouseGUI extends JFrame {
        JLabel[][] labels;
        
        HouseGUI() {
			super("Domestic Robot");
            labels = new JLabel[dirty.length][dirty.length];
			getContentPane().setLayout(new GridLayout(labels.length, labels.length));
            for (int j = 0; j < labels.length; j++) {
                for (int i = 0; i < labels.length; i++) {
                    labels[i][j] = new JLabel();
                    labels[i][j].setPreferredSize(new Dimension(180,180));
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
			for (int i = 0; i < labels.length; i++) {
                for (int j = 0; j < labels.length; j++) {
                    String l = "<html><center>";
                    if (vcx == i && vcy == j) {
                        l += "<font color=\"red\" size=16><b>Robot</b><br></font>";
                    }
                    if (dirty[i][j]) {
                        l += "<font color=\"blue\" size=12>*kaka*</font>";
                    }
                    l += "</center></html>";
                    labels[i][j].setText(l);
                }
			}
		}
	}
    
}

