package jason.asunit;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.CentralisedEnvironment;
import jason.infra.centralised.RunCentralisedMAS;

import java.util.ArrayList;
import java.util.List;

public class TestArch extends CentralisedAgArch implements Runnable {

    private static int nameCount = 0;
    
    private Condition condition;
    private int cycle = 0;
    
    private List<Structure> actions = new ArrayList<Structure>();
    
    StringBuilder output = new StringBuilder();

    public TestArch() {
        this("ASUnitTest" + (nameCount++));
    }

    public TestArch(String agName) {
        setAgName(agName);
        AgArch a = new AgArch();
        a.setArchInfraTier(this);
        setUserAgArch(a);
        RunCentralisedMAS.getRunner().addAg(this);
    }
    
    public int getCycle() {
        return cycle;
    }
    
    public List<Structure> getActions() {
        return actions;
    }
    
    public void start(Condition c) {
        condition = c;
        cycle = 0;
        actions.clear();
        new Thread(this).start();
    }
    
    
    public void run() {
        while (condition.test(this)) {
            cycle++;
            getUserAgArch().getTS().reasoningCycle();
        }
        synchronized (condition) {
            condition.notifyAll();
        }
    }
    
    public void setEnv(Environment env) {
        try {
            CentralisedEnvironment infraEnv = new CentralisedEnvironment(null, RunCentralisedMAS.getRunner());
            infraEnv.setUserEnvironment(env);
            env.setEnvironmentInfraTier(infraEnv);
            setEnvInfraTier(infraEnv);
        } catch (JasonException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List<Literal> perceive() {
        if (getEnvInfraTier() != null)
            return super.perceive();
        else 
            return null;
    }
    
    @Override
    public void act(ActionExec action, List<ActionExec> feedback) {
        actions.add(action.getActionTerm());        
        if (getEnvInfraTier() != null) {
            super.act(action, feedback); //env.scheduleAction(getAgName(), action.getActionTerm(), action);
        } else { 
            action.setResult(true);
            feedback.add(action);
        }
    }
    
    public void print(String s) {
        System.out.println(s);
        output.append(s+"\n");
    }
    
    public StringBuilder getOutput() {
        return output;
    }
    
    public void clearOutput() {
        output = new StringBuilder();
    }
}
