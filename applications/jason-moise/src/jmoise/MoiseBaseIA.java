package jmoise;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Common base code for all JMoise+ internal actions.
 * 
 * @author hubner
 */
public abstract class MoiseBaseIA extends DefaultInternalAction  {

	private static Logger logger = Logger.getLogger(MoiseBaseIA.class.getName());

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	String    acName = this.getClass().getSimpleName(); // remove the package name "jmoise"
        Structure acTerm = new Structure(acName);
        acTerm.addTerms(args);
        // remove the last arg if unground (the return of the IA)
        if (!acTerm.getTerm(args.length-1).isGround()) {
        	acTerm.delTerm(args.length-1);
        }
		if (logger.isLoggable(Level.FINE)) logger.fine("sending: "+acTerm);
		
		// send acTerm as message to OrgManager
		try {
            OrgAgent oag = (OrgAgent)ts.getUserAgArch();
	        Message m = new Message("achieve", null, oag.getOrgManagerName(), acTerm);
            oag.sendMsg(m);
            
            if (suspendIntention()) {
                Intention i = ts.getC().getSelectedIntention();
                i.setSuspended(true);
                ts.getC().getPendingIntentions().put("om/"+m.getMsgId(), i);
            }
            return true;
		} catch (JasonException e) {
		    throw e;
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "Error sending "+acTerm+" to OrgManager.",e);
    	}
        return false;
    }
	
    @Override
    public boolean suspendIntention() {
    	return true;
    }
	
}
