

import jason.architecture.AgArchInterface;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Example of an agent that only uses Jason BDI engine.
 * It runs without all Jason IDE stuff.
 * (see Jason FAQ for more information about this example)
 * 
 * The class must implement AgArchInterface to be used by the Jason engine.
 */
public class SimpleJasonAgent implements AgArchInterface {

    private static Logger logger = Logger.getLogger(SimpleJasonAgent.class);
    
    public static void main(String[] a) {
    	// Jason classes uses log4j to output messages,
    	// so we need to configure it.
       	Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
       	Logger.getRootLogger().setLevel(Level.INFO);

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
			// args[1] = AgentSpeak source
			String[] args = { null, "demo.asl" };
			fTS = ag.initAg(args, this);
		} catch (Exception e) {
			logger.error("Init error", e);
		}
    }
    
    public void run() {
		try {
			while (isRunning()) {
				// calls the Jason engine to perform one reasoning cycle
				logger.debug("Reasoning....");
				fTS.reasoningCycle();
			}
		} catch (Exception e) {
			logger.error("Run error",e);
		}
	}
	
    public String getAgName() {
    	return "JasonAgent";
    }

    // this method just add some perception for the agent
    public List perceive() { 
    	List l = new ArrayList();
    	l.add(Literal.parseLiteral("x(10)"));
    	return l;
    }

    // this method get the agent actions
    public void act() {
    	ActionExec acExec = fTS.getC().getAction(); 
        if (acExec != null) {
        	logger.info("Agent "+getAgName()+" is doing: "+acExec.getActionTerm());
        }
    }
    
    public boolean isRunning() {
    	return true;
    }
	
    // Not used methods 
    // This simple agent does not need messages/control/...
    public void sendMsg(jason.asSemantics.Message m) throws Exception {}
    public void broadcast(jason.asSemantics.Message m) throws Exception {}    
    public void checkMail() {}
    public void informCycleFinished(boolean breakpoint) {}
}
