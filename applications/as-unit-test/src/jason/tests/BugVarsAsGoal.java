package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** based on bug found by Jorgen Villadsen */
public class BugVarsAsGoal {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "+!g <- +p(ggg); !gg; Y={!ggg}; Y. " + 
                "+!gg : p(X) <- +X; !!X. " +
                "+!ggg[source(A)] <- jason.asunit.print(A). "
        );
    }
       
    @Test(timeout=2000)
    public void testGoal() {
        ag.addGoal("g");
        ag.assertPrint("self", 10);
        ag.assertPrint("self", 10);
    }
}
