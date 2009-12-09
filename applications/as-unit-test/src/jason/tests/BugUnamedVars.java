package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

/** bug reported by Tim Cleaver in jason-bugs */

public class BugUnamedVars {

    TestAgent ag; 

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent("a");
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
                "wrap([],[]). "+
                "wrap([_ | Rest], [wrapped(_) | Result]) :-  wrap(Rest, Result). "+
                "+!start : wrap([a,b,c],R) & R = [wrapped(a), wrapped(b), wrapped(c)] <- jason.asunit.print(ok)."+
                "+!start : wrap([a,b,c],R) & R = [wrapped(a), wrapped(a), wrapped(a)] <- jason.asunit.print(nok). "
        );
    }
    
    @Test(timeout=2000)
    public void testWrap() {
        ag.addGoal("start");
        ag.assertPrint("ok", 20);
    }

}
