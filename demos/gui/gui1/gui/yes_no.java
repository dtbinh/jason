package gui;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

import javax.swing.JOptionPane;

public class yes_no extends SuspendInternalAction {

    private Logger logger = Logger.getLogger("gui."+yes_no.class.getName());

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, final Term[] args) throws Exception {
        try {
            // suspend the intention (max 5 seconds)
            final String key = suspend(ts, "gui", 5000); 

            // to not block the agent thread, 
            // create a new thread that show the GUI and resume the intention latter 
            new Thread() {
                public void run() {
                    int answer = JOptionPane.showConfirmDialog(null, args[0].toString());
                    // resume the intention with success
                    if (answer == JOptionPane.YES_OPTION)
                        yes_no.this.resume(ts, key);
                    else
                        fail(ts, key);
                }
            }.start();
            
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'gui.yes_no'! "+e);
        }
        return false;
    }
    
    /** called back when some intention should be resumed/failed by timeout */
    @Override
    public void timeout(TransitionSystem ts, String intentionKey) {
        // this method have to decide what to do with actions finished by timeout
        // 1: resume
        //resume(ts,intentionKey);
        
        // 2: fail
        fail(ts, intentionKey);
    }
}
