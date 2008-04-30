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
        		"p(1,a). p(2,a). p(3,b). p(4,b). p(6,a). "+
                "+!test1 <- .while( .count(b(_),N) & N < 4, {+b(N+1) })."+
                
                "+!test2 <- L=4; .while( .count(b(_)) < L, { ?b(X); +b(X+1) }); jason.asunit.print(end). "+
                
                "+!test3 <- L=4; .for( p(N,a) & N < L, { jason.asunit.print(N) }); jason.asunit.print(end). "+

                "+!test4 <- .for( .member(N, [1,3,4]), { jason.asunit.print(N) }); jason.asunit.print(end). "
        );
    }
    
    @Test
    public void testWhile1() {
        ag.addGoal("test1");
        ag.assertBel("b(4)", 20);
    }

    @Test
    public void testWhile2() {
        ag.addGoal("test2");
        ag.assertBel("b(4)", 30);
    }

    @Test
    public void testFor1() {
        ag.addGoal("test3");
        ag.assertPrint("2", 10);
        ag.assertPrint("end", 10);
    }

    @Test
    public void testFor2() {
        ag.addGoal("test4");
        ag.assertPrint("4", 10);
        ag.assertPrint("end", 10);
    }
}
