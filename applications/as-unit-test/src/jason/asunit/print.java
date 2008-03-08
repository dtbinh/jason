package jason.asunit;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.stdlib.println;

public class print extends println {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)  throws Exception {
        TestArch arch = (TestArch)ts.getUserAgArch().getArchInfraTier();
        arch.print(argsToString(args));
        return true;
    }
}
