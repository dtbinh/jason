package myPkg;

import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.environment.Environment;

public class testEnv extends Environment {
    
    static Term a1 = TermImpl.parse("a(b)");
    static Term a2 = TermImpl.parse("b(c)");
    static Literal p1 = Literal.parseLiteral("s(b)");
    
    public testEnv() {
        // initial global percepts
        addPercept(Literal.parseLiteral("p(a)"));
        addPercept(Literal.parseLiteral("p(b)"));
        addPercept(Literal.parseLiteral("q(a,b)"));
        addPercept(Literal.parseLiteral("r(b)"));
        addPercept(Literal.parseLiteral("t(b,c)"));
        addPercept(Literal.parseLiteral("s(b)"));
        
    }

    /**
	 * Implementation of the agent's basic actions
	 */
    public boolean executeAction(String ag, Term act) {
        try {
            if (act.equals(a1)) {
                addPercept(p1);
            } else if (act.equals(a2)) {
                removePercept(p1);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Unexpected agent action");
            e.printStackTrace();
            return false;
        }
    } 
}
