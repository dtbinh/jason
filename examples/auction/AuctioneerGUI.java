import java.util.List;

import jason.architecture.*;
import jason.asSemantics.ActionExec;

import javax.swing.*;

/** example of agent architecture's functions overriding */
public class AuctioneerGUI extends AgArch {

    JTextArea jt;

    JFrame    f;

    public AuctioneerGUI() {
        f = new JFrame("Auctioneer agent");
        jt = new JTextArea(10, 30);
        f.getContentPane().add(new JScrollPane(jt));
        f.pack();
        f.setVisible(true);
    }

    public void act(ActionExec action, List<ActionExec> feedback) {
        if (action.getActionTerm().getFunctor().startsWith("show_winner")) {
            jt.append("Winner of auction  " + action.getActionTerm().getTerm(0));
            jt.append(" is " + action.getActionTerm().getTerm(1) + "\n");
        }
        super.act(action,feedback); // send the action to the environment to be performed.
    }

    public void stopAg() {
        f.dispose();
        super.stopAg();
    }
}