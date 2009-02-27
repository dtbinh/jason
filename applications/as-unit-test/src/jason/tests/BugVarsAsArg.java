package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by J¿rgen Villadsen */
public class BugVarsAsArg {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "+!g <- +p(ggg); !gg. " + 
                "+!gg : p(X) <- !!X. " +
                "+!ggg[source(A)] <- jason.asunit.print(A). "
        );
    }
    
    
    
    @Test 
    public void testGoal() {
    	ag.addGoal("g");
        ag.assertPrint("self", 10); 
    }
}
