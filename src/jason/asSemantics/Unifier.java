//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.15  2005/12/22 00:03:30  jomifred
//   ListTerm is now an interface implemented by ListTermImpl
//
//   Revision 1.14  2005/12/20 19:52:05  jomifred
//   no message
//
//   Revision 1.13  2005/08/16 21:03:42  jomifred
//   add some comments on TODOs
//
//   Revision 1.12  2005/08/12 22:18:37  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.DefaultLiteral;
import jason.asSyntax.ExprTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Unifier implements Cloneable {
    
	static Logger logger = Logger.getLogger(Unifier.class);

	private HashMap function = new HashMap();
    
    // TODO IMPORTANT: remove unnecessary tail symbols
    // and accompanying extra list when grounding variables
    // that are internal to a list
	// RAFA: is is already done?

    // TODO JOMI: try to not clone and apply before unifing, since this method is
    // a bottleneck (clone/apply consumes memory/cpu). Initial proposal: use
    // "t1.equals(t2, this)", ie, the equals will consider the unifier to check
    // equality.

    public void apply(Term t) {
    	if (t.isExpr()) {
    		ExprTerm et = (ExprTerm)t;
    		// apply values to expression variables
    		apply( (Term)et.getLHS()); // TODO: remove this cast when NumberTerm is sub-term interface
    		if (!et.isUnary()) {
    			apply( (Term)et.getRHS());// TODO: remove this cast when NumberTerm is sub-term interface
    		}
    		et.setValue(new NumberTermImpl(et.solve()));
    	} else if (t.isVar()) {
			VarTerm vt = (VarTerm) t;
			if (! vt.hasValue()) { 
				Term vl = get(vt.getFunctor());
				//System.out.println("appling="+t+"="+vl+" un="+this);
				if (vl != null) {
					vt.setValue(vl);
					apply(vt); // in case t has var args
				}
			}
			return;
		}
		for (int i = 0; i < t.getTermsSize(); i++) {
			apply(t.getTerm(i));
		}
    }

    public void apply(Pred p) {
    	apply((Term) p);
		if (p.getAnnots() != null) {
			for (int i = 0; i < p.getAnnots().size(); i++) {
				apply((Term) p.getAnnots().get(i));
			}
		}
    }

    
	/**
	 * gets the value for a Var, if it is unified with another var, gets this
	 * other's value
	 */
    public Term get(String var) {
		if (var == null) return null;
		
		Term vl = (Term)function.get(var);
		if (vl == null) return null;
		
		// if vl is also a var, get this var value
		try {
			//System.out.println("*"+var+"*"+vl+" - "+this);
			VarTerm vt = (VarTerm)vl;
			Term vtvl = vt.getValue();
			if (vtvl != null) { // the variable has value, is ground
				return vtvl;
			} else { // the variable is not ground, but could be unified
				vtvl = get( vt.getFunctor() );
				if (vtvl != null) {
					return vtvl;
				}
			}
	
			return null; // no value!
		} catch (StackOverflowError e) {
			logger.error("Stack overflow in unifier.get!\n\t"+this,e);
			return null;
		} catch (ClassCastException e) {
			return vl;
		}
    }
    
    public void compose(Term t, Unifier u) {
        if (t.isVar()) {
            if (u.function.containsKey(t.getFunctor())) {
            	// Note we are losing any previous maping of that variable,
            	// presumably this was either left unchanged or updated
            	// by the plan execution
                function.put(t.getFunctor(),u.function.get(t.getFunctor()));
            } // else {
                // Uninstantiated variables remain when the plan for a
            	// goal has finished. Normally this shouldn't happend, but
                // nothing necessarily wrong with it. If it was a programming
                // mistake, an error will eventually occur (e.g., in an action
            	// with an uninstantiated variable).
            // }
            return;
        }
        if (t.getTerms()==null)
            return;
        for (int i=0; i < t.getTermsSize(); i++) {
            compose(t.getTerm(i), u);
        }
    }
    
    private boolean unifiesNoClone(Term t1g, Term t2g) {
		List t1gl = t1g.getTerms();
		List t2gl = t2g.getTerms();

		/*
		// check if an expression needs solving, before anything else
		// version with expression unification (X+3 = (2+1)+3) unifies X with (2+1) 
		try {
			ExprTerm t1ge = (ExprTerm)t1g;
			try {
				ExprTerm t2ge = (ExprTerm)t2g;
			} catch (ClassCastException e) {
				// t1 is expr but t2 is not
				double t1gd = t1ge.solve();
				String t1gs = Double.toString(t1gd);
				if (t1gs.endsWith(".0")) {
					t1g = new Term(Long.toString(Math.round(t1gd)));
				}
				else {
					t1g = new Term(t1gs);
				}
			}
		} catch (ClassCastException e) {
			try {
				ExprTerm t2ge = (ExprTerm)t2g;
				// t1 is not expr but t2 is
				double t2gd = t2ge.solve();
				String t2gs = Double.toString(t2gd);
				if (t2gs.endsWith(".0")) {
					t2g = new Term(Long.toString(Math.round(t2gd)));
				}
				else {
					t2g = new Term(t2gs);
				}
			} catch (ClassCastException e2) {
			}
		}
		*/
		
        // identical variables or constants
		if (t1g.equals(t2g)) {
			//System.out.println("Equals." + t1 + "=" + t2 + "...." + this);
			return true;
		}
        
        // if two atoms or structures
		if (!t1g.isVar() && !t2g.isVar()) {
			// different funcSymb in atoms or structures
        	if (t1g.getFunctor() != null && !t1g.getFunctor().equals(t2g.getFunctor())) {
				return false;
        	}
            
			// different arities
        	if ( (t1gl==null && t2gl!=null)   ||
					(t1gl!=null && t2gl==null) ) {
				return false;
			}
			if (t1g.getTermsSize() != t2g.getTermsSize()) {
				return false;
			}
        }
		
        // t1 is var that doesn't occur in t2
		if (t1g.isVar() && !t2g.hasVar(t1g)) {
			
			// if t1g is unified with another var, also unify another
			try {
				VarTerm t1gvl = (VarTerm)function.get(t1g.getFunctor());
				if (t1gvl != null) {
					unifies(t1gvl,t2g);
				}
			} catch (Exception e) {}
			
			function.put(t1g.getFunctor(), t2g);
			return true;
		}

		// t2 is var that doesn't occur in t1
		if (t2g.isVar() && !t1g.hasVar(t2g)) {
			// if t2g is unified with another var, also unify another
			try {
				VarTerm t2gvl = (VarTerm)function.get(t2g.getFunctor());
				if (t2gvl != null) {
					unifies(t2gvl,t1g);
				}
			} catch (Exception e) {}
			
			function.put(t2g.getFunctor(), t1g);
			//System.out.println("Unified." + t1 + "=" + t2);
			return true;
		}
		
        // both are structures, same funcSymb, same arity
		if (!t1g.isList() && !t2g.isList()) { // lists have always terms == null
            if (t1gl == null &&  t2gl == null) {
                return true;
            }
		} 
					    
		for (int i=0; i < t1g.getTermsSize(); i++) {
            if (!unifies(t1g.getTerm(i),t2g.getTerm(i)))
                return false;
		}
		return true;
    }

    public boolean unifies(Term t1, Term t2) {
        Term t1g = (Term)t1.clone();
        Term t2g = (Term)t2.clone();
        apply(t1g);
        apply(t2g);
        //System.out.println("TermUn: "+t1+"="+t2+" : "+t1g+"="+t2g);
        return unifiesNoClone(t1g, t2g);
    }
  
   	public boolean unifies(Pred p1, Pred p2) {
   		Pred np1 = (Pred)p1.clone();
   		Pred np2 = (Pred)p2.clone();
   		apply(np1);
   		apply(np2);
        //System.out.println("PredUn: "+p1+"="+p2+" : "+np1+"="+np2);
        return unifiesNoClone((Pred)np1, (Pred)np2); 
    }
   	private boolean unifiesNoClone(Pred np1, Pred np2) {
        // unification with annotation:
        // terms unify and annotations are subset
        if (!np1.isVar() && !np2.isVar() && !np1.hasSubsetAnnot(np2, this)) {
        	return false;
        }
        return unifiesNoClone((Term)np1, (Term)np2); 
    }
    
    public boolean unifies(Literal l1, Literal l2) {
    	Literal nl1 = (Literal)l1.clone();
   		Literal nl2 = (Literal)l2.clone();
   		apply(nl1);
   		apply(nl2);
   		//System.out.println("LiteralUn: "+l1+"="+l2+" : "+nl1+"="+nl2);
        if (!nl1.isVar() && !nl2.isVar() && nl1.negated() != nl2.negated()) {
        	return false;
        }
        return unifiesNoClone((Pred)nl1,(Pred)nl2);
    }
    
    public boolean unifies(DefaultLiteral d1, DefaultLiteral d2) {
        return d1.isDefaultNegated()==d2.isDefaultNegated() && unifies((Literal)d1.getLiteral(),(Literal)d2.getLiteral());
    }
    
    public boolean unifies(Trigger te1, Trigger te2) {
        return te1.sameType(te2) && unifies((Literal)te1,(Literal)te2);
    }
    
    public void clear() {
        function.clear();
    }
    
    public String toString() {
        return function.toString();
    }
    
    public Object clone() {
        try {
        	Unifier newUn = new Unifier();
        	newUn.function = (HashMap)this.function.clone();
            return newUn;
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

    /** get as XML */
	public Element getAsDOM(Document document) {
		Element u = (Element) document.createElement("unifier");
		u.appendChild(document.createTextNode(this.toString()));
		return u;
	}
    
}
