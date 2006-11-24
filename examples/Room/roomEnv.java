import java.util.*;
import jason.*;
import jason.asSyntax.*;
import jason.environment.*;

public class roomEnv extends Environment {
    
    Literal ld  = Literal.parseLiteral("locked(door)");
    Literal nld = Literal.parseLiteral("~locked(door)");
    boolean doorLocked = true;
    
    @Override
	public void init(String[] args) {
        // initial percepts
        addPercept(ld);
    }
    
    /**
     * Implementation of the agent's basic actions
     */
    @Override
    public boolean executeAction(String ag, Structure act) {
	
	    clearPercepts();
        
	    if (act.getFunctor().equals("lock"))
		    doorLocked = true;

	    if (act.getFunctor().equals("unlock"))
		    doorLocked = false;
	
	    // update percepts given state of the environment
	    if (doorLocked) {
		    addPercept(ld);
	    }
	    else {
		    addPercept(nld);
	    }
	    return true;
    }
}

