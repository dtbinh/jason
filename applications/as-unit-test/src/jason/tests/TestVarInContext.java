package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestVarInContext {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        ag.setDebugMode(true);
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "b1[b]. b2[c]. b3[d]. b4[a,d]. "+
                "+!test1 : P[e] | P[c] <- jason.asunit.print(P). " + 
                
                "+!test2 : P[e] & P[c] <- jason.asunit.print(P). " + 
                "-!test2               <- jason.asunit.print(\"error\"). " + 
                
                "+!test3 : P[a] & P[d] <- jason.asunit.print(P). " 
        );
    }
    
    @Test
    public void testContext() {
        ag.addGoal("test1");
        ag.assertPrint("b2", 5);
        
        ag.addGoal("test2");
        ag.assertPrint("error", 5);

        ag.addGoal("test3");
        ag.assertPrint("b4", 5);
    }

}
