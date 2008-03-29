package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestPlanbodyAsTerm {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "+!start <- +g(a; b; c); ?g(X); !g(X). "+
                "+!g(A; R) <- A; !g(R). "+
                "+!g(A)    <- .print(A); A; .print(end)."
        );
    }
    
    @Test
    public void testProgram() {
        ag.addGoal("start");
        ag.assertBel("g(a;b;c)", 5);
        ag.assertAct("a", 4);
        ag.assertAct("b", 4);
        ag.assertAct("c", 4);
    }

}
