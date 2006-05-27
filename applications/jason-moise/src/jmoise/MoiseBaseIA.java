package jmoise;

import jason.asSemantics.InternalAction;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MoiseBaseIA implements InternalAction {

	Logger logger = Logger.getLogger(MoiseBaseIA.class.getName());


    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	String acName = this.getClass().getName().substring(7); // remove the package name "jmoise"
		Term acTerm = new TermImpl(acName);
		for (int i=0; i<args.length; i++) {
			acTerm.addTerm(args[i]);
		}
		un.apply(acTerm);
		if (logger.isLoggable(Level.FINE)) logger.fine("sending:"+acTerm);
		
		// send acTerm as message to OrgManager
		try {
	        Message m = new Message("achieve", null, "orgManager", acTerm.toString());
            ts.getUserAgArch().sendMsg(m);
            return true;
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "Error sending "+acTerm+" to OrgManager.",e);
    	}
        return false;
    }
}
