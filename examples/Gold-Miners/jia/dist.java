package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class dist extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            NumberTerm agx = (NumberTerm) terms[0];
            NumberTerm agy = (NumberTerm) terms[1];
            NumberTerm tox = (NumberTerm) terms[2];
            NumberTerm toy = (NumberTerm) terms[3];

            un.apply(agx);
            un.apply(agy);
            un.apply(tox);
            un.apply(toy);

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
