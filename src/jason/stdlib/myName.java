//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class myName implements InternalAction {

	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		Term name = null;
		try {
			name = args[0];
			if (name == null) {
				throw new JasonException("The parameter Name of internal action 'myName' is not a term!");
			}
			// do not need to be VAR
			//if (!name.isVar()) {
			//	throw new JasonException("The parameter Name of internal action 'myName' is not a variable!");
			//}

		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'myName' has not received one argument");
		}
		return un.unifies(name, new Term(ts.getAgArch().getAgName()));
	}
}
