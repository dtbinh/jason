// Internal action code for project ObjectTerm

package date;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.Calendar;
import java.util.logging.Logger;

public class add_days extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("ObjectTerm."+add_days.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            // ObjectTerms are always represented by VarTerms
            // get the value of the Var
            ObjectTerm ot = (ObjectTerm)((VarTerm)args[0]).getValue();
            
            // get the object wrapped by ot
            Calendar c = (Calendar)ot.getObject();
            
            // clone (so to not change the original object)
            c = (Calendar)c.clone();
            
            // do the changes
            c.add(Calendar.DAY_OF_YEAR, (int)((NumberTerm)args[1]).solve());
            
            // unify the result
            return un.unifies(args[2], new ObjectTerm(c));
        } catch (Exception e) {
            logger.warning("Error in internal action 'date.add_days'! "+e);
        }
        return false;
    }
    
}
