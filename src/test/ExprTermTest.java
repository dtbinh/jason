package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.RelExpr;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ExprTermTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSolve() {
        NumberTerm nb;
        nb = ArithExpr.parseExpr("3");
        assertEquals(nb.solve(),3.0);

        nb = ArithExpr.parseExpr("3+2");
        assertEquals(nb.solve(),new NumberTermImpl(5).solve());

        nb = ArithExpr.parseExpr("3+2*5");
        assertTrue(nb.solve() == 13);

        nb = ArithExpr.parseExpr("(3+2)*5");
        assertTrue(nb.solve() == 25);

        nb = ArithExpr.parseExpr("3 - 5");
        assertTrue(nb.solve() == -2);
                
        nb = ArithExpr.parseExpr("-(3+5*(4----1))*-1-15");
        // System.out.println(nb+"="+nb.solve());
        assertTrue(nb.solve() == 13d);

        nb = ArithExpr.parseExpr("3+5.1*2");
        // System.out.println(nb+"="+nb.solve());
        assertTrue(nb.solve() == 13.2);
    }

    public void testApply() {
        NumberTerm nb = ArithExpr.parseExpr("(30-X)/(2*X)");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new NumberTermImpl(5));
        u.apply(nb);
        //System.out.println(nb+"="+nb.solve());
        assertEquals(nb, new NumberTermImpl(2.5));
        assertEquals(new NumberTermImpl(2.5), nb);
        assertEquals(new NumberTermImpl(2.5).hashCode(), nb.hashCode());
    }

    public void testUnify() {
        Literal t1 = (Literal) Literal.parseLiteral("p(X*2)").clone();
        Literal t2 = Literal.parseLiteral("p(Y)");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("H"), new NumberTermImpl(5));
        u.unifies(new VarTerm("X"), new VarTerm("H"));
        assertTrue(u.unifies(t1, t2));
        u.apply(t1);
        t1 = (Literal)t1.clone();
        assertEquals(t1.toString(), "p(10)");
        assertTrue(t1.getTerm(0).isNumeric());
        VarTerm yvl = new VarTerm("Y");
        u.apply(yvl);
        assertEquals(yvl, new NumberTermImpl(10));
        u.apply(t2);
        assertEquals(t2.toString(), "p(10)");
    }

    public void testAddAddAdd() {
        Literal t1 = Literal.parseLiteral("p(X+1)");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new NumberTermImpl(0));
        u.apply(t1);
        assertEquals(t1.toString(),"p(1)");

        u = new Unifier();
        u.unifies(Literal.parseLiteral("p(CurVl)"), t1);
        u.unifies(new VarTerm("CurVl"), new VarTerm("X"));
        t1 = Literal.parseLiteral("p(X+1)");
        u.apply(t1);

        u = new Unifier();
        u.unifies(Literal.parseLiteral("p(CurVl)"), t1);
        u.unifies(new VarTerm("CurVl"), new VarTerm("X"));
        t1 = Literal.parseLiteral("p(X+1)");
        u.apply(t1);

        assertEquals(t1.toString(), "p(3)");
    }

	public static void main(String[] a) {
		new ExprTermTest().testLiteralBuilder();
	}

    public void testLiteralBuilder() {
        try {
            Literal l = Literal.parseLiteral("~p(t1,t2)[a1,a2]");
            assertEquals(l.getAsListOfTerms().size(), 3);

            ListTerm lt1 = ListTermImpl.parseList("[~p,[t1,t2],[a1,a2]]");
            assertTrue(l.equals(Literal.newFromListOfTerms(lt1)));
            ListTerm lt2 = ListTermImpl.parseList("[p,[t1,t2],[a1,a2]]");
            assertFalse(l.equals(Literal.newFromListOfTerms(lt2)));
            ListTerm lt3 = ListTermImpl.parseList("[~p,[t1,t2],[a1,a2,a3]]");
            assertFalse(l.equals(Literal.newFromListOfTerms(lt3)));

            Unifier u = new Unifier();
            assertFalse(u.unifies(lt1, lt2));

            assertTrue(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, lt1).logicalConsequence(null, u).hasNext());
            assertFalse(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, lt2).logicalConsequence(null, u).hasNext());
            assertFalse(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, lt3).logicalConsequence(null, u).hasNext());

            VarTerm v = new VarTerm("X");
            u.clear();
            assertTrue(new RelExpr(v, RelExpr.RelationalOp.literalBuilder, lt1).logicalConsequence(null, u).hasNext());
            assertEquals(u.get("X").toString(), l.toString());
            assertEquals(u.get("X"), l);
            assertEquals(l, u.get("X"));

            u.clear();
            assertTrue(new RelExpr(l, RelExpr.RelationalOp.literalBuilder, v).logicalConsequence(null, u).hasNext());
            assertEquals(u.get("X").toString(), lt1.toString());
            assertEquals(u.get("X"), lt1);
            assertEquals(lt1, u.get("X"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
