
import java.util.*;
import jason.*;
import jason.asSyntax.*;
import jason.environment.*;

public class testOWEnv extends Environment {
    
    Literal pa  = Literal.parseLiteral("p(a)");
    Literal pb  = Literal.parseLiteral("p(b)");
    Literal npa = Literal.parseLiteral("~p(a)");
    Literal npb = Literal.parseLiteral("~p(b)");
    Random random;
    
    public testOWEnv() {
        // initial percepts
        addPercept(pa);
        addPercept(pb);
        addPercept(npa);
        addPercept(npb);
        random = new Random(System.currentTimeMillis());
    }
    
    /**
     * Implementation of the agent's basic actions
     */
    public boolean executeAction(String ag, Term act) {
		clearPercepts();
        if (random.nextBoolean())
            addPercept(pa);
        if (random.nextBoolean())
            addPercept(pb);
        if (random.nextBoolean())
            addPercept(npa);
        if (random.nextBoolean())
            addPercept(npb);
        return true;
    }
}
