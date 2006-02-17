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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.5  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.4  2005/08/12 21:12:50  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class broadcast implements InternalAction {

	public boolean execute(TransitionSystem ts, Unifier un, Term[] args)
			throws Exception {
		Term ilf = null;
		Term pcnt = null;

		try {
			ilf = (Term) args[0].clone();
			// if (ilf == null) {
			// throw new JasonException("The Ilf Force parameter of the internal
			// action 'broadcast' is not a term!");
			// }
			if (!ilf.isGround()) {
				throw new JasonException(
						"The Ilf Force parameter of the internal action 'broadcast' is not a ground term!");
			}

			pcnt = (Term)args[1].clone();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException(
					"The internal action 'broadcast' has not received two arguments");
		}
		un.apply(pcnt);
		if (!pcnt.isGround()) {
			throw new JasonException("The content of the message '" + pcnt
					+ "' is not ground!");
		}

		Message m = new Message(ilf.toString(), null, null, pcnt.toString());

		try {
			ts.getUserAgArch().broadcast(m);
			return true;
		} catch (Exception e) {
			throw new JasonException("Error broadcasting message " + pcnt);
		}
	}

}
