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
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

public class concat extends DefaultInternalAction {

	/**
	 * Concat list args[0] with args[1] and unifies with args[2]
	 */
    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args)	throws Exception {
		Term l1 = (Term)args[0].clone();
		Term l2 = (Term)args[1].clone();
		Term l3 = (Term)args[2].clone();
		un.apply(l1);
		un.apply(l2);
		un.apply(l3);
		
		if (l1.isList()) {
			if (!l2.isList()) {
				throw new JasonException("arg[1] is not a list (concat)");
			}
			if (!l3.isVar() && !l3.isList()) {
				throw new JasonException("arg[2] is not a list or variable (concat)");
			}
		
			ListTerm l1l = (ListTerm)l1;
			l1l.concat((ListTerm)l2);
			return un.unifies(l3, l1l);
		} else {
			String v1 = l1.toString();
			if (l1.isString()) {
				v1 = ((StringTerm)l1).getString();
			}
			String v2 = l2.toString();
			if (l2.isString()) {
				v2 = ((StringTerm)l2).getString();
			}
			if (!l3.isVar() && !l3.isString()) {
				throw new JasonException("arg[2] is not a string or variable (concat)");
			}
		
			return un.unifies(l3, new StringTermImpl(v1+v2));
		}
	}
}
