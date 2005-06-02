package myPkg;

import jason.asSemantics.*;
import java.util.*;

/** example of agent function overriding */
public class MyAgClass extends Agent {

    public Event selectEvent(List evList) {
        System.out.println("[Custom Agent class] Selecting an event from "+evList);
        return((Event)evList.remove(0));
    }

}
