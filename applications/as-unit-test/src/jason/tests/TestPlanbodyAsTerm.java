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
                
                "+!test5 <- !g5(R); jason.asunit.print(R). "+
                "+!g5(X) <- C; .print(errrrrror). "+
                "-!g5(X)[error(I),error_msg(M)] <- jason.asunit.print(I,M); X = ok. "+

                "+!test6 <- A = { a }; B = { A }; C = { B }; C; jason.asunit.print(end). "+
                
                "+!test7(X) <- A = { jason.asunit.print(a,X); jason.asunit.print(b,X*2) }; A; jason.asunit.print(end,X/2). "+
                
                "+!g({A; R}) <- A; !g(R). "+
                "+!g(A)    <- A." +
                "+!g2(A)     <- jason.asunit.print(A)."
        );
    }
    
    @Test
    public void testProgram1a() {
        ag.addGoal("start");
        ag.assertBel("g({a(1);b;c})", 5);
        ag.assertAct("a(1)", 5);
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

    @Test
    public void test5() {
        ag.addGoal("test5");
        ag.assertPrint("body_var_ungroundstringcode:1: Variable 'C' must be ground.",10);
        ag.assertPrint("ok", 10);
    }

    @Test
    public void test6() {
        ag.addGoal("test6");
        ag.assertAct("a", 5);
        ag.assertPrint("end", 5);
    }

    @Test
    public void test7() {
        ag.addGoal("test7(100)");
        ag.assertPrint("a100", 5);
        ag.assertPrint("b200", 5);
        ag.assertPrint("end50", 5);
    }
}
