package arch;

import java.util.List;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSyntax.Literal;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

/** 
 *  An agent architecture that try to identify a crashed agent and then try to fix it.
 * 
 *  @author Jomi
 */
public class IdentifyCrashed extends AgArch {

    private boolean didPercept = true;
    private int     maxCycleTime = 3000;
    private boolean dead = false;
    private Thread  agThread = null;
    private Thread  checkThread = null;
    
    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        processParameters();
        createCheckThread();
    }

    void processParameters() {
        String arg = getTS().getSettings().getUserParameter("maxCycleTime");
        if (arg != null) 
            maxCycleTime = Integer.parseInt(arg);        
    }
    
    void createCheckThread() {
        // creates a thread that check if the agent is dead
        checkThread = new TestDead();
        checkThread.start();
    }
    
    @Override
    public List<Literal> perceive() {
        agThread = Thread.currentThread(); 
        doPercept();
        return super.perceive();
    }
    
    private synchronized void doPercept() {
        didPercept = true;
        notifyAll();
    }
    
    public boolean didPercept() {
        return didPercept;
    }
    
    public boolean isCrashed() {
        return !didPercept;
    }
    
    private synchronized void waitPercept() throws InterruptedException {
        wait(maxCycleTime);
    }

    @Override
    public void stopAg() {
        checkThread.interrupt();
        super.stopAg();
    }
    
    /** try to fix the agent: approach 1: simulates the agent has stopped */
    protected boolean fix1() throws Exception {
        getTS().getLogger().warning("I am dead!");
        
        // simulates a stop running
        dead = true;

        // gives some time to TS get the change in state
        waitPercept();
        try {
            if (isCrashed())
                return fix2();
            else
                return true;
        } finally {
            // start the agent again
            dead = false;
        }
    }

    /** try to fix the agent: approach 2: interrupt the agent thread */
    protected boolean fix2() throws Exception {
        getTS().getLogger().warning("I am still dead!");
        // try to interrupt the agent thread.
        agThread.interrupt();
        
        waitPercept();
        if (isCrashed()) {
            getTS().getLogger().warning("Interrupt doesn't work!!! The agent remains dead!");
            return fix3();
        } else {
            return true;
        }
    }
    
    /** try to fix the agent: approach 3: user defined */
    protected boolean fix3() throws Exception {
        return false;
    }    
    
    @Override
    public boolean isRunning() {
        return !dead && super.isRunning();
    }
    
    class TestDead extends Thread {
        public TestDead() {
            super("TestDeadAg-"+getAgName());
        }
        @Override
        public void run() {
            try {
                while (IdentifyCrashed.super.isRunning()) {
                    didPercept = false;
                    sleep(maxCycleTime);
                    if (isCrashed()) {
                        // agent is dead!
                        fix1();                        
                    }
                }
            } catch (Exception e) {} // no problem the agent should stop running, simply quite the thread 
        }
    }
}
