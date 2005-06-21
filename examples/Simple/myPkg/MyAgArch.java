package myPkg;

import jason.architecture.CentralisedAgArch;

import java.util.List;

/** example of agent architecture's functions overriding */
public class MyAgArch extends CentralisedAgArch {

    public List perceive() {
	// change lists "percepts" and "negPercepts" to
	// simulate faulty percepction, for example
        System.out.println("[custom ag arch] Getting percepts!");
        return super.perceive();
    }
    
}
