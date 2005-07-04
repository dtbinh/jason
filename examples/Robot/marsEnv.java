
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.environment.Environment;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

public class marsEnv extends Environment {

	public static final int NGarb = 3;
	public static final int GSize = 7;
	public static final int Min = 0;
	public static final int Mid = 4;
	public static final int X = 0; 
	public static final int Y = 1; 
	public static final int MErr = 2;

	public static final Term ns = Term.parse("next(slot)");
	public static final Term pg = Term.parse("pick(garb)");
	public static final Term dg = Term.parse("drop(garb)");
	public static final Term bg = Term.parse("burn(garb)");

	public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
	public static final Literal g2 = Literal.parseLiteral("garbage(r2)");

	int nerr;

	Random random = new Random(System.currentTimeMillis());

	// garbage places
	boolean[][] mars = new boolean[GSize][GSize];

	int x, y;

	int[] cr1 = new int[2]; // pos of Robot1
	int[] cr2 = new int[2]; // pos of Tobot2

	Literal pos1 = null;

	MarsGUI gui;
	
	public marsEnv() {
		cr1[X] = Min;
		cr1[Y] = Min;
		cr2[X] = Mid;
		cr2[Y] = Mid;
		for (int i = 0; i < GSize; i++)
			for (int j = 0; j < GSize; j++)
				mars[i][j] = false;

		// for less non-determinism, comment out from here...
		for (int i = 0; i < NGarb; i++) {
			x = random.nextInt(GSize);
			y = random.nextInt(GSize);
			if (x == Mid && y == Mid) {
				switch (random.nextInt(4)) {
				case 0:
					x++;
					break;
				case 1:
					x--;
					break;
				case 2:
					y++;
					break;
				case 3:
					y--;
					break;
				}
			}
			mars[x][y] = true;
		}
		// ... until here and uncomment these 2 lines
		//mars[1][1] = true;
		//mars[3][3] = true;

		pos1 = Literal.parseLiteral("pos(r1," + cr1[X] + "," + cr1[Y] + ")");

		// Add initial percepts below, for example:
		addPercept(pos1);
		if (mars[cr1[X]][cr1[Y]])
			addPercept(g1);
		if (mars[cr2[X]][cr2[Y]])
			addPercept(g2);
		
		gui = new MarsGUI();
	}

	public boolean executeAction(String ag, Term action) {
		if (action.equals(ns)) {
			cr1[X]++;
			if (cr1[X] == GSize) {
				cr1[X] = Min;
				cr1[Y]++;
			}
			// finished searching the whole grid
			if (cr1[Y] == GSize)
				return true;
			if (cr1[X] == Mid && cr1[Y] == Mid)
				cr1[X]++;
			
		} else if (action.getFunctor().equals("moveTowards")) {
			x = (new Integer(action.getTerm(0).toString())).intValue();
			y = (new Integer(action.getTerm(1).toString())).intValue();
			if (cr1[X] < x)
				cr1[X]++;
			else if (cr1[X] > x)
				cr1[X]--;
			if (cr1[Y] < y)
				cr1[Y]++;
			else if (cr1[Y] > y)
				cr1[Y]--;
			
		} else if (action.equals(pg)) {
			// sometimes the "picking" action doesn't work
			// but never more than MErr times
			if (random.nextBoolean() || nerr == MErr) {
				mars[cr1[X]][cr1[Y]] = false;
				nerr = 0;
			} else {
				nerr++;
			}
		} else if (action.equals(dg)) {
			mars[cr1[X]][cr1[Y]] = true;
		} else if (action.equals(bg)) {
			mars[cr2[X]][cr2[Y]] = false;
		}
		removePercept(pos1);
		removePercept(g1);
		removePercept(g2);
		pos1 = Literal.parseLiteral("pos(r1," + cr1[X] + "," + cr1[Y] + ")");
		addPercept(pos1);
		if (cr1[X] < GSize && cr1[Y] < GSize)
			if (mars[cr1[X]][cr1[Y]])
				addPercept(g1);
		if (cr2[X] < GSize && cr2[Y] < GSize)
			if (mars[cr2[X]][cr2[Y]])
				addPercept(g2);

		informAgsEnvironmentChanged(); // notify all agents that the env. has changed

		gui.paint();
		return true;
	}
	
	
	class MarsGUI extends JFrame {
		JLabel[][] labels = new JLabel[GSize][GSize];
		MarsGUI() {
			super("Mars robots");
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
					if (mars[i][j]) {
						labels[i][j].setText("G");
					} else {
						labels[i][j].setText("");						
					}
				}
			}
			labels[cr1[X]][cr1[Y]].setText(labels[cr1[X]][cr1[Y]].getText()+"-R1-");
			labels[cr2[X]][cr2[Y]].setText(labels[cr2[X]][cr2[Y]].getText()+"-R2-");
			
			try {
				Thread.sleep(500);
			} catch (Exception e) {}
		}
	}
}