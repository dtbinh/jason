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
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.Iterator;

public class findall extends DefaultInternalAction {

    /** .findall(Var, a(Var), List) */
    @Override
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term var = (Term) args[0].clone();
            Literal bel = Literal.parseLiteral(args[1].toString());
            if (bel == null) {
                throw new JasonException("The second parameter ('" + args[1] + "') of the internal action 'findAll' is not a literal!");
            }
            un.apply(bel);
            // find all bel in belief base and build a list with them
            ListTerm all = new ListTermImpl();
            Iterator<Unifier> iu = bel.logCons(ts.getAg(), un);
            while (iu.hasNext()) {
                Unifier nu = iu.next();
                Term vl = (Term) var.clone();
                nu.apply(vl);
                all.add(vl);
            }
            Term list = args[2];
            return un.unifies(list, all);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'findall' has not received three arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'findall': " + e);
        }
    }
}
