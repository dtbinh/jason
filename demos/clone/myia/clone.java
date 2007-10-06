// Internal action code for project clone.mas2j

package myia;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.RunCentralisedMAS;

public class clone extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	
    	String agName = ((StringTerm)args[0]).getString();
    	
    	// create infrastructure architecture (it works only for centralised infra!)
    	CentralisedAgArch infraArch = new CentralisedAgArch();
    	infraArch.setAgName(agName);
    	infraArch.setEnvInfraTier(RunCentralisedMAS.getRunner().getEnvironmentInfraTier());
    	RunCentralisedMAS.getRunner().addAg(infraArch);
    	
    	// create user level architecture
    	AgArch arch = new AgArch();
        arch.setArchInfraTier(infraArch);
        
        // create the agent clone
        Agent clone = ts.getAg().clone(arch);
        arch.setTS(clone.getTS());
        
        // start the agent
        infraArch.setUserAgArch(arch);
    	infraArch.setLogger();
        (new Thread(infraArch, agName)).start();
        
        return true;
    }
}

