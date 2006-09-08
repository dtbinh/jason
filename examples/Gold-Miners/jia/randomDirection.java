package jia;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;

import java.util.Random;

import mining.WorldModel;


public class randomDirection implements InternalAction {
    Random rnd = new Random();

    public boolean execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = null;

            WorldModel model = WorldModel.get();

            NumberTerm agx = (NumberTerm) terms[0].clone();
            un.apply((Term) agx);
            NumberTerm agy = (NumberTerm) terms[1].clone();
            un.apply((Term) agy);
            int iagx = (int) agx.solve();
            int iagy = (int) agy.solve();
            int itox = -1;
            int itoy = -1;
            while (!model.isFree(itox, itoy)) {
                switch (rnd.nextInt(4)) {
                case 0:
                    itox = iagx - 1;
                    sAction = "left";
                    break;
                case 1:
                    itox = iagx + 1;
                    sAction = "right";
                    break;
                case 2:
                    itoy = iagy - 1;
                    sAction = "up";
                    break;
                case 3:
                    itoy = iagy + 1;
                    sAction = "down";
                    break;
                }
            }
            return un.unifies(terms[2], new TermImpl(sAction));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
