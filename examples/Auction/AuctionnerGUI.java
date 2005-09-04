
import jason.architecture.*;
import jason.asSemantics.ActionExec;

import javax.swing.*;

/** example of agent architecture's functions overriding */
public class AuctionnerGUI extends CentralisedAgArch {

   JTextArea jt;
   JFrame f;

   public AuctionnerGUI() {
      f = new JFrame("Auctionner agent");
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
	f.setVisible(false);
	super.stopAg();
   }
}