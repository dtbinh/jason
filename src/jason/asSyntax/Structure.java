// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a structure: a functor with <i>n</i> arguments, 
 * e.g.: val(10,x(3)). 
 * <i>n</i> can be 0, so this class also represents atoms.
 */
public class Structure extends DefaultTerm {

	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Structure.class.getName());

	private final String functor; // immutable field
    private List<Term> terms;

    protected PredicateIndicator predicateIndicatorCache = null; // to not compute it all the time (is is called many many times)
    
    public Structure(String functor) {
        //if (functor != null && Character.isUpperCase(functor.charAt(0))) {
        //    logger.warning("Are you sure you want to create a structure that begins with uppercase ("+functor+")? Should it be a VarTerm instead?");
        //}
        //if (functor != null && functor.charAt(0) == '~') {
        //    logger.warning("A functor should not start with ~ ("+functor+")!");
        //}
        //this.functor = (functor == null ? null : functor.intern()); // it does not improve performance in test i did!
        this.functor = functor;
        this.terms = new ArrayList<Term>(5);
    }
    
    public Structure(Structure t) {
        functor = t.getFunctor();
        terms   = t.getDeepCopyOfTerms();
        setSrc(t);
    }

    /** to be used by list term and atom to not create the array list for terms */
    protected Structure(String functor, int termsSize) {
        //this.functor = (functor == null ? null : functor.intern()); 
        this.functor = functor;
        if (termsSize > 0)
        	terms = new ArrayList<Term>(termsSize);
    }

    public static Structure parse(String sTerm) {
        as2j parser = new as2j(new StringReader(sTerm));
        try {
            return (Structure)parser.term();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing structure " + sTerm,e);
            return null;
        }
    }
    
    public String getFunctor() {
        return functor;
    }

    /** returns functor symbol "/" arity */
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null) {
            predicateIndicatorCache = new PredicateIndicator(getFunctor(),getArity());
        }
        return predicateIndicatorCache;
    }

    protected int calcHashCode() {
        final int PRIME = 7;
        int result = 1;
        if (functor != null) result = PRIME * result + functor.hashCode();
        final int ts = getArity();
        result = PRIME * result + ts;
        for (int i=0; i<ts; i++) {
        	result = PRIME * result + getTerm(i).hashCode();
        }
        return result;
    }

    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;

        if (t instanceof Structure) {
            Structure tAsStruct = (Structure)t;

            // if t is a VarTerm, uses var's equals
            if (tAsStruct.isVar()) return ((VarTerm)t).equals(this);

            if (getFunctor() == null && tAsStruct.getFunctor() != null) return false;
            if (getFunctor() != null && !getFunctor().equals(tAsStruct.getFunctor())) return false;
            //if (getFunctor() != tAsStruct.getFunctor()) return false;

            final int ts = getArity(); 
            if (ts != tAsStruct.getArity()) return false;

            for (int i=0; i<ts; i++) {
                if (!getTerm(i).equals(tAsStruct.getTerm(i))) return false;
            }
            return true;
        } 
        return false;
    }


    public int compareTo(Term t) {
        if (! t.isStructure()) return super.compareTo(t);

		// this is a list and the other not
		if (isList() && !t.isList()) return 1;

		// this is not a list and the other is
		if (!isList() && t.isList()) return -1;

		// both are lists, check the size
		if (isList() && t.isList()) {
			ListTerm l1 = (ListTerm)this;
			ListTerm l2 = (ListTerm)t;
			final int l1s = l1.size();
			final int l2s = l2.size();
			if (l1s > l2s) return 1;
			if (l2s > l1s) return -1;
		}

		// both are list with same size,
		// or none are list
        Structure tAsStruct = (Structure)t;

        final int ma = getArity();
        final int oa = tAsStruct.getArity();
        if (ma < oa) return -1;
        if (ma > oa) return 1;

        int c;
        if (getFunctor() != null && tAsStruct.getFunctor() != null) {
            c = getFunctor().compareTo(tAsStruct.getFunctor());
            if (c != 0) return c;
        }

        for (int i=0; i<ma && i<oa; i++) {
            c = getTerm(i).compareTo(tAsStruct.getTerm(i));
            if (c != 0) return c;
        }

        return 0;
    }

    public boolean apply(Unifier u) {
    	boolean r = false;
        // do not use iterator! (see ListTermImpl class)
        final int tss = getArity();
        for (int i = 0; i < tss; i++) {
        	boolean tr = getTerm(i).apply(u); 
            r = r || tr;
        }
        resetHashCodeCache();
        return r;
    }

    /** make a deep copy of the terms */
    public Object clone() {
        Structure c = new Structure(this);
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        c.hashCodeCache           = this.hashCodeCache;
        return c;
    }

    public void addTerm(Term t) {
    	if (t == null) return;
        terms.add(t);
        predicateIndicatorCache = null;
        resetHashCodeCache();
    }
    
    public void addTerms(List<Term> l) {
        for (Term t: l) {
            addTerm(t);
        }
    }
 
    public void setTerms(List<Term> l) {
        terms = l;
        predicateIndicatorCache = null;
        resetHashCodeCache();
    }
    
    public void setTerm(int i, Term t) {
        terms.set(i,t);
        resetHashCodeCache();
    }
     
    /** returns the i-th term (first term is 0) */
    public Term getTerm(int i) {
        if (terms.size() > i) {
            return terms.get(i);
        } else {
            return null;
        }
    }

    public int getArity() {
        return terms.size();
    }
    
    /** @deprecated use getArity */
    public int getTermsSize() {
        return getArity();
    }

    public List<Term> getTerms() {
        return terms;
    }
    
    public boolean hasTerm() {
    	return getArity() > 0; // should use getArity to work for list/atom
    }
    
    private static final Term[] emptyTermArray = new Term[0]; // just to have a type for toArray in the getTermsArray method
    
    public Term[] getTermsArray() {
        return terms.toArray(emptyTermArray);
    }

    @Override
    public boolean isStructure() {
        return true;
    }
    
	@Override
	public boolean isAtom() {
		return !hasTerm() && !isList();
	}

    public boolean isGround() {
        final int size = getArity();
        for (int i=0; i<size; i++) {
            if (!getTerm(i).isGround()) {
                return false;
            }
        }
        return true;
    }

    /** Replaces all variables of the term for unnamed variables (_). */
    public void makeVarsAnnon() {
    	makeVarsAnnon(new HashMap<VarTerm,UnnamedVar>());
    }
    
    /**
     * Replaces all variables of the term for unnamed variables (_).
     * 
     * @param changes is the map of replacements
     */
    public void makeVarsAnnon(Map<VarTerm,UnnamedVar> changes) {
        final int size = getArity();
        for (int i=0; i<size; i++) {
            Term ti = getTerm(i);
            if (ti.isVar()) {
            	// replace ti to an unnamed var
            	UnnamedVar uv = changes.get(ti);
            	if (uv == null) {
            		uv = new UnnamedVar();
            		changes.put((VarTerm)ti, uv);
            	}
            	setTerm(i,uv);
            } else if (ti.isStructure()) {
                Structure tis = (Structure)ti;
                if (tis.hasTerm()) {
                    tis.makeVarsAnnon(changes);
                }
            }
        }
        resetHashCodeCache();
    }

    public void makeTermsAnnon() {
        final int size = getArity();
        for (int i=0; i<size; i++) {
            setTerm(i,new UnnamedVar());
        }
        resetHashCodeCache();
    }

    public boolean hasVar(Term t) {
        if (this.equals(t)) return true;
        final int size = getArity();
        for (int i=0; i<size; i++) {
            if (getTerm(i).hasVar(t)) {
                return true;
            }
        }
        return false;
    }

    protected List<Term> getDeepCopyOfTerms() {
        List<Term> l = new ArrayList<Term>(terms.size());
        for (Term ti: terms) {
            l.add((Term)ti.clone());
        }
        return l;
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (functor != null) s.append(functor);
        if (getArity() > 0) {
            s.append("(");
            Iterator<Term> i = terms.iterator();
            while (i.hasNext()) {
                s.append(i.next());
                if (i.hasNext()) s.append(",");
            }
            s.append(")");
        }
        return s.toString();
    }
   
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("structure");
        u.setAttribute("functor",getFunctor());
        //u.appendChild(document.createTextNode(toString()));
        if (hasTerm()) {
            Element ea = document.createElement("arguments");
            for (Term t: getTerms()) {
                ea.appendChild(t.getAsDOM(document));
            }
            u.appendChild(ea);
        }
        return u;
    }
}
