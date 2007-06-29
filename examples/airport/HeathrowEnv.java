
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.util.HashMap;
import java.util.Map;

public class HeathrowEnv extends Environment {
   
	Literal[] initialLocations = { Literal.parseLiteral("location(t1,g1)"), 
                                   Literal.parseLiteral("location(t1,g2)"), 
                                   Literal.parseLiteral("location(t1,g3)")};
                                   
	Map agsLocation = new HashMap();
	
    @Override
    public void init(String[] args) {
        clearPercepts();
        
        // Add initial percepts below, for example:
        addPercept(Literal.parseLiteral("unattended_luggage(t1,g3,1)") );
        addPercept("mds1", initialLocations[1]);
    }
    
    /**
     * Implementation of the agents' basic actions
     */
    @Override
    public boolean executeAction(String ag, Structure action) {
        if (action.getFunctor().equals("place_bid")) {
            Integer x = new Integer(action.getTerm(2).toString());
            // TODO: implement it!
            //bid.put(ag,x);
        }
        
        return true;
    }
}
