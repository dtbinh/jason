

import jason.D;
import jason.architecture.AgentArchitecture;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;

import java.util.List;

/**
 * Example of an agent that only uses Jason BDI engine.
 * It runs without all Jason IDE stuff.
 * 
 * The class must implement AgentArchitecture to be used by the Jason engine.
 */
public class SimpleJasonAgent implements AgentArchitecture {
    
	public static void main(String[] a) {
		SimpleJasonAgent ag = new SimpleJasonAgent();
		ag.run();
	}

	// The BDI Engine (where the AS Semantics is implemented) 
	protected TransitionSystem fTS = null;
	
    public SimpleJasonAgent() {
        // set up the Jason agent
        try {
            Agent ag = new Agent();
			// Create args for the agent
			//   args[1] = AgentSpeak source
			//   args[2] = Jason Directory 
			String[] args = {null, "demo.asl", "../.."};
            fTS = ag.initAg(args, this);
	    } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
		try {
			while (true) {
				// calls the Jason engine to perform one reasoning cycle
				fTS.reasoningCycle();
	        }
		} catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public String getName() {
		return "JasonAgent";
	}

	// this method just add some belief in the agent Belief Set
	public void brf() {
		Literal bel = Literal.parseLiteral("x(10)");
        fTS.getAg().addBel(bel, D.TSelf, fTS.getC(), D.EmptyInt);
    }

	// this method get the agent actions
	public void act() {
    	ActionExec acExec = fTS.getC().getAction(); 
        if (acExec != null) {
			System.out.println("Agent "+getName()+" is doing: "+acExec.getActionTerm());
        }
    }
    
    
    // Not used methods 
	// These simple agent does not have messages/control/...
	public List perceive() { return null; }
    public void sendMsg(jason.asSemantics.Message m) throws Exception {}
    public void broadcast(jason.asSemantics.Message m) throws Exception {}    
    public void checkMail() {}
	public void informCycleFinished() {}

}
