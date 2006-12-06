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
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
  <p>Internal action: <b><code>.at</code></b>.
  
  <p>Description: add an event at some time in future. This command is 
  based on the unix at command, although not fully implemented yet.
  
  <p>Parameters:<ul>
  <li>+ arg[0] (string): the time for the event to be generated.<br/>

      The syntax of this string in the current implementation has
      the following format:<br>
      <blockquote> 
        <code>now + &lt;number> [&lt;time_unit>]</code>
      </blockquote > where &lt;time_unit> can be
      "s" or "second(s)",  "m" or "minute(s)", "h" or "hour(s)", 
      "d" or "day(s)".
      The default &lt;time_unit> is miliseconds.<br/><br/>

  <li>+ arg[1] (string): the event. The string of the event will be
      parsed as a trigger event. (see Jason syntax)
  </ul>
  
  <p>Examples:<ul>
  <li> <code>.at("now +3 minutes", "+!g")</code>: generate the event <code>+!g</code> 
       at 1 minute from now.
  <li> <code>.at("now +1 m", "+!g")</code>
  <li> <code>.at("now +2 h", "+!g")</code>
  </ul>

 */
public class at extends DefaultInternalAction {
     
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
	        StringTerm time  = (StringTerm)args[0].clone();
	        StringTerm sevent = (StringTerm)args[1].clone();
	        un.apply(time);
            un.apply(sevent);
			Trigger te = Trigger.parseTrigger(sevent.getString());
			String  stime = time.getString();

			// parse time
			long deadline = -1;

			// if it starts with now
			if (stime.startsWith("now")) {
				// it is something like "now +3 minutes"
				stime = stime.substring(3).trim();
				// get time amount
				if (stime.startsWith("+")) {
					stime = stime.substring(1).trim();
					int pos = stime.indexOf(" ");
					if (pos > 0) {
						deadline = Integer.parseInt(stime.substring(0,pos));
						// get the time unit
						stime = stime.substring(pos).trim();
						if (stime.equals("s") || stime.startsWith("second")) {
							deadline *= 1000;
						}
						if (stime.equals("m") || stime.startsWith("minute")) {
							deadline *= 1000 * 60;
						}
						if (stime.equals("h") || stime.startsWith("hour")) {
							deadline *= 1000 * 60 * 60;
						}
						if (stime.equals("d") || stime.startsWith("day")) {
							deadline *= 1000 * 60 * 60 * 24;
						}
					}
				}
						
			} else {
                throw new JasonException("The time parameter ('"+stime+"') of the internal action 'at' is not implemented!");            	
            }

			if (deadline == -1) {
                throw new JasonException("The time parameter ('"+time+"') of the internal action 'at' is not correctly parsed!");            	
			}

			new CheckDeadline(deadline, te, ts.getC()).start(); 

			return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'at' has not received two arguments");
        } 
    }
	
	class CheckDeadline extends Thread {
        
        private long deadline = 0;
        private Event event;
        private Circumstance c;
        
        public CheckDeadline(long d, Trigger te, Circumstance c) {
            this.deadline = d;
            this.event = new Event(te, Intention.EmptyInt);
            this.c = c;
        }
        
        public void run() {
            try {
                sleep(deadline);
				// add event in C.Events
				c.addEvent(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}