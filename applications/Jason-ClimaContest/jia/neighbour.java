// Internal action code for project jasonTeam.mas2j

package jia;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

public class neighbour implements jason.asSemantics.InternalAction {

	private Logger logger = Logger.getLogger("jasonTeam.mas2j."+neighbour.class.getName());

	 public boolean execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
		try {
			NumberTerm agx = (NumberTerm)terms[0].clone(); un.apply((Term)agx);
			NumberTerm agy = (NumberTerm)terms[1].clone(); un.apply((Term)agy);
			NumberTerm tox = (NumberTerm)terms[2].clone(); un.apply((Term)tox);
			NumberTerm toy = (NumberTerm)terms[3].clone(); un.apply((Term)toy);
			int iagx = (int)agx.solve();
			int iagy = (int)agy.solve();
			int itox = (int)tox.solve();
			int itoy = (int)toy.solve();
			if (Math.abs(iagx - itox) + Math.abs(iagy - itoy) == 1) return true;
			if (Math.abs(iagx - itox) == 1 && Math.abs(iagy - itoy) == 1) return true;
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
}

