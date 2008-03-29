package jason.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BugVarsInInitBels.class,
    TestAddLogExprInBB.class,
    TestKQML.class,
    TestVarInContext.class,
    TestPlanbodyAsTerm.class
 })
public class TestAll { }
