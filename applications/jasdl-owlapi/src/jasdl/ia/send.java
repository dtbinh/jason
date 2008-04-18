/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package jasdl.ia;

import jasdl.asSemantics.JASDLAgent;
import jasdl.bridge.DLUnifier;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class send extends jason.stdlib.send {

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		// replace unifier with a DL-unifier in-case synchronous ask is used

		JASDLAgent agent = (JASDLAgent) ts.getAg();
		Intention i = ts.getC().getSelectedIntention();
		DLUnifier dlun = new DLUnifier(agent);
		dlun.compose(i.peek().getUnif());
		i.peek().setUnif(dlun);

		return super.execute(ts, un, args);
	}

}
