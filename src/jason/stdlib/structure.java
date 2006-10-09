package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.structure</code></b>.

<p>Description: check whether the argument is a structure, e.g.: "p", "p(1)", "[a,b]". 
Numbers, strings, and unground variables are not structures.

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.structure(b(10))</code>: success.
<li> <code>.structure(b)</code>: success.
<li> <code>.structure(10)</code>: fail.
<li> <code>.structure("home page")</code>: fail.
<li> <code>.structure(X)</code>: fail if X is free and success if X is bind with a structure.
<li> <code>.structure(a(X))</code>: success.
<li> <code>.structure([a,b,c])</code>: success.
<li> <code>.structure([a,b,c(X)])</code>: success.
</ul>

*/
public class structure extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isStructure();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'structure' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'structure': " + e);
        }
    }
}
