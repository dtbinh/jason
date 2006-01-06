package myPkg;

import jason.asSemantics.Agent;
import jason.asSemantics.Event;

import java.util.List;
import java.util.logging.Logger;

/** example of agent function overriding */
public class MyAgClass extends Agent {
    Logger logger = Logger.getLogger(MyAgClass.class.getName());

    public Event selectEvent(List evList) {
        logger.info("Selecting an event from "+evList);
        return((Event)evList.remove(0));
    }
}
