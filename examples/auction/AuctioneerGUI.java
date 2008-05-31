import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import jason.architecture.*;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;

import javax.swing.*;

/** example of agent architecture's functions overriding */
public class AuctioneerGUI extends AgArch {

    JTextArea jt;
    JFrame    f;
    
    int auctionId = 0;

    public AuctioneerGUI() {
        jt = new JTextArea(10, 30);
        JButton auction = new JButton("Start new auction");
        auction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                auctionId++;
                getTS().getC().addAchvGoal(Literal.parseLiteral("start_auction("+auctionId+")"), null);
            }
        });
        
        f = new JFrame("Auctioneer agent");
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(BorderLayout.CENTER, new JScrollPane(jt));
        f.getContentPane().add(BorderLayout.SOUTH, auction);
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
