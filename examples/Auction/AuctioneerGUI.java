
import jason.architecture.*;
import jason.asSemantics.ActionExec;

import javax.swing.*;

/** example of agent architecture's functions overriding */
public class AuctioneerGUI extends AgArch {

   JTextArea jt;
   JFrame f;

   public AuctioneerGUI() {
      f = new JFrame("Auctioneer agent");
      jt = new JTextArea(10,30);
      f.getContentPane().add(new JScrollPane(jt));
      f.pack();
      f.setVisible(true);
   }

   public void act() {
	   ActionExec acExec = fTS.getC().getAction(); 
	   if (acExec != null) {
		   if (acExec.getActionTerm().getFunctor().startsWith("showWinner")) {
			jt.append("Winner of auction  "+acExec.getActionTerm().getTerm(0));
			jt.append(" is "+acExec.getActionTerm().getTerm(1)+"\n");
		   }
		   super.act(); // send the action to the environment to be performed.
	   }
   }

   public void stopAg() {
	f.dispose();
	super.stopAg();
   }
}
