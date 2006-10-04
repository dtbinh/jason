package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class neighbour extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            NumberTerm agx = (NumberTerm) terms[0].clone();
            un.apply((Term) agx);
            NumberTerm agy = (NumberTerm) terms[1].clone();
            un.apply((Term) agy);
            NumberTerm tox = (NumberTerm) terms[2].clone();
            un.apply((Term) tox);
            NumberTerm toy = (NumberTerm) terms[3].clone();
            un.apply((Term) toy);
            int iagx = (int) agx.solve();
            int iagy = (int) agy.solve();
            int itox = (int) tox.solve();
            int itoy = (int) toy.solve();
            return new Location(iagx, iagy).isNeigbour(new Location(itox, itoy));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
