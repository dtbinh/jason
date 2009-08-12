package jason.tests;

import jason.asunit.TestAgent;

import org.junit.Before;
import org.junit.Test;

public class BugListReturnUnification {

    TestAgent ag; 

    @Before
    public void setupAg() {
        ag = new TestAgent();
        ag.setDebugMode(true);
        
        // defines the agent's AgentSpeak code
        ag.parseAScode(
        		"+!test1 <- L=[A,B]; ?bundle(L); jason.asunit.print(L). "+
        		"+?bundle([]). "+
        		"+?bundle([a|T]) <- ?bundle(T). "+

        		"+!test2 <- L=[A,B,C,D]; !bundle(L); jason.asunit.print(L). "+
        		"+!bundle([]). "+
        		"+!bundle([a|T]) <- !bundle(T). "+
        		
        		"+!test3 <- !a(Y)[x(Z), y]; jason.asunit.print(Y, Z). "+
        		"+!a(Y)[x(Z),kk]           <- Y=3; Z=4. "+  		
        		"+!a(Y)[x(Z),source(self)] <- Y=1; Z=2. "  		
        );
    }
    
    @Test(timeout=2000)
    public void testList() {
        ag.addGoal("test1");
        ag.assertPrint("[a,a]", 10);
        ag.addGoal("test2");
        ag.assertPrint("[a,a,a,a]", 10);
    }

    @Test(timeout=2000)
    public void testAnnots() {
        ag.addGoal("test3");
        ag.assertPrint("12", 10);
    }
}
