package jmoise;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import moise.os.ss.Group;


/**

 Obtains a representation (as a term) of a group specification.
 
 Example:
 
 <pre>
 +play(Me,R,_)
   : .my_name(Me)
  <- jmoise.group_specification(wpgroup,S);
     S = group_specification(GrId,Roles,SubGroups,Properties);
     .member(role(R,Min,Max,Compat,Links),Roles);
     .print("I am starting playing ",R);
     .print(" -- cardinality of my role (Min,Max): (",Min,",",Max,")");
     .print(" -- roles compatible with mine: ", Compat);
     .print(" -- links of my role: ",Links);
     .print(" -- all specification of the group is ",S).
 </pre>
 
 @author hubner
*/
public class group_specification extends DefaultInternalAction  {

	@Override public int getMaxArgs() { return 2; }
	@Override public int getMinArgs() { return 2; }
	
	@Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args);
	    if (!args[0].isAtom() && !args[0].isString()) 
            throw JasonException.createWrongArgument(this,"first argument must be an atom or string identifying the group specification");
	}
	
	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
	    checkArguments(args);
	    
        String grId = "";
        if (args[0].isString())
            grId = ((StringTerm)args[0]).getString();
        else
            grId = args[0].toString();
        
        OrgAgent oag = (OrgAgent)ts.getUserAgArch();
        Group g = oag.getOE().getOS().getSS().getRootGrSpec().findSubGroup(grId);
        if (g == null)
            throw new JasonException("the group with id '"+grId+"' does not exists in the OS.");
        return un.unifies(args[1], ASSyntax.parseTerm(g.getAsProlog()));
    }	
}
