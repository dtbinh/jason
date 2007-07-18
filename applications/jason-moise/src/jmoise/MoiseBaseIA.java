package jmoise;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MoiseBaseIA extends DefaultInternalAction  {

	private static Logger logger = Logger.getLogger(MoiseBaseIA.class.getName());

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	String    acName = this.getClass().getSimpleName(); // remove the package name "jmoise"
        Structure acTerm = new Structure(acName);
		for (int i=0; i<args.length; i++) {
			acTerm.addTerm(args[i]);
		}
		if (logger.isLoggable(Level.FINE)) logger.fine("sending:"+acTerm);
		
		// send acTerm as message to OrgManager
		try {
            OrgAgent oag = (OrgAgent)ts.getUserAgArch();
	        Message m = new Message("achieve", null, oag.getOrgManagerName(), acTerm);
            oag.sendMsg(m);
            return true;
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "Error sending "+acTerm+" to OrgManager.",e);
    	}
        return false;
    }
}
