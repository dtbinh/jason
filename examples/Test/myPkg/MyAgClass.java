package myPkg;

import jason.asSemantics.*;
import java.util.*;

import org.apache.log4j.Logger;

/** example of agent function overriding */
public class MyAgClass extends Agent {
	Logger logger = Logger.getLogger(MyAgClass.class.getName());

    public Event selectEvent(List evList) {
        logger.info("Selecting an event from "+evList);
        return((Event)evList.remove(0));
    }
}
