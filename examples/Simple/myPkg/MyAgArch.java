package myPkg;

import jason.architecture.CentralisedAgArch;
import jason.architecture.SaciAgArch;

import java.util.List;

import org.apache.log4j.Logger;


/** example of agent architecture's functions overriding */
public class MyAgArch extends CentralisedAgArch {

	Logger logger = Logger.getLogger(MyAgArch.class.getName());

    public List perceive() {
    	// change lists "percepts" and "negPercepts" to
    	// simulate faulty percepction, for example
    	logger.info("Getting percepts!");
        return super.perceive();
    }
    
}
