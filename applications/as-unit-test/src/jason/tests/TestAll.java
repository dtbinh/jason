package jason.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BugVarsInInitBels.class,
    TestAddLogExprInBB.class,
    TestGoalSource.class, 
    TestIF.class, 
    TestKQML.class,
    TestPlanbodyAsTerm.class,
    TestPlanFailure.class,
    TestVarInContext.class
 })
public class TestAll { }
