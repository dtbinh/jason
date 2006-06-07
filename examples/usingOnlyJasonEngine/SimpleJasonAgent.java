
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.bb.DefaultBeliefBase;
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example of an agent that only uses Jason BDI engine. It runs without all
 * Jason IDE stuff. (see Jason FAQ for more information about this example)
 * 
 * The class must extend AgArch class to be used by the Jason engine.
 */
public class SimpleJasonAgent extends AgArch {

    private static Logger logger = Logger.getLogger(SimpleJasonAgent.class.getName());

    public static void main(String[] a) {
        RunCentralisedMAS.setupLogger();
        SimpleJasonAgent ag = new SimpleJasonAgent();
        ag.run();
    }

    // The BDI Engine (where the AS Semantics is implemented)
    protected TransitionSystem fTS = null;

    public SimpleJasonAgent() {
        // set up the Jason agent
        try {
            Agent ag = new Agent();
            fTS = ag.initAg(this, new DefaultBeliefBase(), "demo.asl", new Settings());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Init error", e);
        }
    }

    public void run() {
        try {
            while (isRunning()) {
                // calls the Jason engine to perform one reasoning cycle
                logger.fine("Reasoning....");
                fTS.reasoningCycle();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Run error", e);
        }
    }

    public String getAgName() {
        return "JasonAgent";
    }

    // this method just add some perception for the agent
    public List<Literal> perceive() {
        List<Literal> l = new ArrayList<Literal>();
        l.add(Literal.parseLiteral("x(10)"));
        return l;
    }

    // this method get the agent actions
    public void act() {
        ActionExec acExec = fTS.getC().getAction();
        if (acExec != null) {
            logger.info("Agent " + getAgName() + " is doing: " + acExec.getActionTerm());
        }
    }

    public boolean canSleep() {
        return true;
    }

    public boolean isRunning() {
        return true;
    }

    // Not used methods
    // This simple agent does not need messages/control/...
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
    }

    public void checkMail() {
    }

    public void informCycleFinished(boolean breakpoint) {
    }
}
