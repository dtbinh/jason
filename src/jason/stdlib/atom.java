package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.atom</code></b>.

<p>Description: checks whether the argument is an atom (a structure with arity
0), for example "p".  Numbers, strings, and free variables are not atoms.

<p>Parameters:<ul>
<li>+ arg (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.atom(b(10))</code>: false.
<li> <code>.atom(b)</code>: true.
<li> <code>.atom(~b)</code>: false.
<li> <code>.atom(10)</code>: false.
<li> <code>.atom("home page")</code>: false.
<li> <code>.atom(X)</code>: only true if X is bound to an atom.
<li> <code>.atom(a(X))</code>: false.
<li> <code>.atom(a[X])</code>: false.
<li> <code>.atom([a,b,c])</code>: false.
<li> <code>.atom([a,b,c(X)])</code>: false.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
public class atom extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            return args[0].isAtom();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'atom' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'atom': " + e, e);
        }
    }
}
