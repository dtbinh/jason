package jmoise;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

@Deprecated
public class start_scheme extends MoiseBaseIA {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        System.out.println("**** Use create_scheme instead of start_scheme ****");
    	return super.execute(ts,un,args);
    }
}
