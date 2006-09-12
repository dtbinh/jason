package jia;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class dist implements jason.asSemantics.InternalAction {

    public boolean execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
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
            int dist = new Location(iagx, iagy).distance(new Location(itox, itoy));
            return un.unifies(terms[4], new NumberTermImpl(dist));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
