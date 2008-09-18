package jason.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BugListReturnUnification.class,
    BugVarsAsArg.class,
    BugVarsInInitBels.class,
    TestAddLogExprInBB.class,
    TestGoalSource.class,
    TestIA.class, 
    TestIAdelete.class, 
    TestIF.class,
    TestKQML.class,
    TestLoop.class,
    TestPlanbodyAsTerm.class,
    TestPlanFailure.class,
    TestVarInContext.class
 })
public class TestAll { }
