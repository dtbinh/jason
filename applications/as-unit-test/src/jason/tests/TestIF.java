package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestIF {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "b(3). " +
                "+!test1 <- a1; "+
                "           .conditional(b(X), {jason.asunit.print(X); b1}, {jason.asunit.print(no);b2}); "+
                "           a2. "+
                "+!test2 <- -b(_); !test1. "
        );
    }
    
    @Test
    public void test1() {
        ag.addGoal("test1");
        ag.assertPrint("3", 5);
        ag.assertAct("b1", 5);
        ag.assertAct("a2", 5);
    }

    @Test
    public void test2() {
        ag.addGoal("test2");
        ag.assertPrint("no", 5);
        ag.assertAct("b2", 5);
        ag.assertAct("a2", 5);
    }
}
