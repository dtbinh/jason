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
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.broadcast</code></b>.
  
  <p>Description: broadcasts a message to all known agents.
  
  <p>Parameters:<ul>
  
  <li>+ ilf (atom): the illocutionary force of the message (tell,
  achieve, ...). <br/>
  
  <li>+ message (literal): the content of the message.<br/>
  
  </ul>
  
  <p>Example:<ul> 

  <li> <code>.broadcast(tell,value(10))</code>: sends <code>value(10)</code>
  as a "tell" message to all known agents in the society.</li>

  </ul>

  @see jason.stdlib.send
  @see jason.stdlib.my_name

*/
public class broadcast extends DefaultInternalAction {

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		Term ilf = null;
		Term pcnt = null;

		try {
			ilf = args[0];
			if (!ilf.isAtom()) {
				throw new JasonException("The illocutionary force parameter of the internal action 'broadcast' is not an atom!");
			}

			pcnt = args[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'broadcast' has not received two arguments.");
		}
		if (!pcnt.isGround()) {
			throw new JasonException("The content of the message '" + pcnt + "' is not ground!");
		}

		Message m = new Message(ilf.toString(), null, null, pcnt);

		try {
			ts.getUserAgArch().broadcast(m);
			return true;
		} catch (Exception e) {
			throw new JasonException("Error broadcasting message " + pcnt, e);
		}
	}

}
