
package agent;

import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Unifier;
import jason.asSyntax.Trigger;

import java.util.Iterator;
import java.util.Queue;

/**
 * change the default select event function to prefer +cow(_,_,_) events.
 * 
 * @author Jomi
 */
public class SelectEvent extends Agent {

	private Trigger cow  = Trigger.parseTrigger("+cow(_,_,_)");
	private Unifier un   = new Unifier();
	
    public Event selectEvent(Queue<Event> events) {
    	Iterator<Event> ie = events.iterator();
    	while (ie.hasNext()) {
    		un.clear();
    		Event e = ie.next();
        	if (un.unifies(cow, e.getTrigger())) {
    			ie.remove();
    			return e;
    		}
    	}
        return super.selectEvent(events);
    }
}
