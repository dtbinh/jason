package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class TestLoop {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
        		"b(1). "+
                "+!test1 <- .while( .count(b(_),N) & N < 4, {+b(N+1) })."+
                
                "+!test2 <- L=4; .while( .count(b(_)) < L, { ?b(X); +b(X+1) }); jason.asunit.print(end)."
        );
    }
    
    @Test
    public void test1() {
        ag.addGoal("test1");
        ag.assertBel("b(4)", 20);
    }

    @Test
    public void test2() {
        ag.addGoal("test2");
        ag.assertBel("b(4)", 30);
    }
}
