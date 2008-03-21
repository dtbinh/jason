package jason.asunit;

import static org.junit.Assert.fail;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Structure;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.io.StringReader;
import java.util.logging.Level;

public class TestAgent extends Agent {
    
    public TestAgent() {
        try {
            TestArch arch = new TestArch();
            arch.getUserAgArch().setTS(initAg(arch.getUserAgArch(), null, null, new Settings()));
        } catch (JasonException e) {
            logger.log(Level.SEVERE, "Error creating TestArch", e);
        }
    }
    
    public boolean parseAScode(String aslCode) {
        try {
            setASLSrc("stringcode");
            parseAS(new StringReader(aslCode));
            addInitialBelsInBB();
            addInitialGoalsInTS();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing\n"+aslCode+": "+e.getMessage());
            return false;
        }
    }

    public TestArch getArch() {
        return (TestArch)getTS().getUserAgArch().getArchInfraTier();
    }
    
    public void setDebugMode(boolean on) {
        if (on) {
            getTS().getLogger().setLevel(Level.FINE);
            getArch().getLogger().setLevel(Level.FINE);
            getTS().getAg().getLogger().setLevel(Level.FINE);
        } else
            getTS().getLogger().setLevel(Level.INFO);            
    }
    
    // --------------------------------------------
    //   methods to change the state of the agent
    // --------------------------------------------
    
    public void addGoal(String g) {
        try {
            addGoal(Literal.tryParsingLiteral(g));
        } catch (Exception e) {
            fail("Parsing '"+g+"' as literal for a goal failed!");
        }
    }
    public void addGoal(Literal g) {
        getTS().getC().addAchvGoal(g, Intention.EmptyInt);
    }
    
    public void addBel(String bel) {
        try {
            super.addBel(Literal.tryParsingLiteral(bel));
        } catch (ParseException e) {
            fail("Parsing '"+bel+"' as a belief!");
        } catch (RevisionFailedException e) {
            fail("BRF error for adding '"+bel+"!");
        }        
    }
    public void delBel(String bel) {
        try {
            Literal l = Literal.tryParsingLiteral(bel);
            if (!l.hasSource()) {
                l.addAnnot(BeliefBase.TSelf);
            }
            super.delBel(l);
        } catch (ParseException e) {
            fail("Parsing '"+bel+"' as a belief!");
        } catch (RevisionFailedException e) {
            fail("BRF error for deleting '"+bel+"!");
        }        
    }
    
    // --------------------------------------------
    //   assert methods
    // --------------------------------------------
    
    public void assertBel(String formula, int maxCycles) {
        try {
            assertBel(LogExpr.tryParsingLogExpr(formula), maxCycles);
        } catch (ParseException e) {
            fail("Parsing '"+formula+"' as a formula failed!");
        }
    }
    public void assertBel(final LogicalFormula bel, final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return believes(bel, new Unifier());
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertBel("+bel+")");
    }
    
    
    public void assertEvt(String te, int maxCycles) {
        try {
            assertEvt(Trigger.tryParsingTrigger(te), maxCycles);
        } catch (ParseException e) {
            fail("Parsing '"+te+"' as trigger failed!");
        }
    }
    public void assertEvt(Trigger te, final int maxCycles) {
        final Event evt = new Event(te, Intention.EmptyInt);
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return getTS().getC().getEvents().contains(evt);
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertEvt("+te+")");
    }
    

    public void assertAct(String act, int maxCycles) {
        try {
            assertAct(Structure.tryParsingStructure(act), maxCycles);
        } catch (ParseException e) {
            fail("Parsing '"+act+"' as action failed!");
        }
    }
    public void assertAct(final Structure act, final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                //System.out.println(arch.getCycle() + " " + arch.getActions()+ getTS().getC().getFeedbackActions());
                return arch.getActions().contains(act);
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertAct("+act+")");
    }
    
    
    public void assertIdle(final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return getTS().canSleep();
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertIdle");
    }

    public void assertPrint(final String out, final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                boolean result = arch.getOutput().indexOf(out) >= 0;
                return result;
            }
        };
        if (assertMaxCyclesAndAnotherCondition(c, maxCycles))
            getArch().clearOutput();
        else
            fail("failed assertPrint("+out+")");
    }
    
    
    private boolean assertMaxCyclesAndAnotherCondition(final Condition c, final int maxCycles) {
        if (maxCycles <= 0)
            return c.test(getArch());
        try {
            Condition mc = new Condition() {
                public boolean test(TestArch arch) {
                    return arch.getCycle() < maxCycles && !c.test(arch);
                }
            };
            getArch().start(mc);
            synchronized (mc) { mc.wait(); }
            return c.test(getArch());
        } catch (InterruptedException e) {}
        return false;
    }
    
}
