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
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.Iterator;


/**
  <p>Internal action: <b><code>.drop_desire(<i>D</i>)</code></b>.
  
  <p>Description: removes desire <i>D</i> in the agent circumstance. Currently
  what it does is to remove all <i>+!D</i> entries in the set of events.
  No event is produced.

  <p><b>Important</b>: unlike <code>.desire</code> this only alters literals
  explicitly desired (rather than intended); that is, it does NOT consider
  intentions. You should use both <code>.drop_desire</code> AND
  <code>drop_intention</code> to remove all desires and intentions matching
  <i>D</i>.

  <p>Example:<ul> 

  <li> <code>.drop_desire(go(X,Y))</code>: remove desires such as
  <code>&lt;+!go(1,3),_&gt;</code> from the set of events.

  </ul>

 
  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.intend


 */
public class drop_desire extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal l = (Literal)args[0]; 
            l.apply(un);
            
            Event e = new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l),Intention.EmptyInt);
            Iterator<Event> ie = ts.getC().getEvents().iterator();
            while (ie.hasNext()) {
            //for (Event ei : ts.getC().getEvents()) {
            	Event ei = ie.next();
                Trigger t = ei.getTrigger();
                if (ei.getIntention() != Intention.EmptyInt) {
                    t = (Trigger) t.clone();
                    t.getLiteral().apply(ei.getIntention().peek().getUnif());
                }
                if (un.unifies(t, e.getTrigger())) {
                    // old implementation: t.setTrigType(Trigger.TEDel); // Just changing "+!g" to "-!g"
                	ie.remove();
                }
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'drop_desire' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'drop_desire': " + e);
        }
    }
}
