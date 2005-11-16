package myPkg;

import jason.architecture.*;

import java.util.List;

import org.apache.log4j.Logger;


/** example of agent architecture's functions overriding */
public class MyAgArch extends AgArch {

	Logger logger = Logger.getLogger(MyAgArch.class.getName());

    public List perceive() {
    	// change lists "percepts" and "negPercepts" to
    	// simulate faulty percepction, for example
    	logger.info("Getting percepts!");
        return super.perceive();
    }
    
}
