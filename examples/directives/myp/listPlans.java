// Internal action code for project goalpattern.mas2j

package myp;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

public class listPlans extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        for (Plan p: ts.getAg().getPL().getPlans()) {
            if (!p.getLabel().toString().startsWith("kqml")) { // do not list kqml plans
                ts.getLogger().info(p.toString());
            }
        }
        return true;
    }
}

