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
                "+!start <- +g( {a(1); b; c}); ?g(X); !g(X). " +
                "+!test2 <- !g( {!g2(1)}). "+
                "+!test3 <- !g2(-1 + 2)."+
                "+!test4 <- X = {a(1); b; c}; !g(X)."+
                
                "+!g({A; R}) <- A; !g(R). "+
                "+!g(A)      <- A." +
                "+!g2(A)     <- jason.asunit.print(A)."
        );
    }
    
    @Test
    public void testProgram1a() {
        ag.addGoal("start");
        ag.assertBel("g({a(1);b;c})", 5);
        ag.assertAct("a(1)", 4);
        ag.assertAct("b", 4);
        ag.assertAct("c", 4);
    }
    
    @Test
    public void testProgram1b() {
        ag.addGoal("test4");
        ag.assertAct("a(1)", 4);
        ag.assertAct("b", 4);
        ag.assertAct("c", 4);
    }

    @Test
    public void testProgram2() {
        ag.addGoal("test2");
        ag.assertPrint("1", 5);
    }

    @Test
    public void testProgram3() {
        ag.addGoal("test3");
        ag.assertPrint("1", 5);
    }

}
