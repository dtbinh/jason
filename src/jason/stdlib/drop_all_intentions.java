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

import jason.asSemantics.*;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.drop_all_intentions</code></b>.
  
  <p>Description: removes all intentions from the agent's set of
  intentions (event suspended intentions are removed). 
  No event is produced.

  <p>This action changes the agent's circumstance structure by simply
  emptying the whole set of intentions (I), pending actions (PA),
  pending intentions (PI), and events in E that are not external
  events (are thus generated by intentions).

  <p>Example:<ul> 

  <li> <code>.drop_all_intentions</code>.

  </ul>

  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_desire
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_goal
  @see jason.stdlib.intend


 */
public class drop_all_intentions extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Circumstance C = ts.getC();
        C.clearIntentions();
        C.clearPendingIntentions();
        C.clearPendingActions();

        // drop intentions in E
        for (Event e: C.getEvents()) {
            // it is not an external event
            if (e.getIntention() != Intention.EmptyInt) {
                C.removeEvent(e);
            }
        }

        return true;
    }
}
