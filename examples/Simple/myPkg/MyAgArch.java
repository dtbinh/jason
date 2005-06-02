package myPkg;

import jason.architecture.*;

/** example of agent architecture's functions overriding */
public class MyAgArch extends CentralisedAgArch {

    public void perceive() {
	// change lists "percepts" and "negPercepts" to
	// simulate faulty percepction, for example
        System.out.println("[custom ag arch] Getting percepts!");
        super.perceive();
    }
    
}
