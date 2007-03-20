package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.List;

/**

  <p>Internal action: <b><code>.member(<i>T</i>,<i>L</i>)</code></b>.
  
  <p>Description: checks if some term <i>T</i> is in a list <i>L</i>. If
  <i>T</i> is a free variable, this internal action backtracks all
  possible values for <i>T</i>.

  <p>Parameters:<ul>
  
  <li>+/- member (term): the term to be checked.</li>
  <li>+ list (list): the list where the term should be in.</li>
  
  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.member(c,[a,b,c])</code>: true.</li>
  <li> <code>.member(3,[a,b,c])</code>: false.</li>
  <li> <code>.member(X,[a,b,c])</code>: unifies X with any member of the list.</li>

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.length
  @see jason.stdlib.sort
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min

*/
public class member extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
			args[0].apply(un);
            Term member = args[0];

			args[1].apply(un);
            if (!args[1].isList()) {
                throw new JasonException("The second parameter ('" + args[1] + "') to the internal action 'member' is not a list!");
            }
            ListTerm lt = (ListTerm)args[1];

            List<Unifier> answers = new ArrayList<Unifier>();
            Unifier newUn = (Unifier)un.clone(); // clone un so as not to change it
            for (Term t: lt) {
                if (newUn.unifies(member, t)) {
                    // add this unification to the  answers
                    answers.add(newUn);
                    newUn = (Unifier)un.clone(); // creates a new clone of un
                }
            }                
            return answers.iterator();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'member' has not received two arguments.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'member': " + e, e);
        }
    }
}
