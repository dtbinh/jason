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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.15  2006/02/24 20:08:31  jomifred
//   no message
//
//   Revision 1.14  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.13  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.12  2005/12/22 00:03:30  jomifred
//   ListTerm is now an interface implemented by ListTermImpl
//
//   Revision 1.11  2005/12/20 19:52:05  jomifred
//   no message
//
//   Revision 1.10  2005/09/26 11:45:46  jomifred
//   fix bug with source add/remove
//
//   Revision 1.9  2005/08/17 18:19:17  jomifred
//   change AS grammar (unify lt and la implementation)
//
//   Revision 1.8  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/** 
 * A Pred is a Term with annotations, eg a(1)[an1,an2].
 */
public class Pred extends Term implements Cloneable, Comparable, Serializable {

	private ListTerm annots;

	static private Logger logger = Logger.getLogger(Pred.class.getName());

	public Pred() {
	}

	public Pred(String ps) {
		super(ps);
	}

	public Pred(Term t) {
		super(t);
	}

	public Pred(Pred p) {
		this((Term)p);
		copyAnnot(p);
	}

	public static Pred parsePred(String sPred) {
		as2j parser = new as2j(new StringReader(sPred));
		try {
			return parser.at();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing predicate " + sPred,e);
			return null;
		}
	}


	/** copy all attributes of Pred <i>p</i> */
	/*
	public void set(Pred p) {
		super.set((Term)p);
		copyAnnot(p);
	}
	*/
	
	public boolean isPred() {
		return true;
	}
	
	public void setAnnots(ListTerm l) {
		annots = l;
	}

	public void addAnnot(Term t) {
		if (annots == null)
			annots = new ListTermImpl(); //new ArrayList();
		if (!annots.contains(t))
			annots.add(t);
	}
	
	public void addAnnots(List l) {
		Iterator i = l.iterator();
		while (i.hasNext()) {
			addAnnot( (Term)i.next());
		}
	}
	
	public void addAnnot(int index, Term t) {
		if (annots == null)
			annots = new ListTermImpl();
		if (!annots.contains(t))
			annots.add(index, t);
	}

	public void delAnnot(Term t) {
		if (annots != null)
			annots.remove(t);
	}

	public void clearAnnots() {
		if (annots != null)
			annots.clear();
	}
	
	public ListTerm getAnnots() {
		return annots;
	}

	public boolean hasAnnot(Term t) {
		if (annots == null)
			return false;
		return annots.contains(t);
	}

	/** returns true if the pred has at leat one annot */
	public boolean hasAnnot() {
		return annots != null && !annots.isEmpty();
	}
	
	/**
	 * Add a source annotation like "source(<i>agName</i>)". 
	 */
	public void addSource(Term agName) {
		if (agName != null) {
			Term ts = new Term("source");
			ts.addTerm(agName);
			addAnnot(ts);
		}
	}

	/** del "source(<i>agName</i>)" */
	public boolean delSource(Term agName) {
		if (annots != null) {
			Term ts = new Term("source");
			ts.addTerm(agName);
			return annots.remove(ts);
			/*
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source") && t.getTerm(0).equals(s)) {
					i.remove();
					return true;
				}
			}
			*/
		}
		return false;
	}
	
	/** 
	 * return the sources of this Pred as a new list.
	 * e.g.: from annots [source(a), source(b)],
	 * it returns [a,b] 
	 */
	public ListTerm getSources() {
		ListTerm ls = new ListTermImpl();
		if (annots != null) {
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source")) {
					ls.add( t.getTerm(0) );
				}
			}
		}
		return ls;
	}
	
	
	/** del all sources annotations */
	public void delSources() {
		if (annots != null) {
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source")) {
					i.remove();
				}
			}
		}
	}
	
	public boolean hasSource() {
		if (annots != null) {
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source")) {
					return true;
				}
			}
		}
		return false;		
	}

	
	/** returns true if this pred has a "source(<i>agName</i>)" */
	public boolean hasSource(Term agName) {
		if (annots != null) {
			Term ts = new Term("source");
			ts.addTerm(agName);
			return annots.contains(ts);
			/*
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.equals(s)) {
					return true;
				}
			}
			*/
		}
		return false;		
	}

	/** 
	 * "import" Annotations from another Predicate <i>p</i>.
	 *  p will only contain the annots actually imported (for Event)
	 */
	public void importAnnots(Pred p) {
		if (p.getAnnots() == null) {
			return;
		}
		if (annots == null && p.hasAnnot()) {
			annots = new ListTermImpl();
		}
		Iterator i = p.getAnnots().iterator();
		while (i.hasNext()) {
			Term t = (Term) i.next();
			// p will only contain the annots actually added (for Event)
			if (!annots.contains(t)) {
				annots.add(t.clone());
			} else {
				// Remove what is not new from p 
				i.remove();
			}
		}
	}

	/** removes all annots in this pred that are in <i>p</i>
	 *  p will only contain the annots actually deleted (for Event)
	 */
	public void delAnnot(Pred p) {
		if (p.getAnnots() == null) {
			return;
		}
		if (!hasAnnot()) {
			p.clearAnnots();
		} else {
			Iterator i = p.getAnnots().iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();				
				if (annots.contains(t)) {
					annots.remove(t);
				} else {
					i.remove();
				}
			}
		}
	}

	public void copyAnnot(Pred p) {
		if (p.annots != null) {
			annots = (ListTerm)p.getAnnots().clone();
		} else {
			annots = null;
		}
	}

	/** returns true if all this predicate annots are in p's annots */ 
	public boolean hasSubsetAnnot(Pred p) {
		if (annots == null)
			return true;
		if (annots != null && p.getAnnots() == null)
			return false;
		Iterator i = annots.iterator();
		while (i.hasNext()) {
			Term myAnnot = (Term)i.next();
			if (!p.getAnnots().contains(myAnnot)) {
				return false;
			}
		}
		return true;
	}

	/** 
	 * returns true if all this predicate annots are in p's annots
	 * (this version unifies the annot list)
	 */
	public boolean hasSubsetAnnot(Pred p, Unifier u) {
		if (annots == null)
			return true;
		if (annots != null && p.getAnnots() == null)
			return false;

		p = (Pred)p.clone(); // clone p to change its annots, the remaining annots will unify this annots Tail

		// if p annots has a Tail, p annots's Tail will receive this annots
		VarTerm pTail = null;
		try {
			pTail = (VarTerm)p.getAnnots().getTail();
		}  catch (Exception e) {}
		
		Iterator i = annots.iterator();
		while (i.hasNext()) {
			Term annot = (Term)i.next();
			
			// search annot in p's annots
			boolean ok = false;
			Iterator j = p.getAnnots().iterator();
			while (j.hasNext() && !ok) {
				Term pAnnot = (Term)j.next();
				if (u.unifies(annot, pAnnot)) {
					ok = true;
					j.remove();
				}
			}
			
			// if p has a tail, add annot in p's tail
			if (!ok && pTail != null) {
				ListTerm pTailAnnots = (ListTerm)u.get(pTail.getFunctor());
				if (pTailAnnots == null) {
					pTailAnnots = new ListTermImpl();
					u.unifies(pTail, (Term)pTailAnnots);
					pTailAnnots = (ListTerm)u.get(pTail.getFunctor());
				}
				pTailAnnots.add(annot);
				ok = true;
			}
			if (!ok)
				return false;
		}
		
		// if this Pred has a Tail, unify it with p remaining annots
		try {
			VarTerm tail = (VarTerm)annots.getTail();
			if (tail != null) {
				//System.out.println("tail="+tail+"/"+p.getAnnots());
				u.unifies(tail, (Term)p.getAnnots());
			}
		}  catch (Exception e) {}
		
		return true;
	}
	
	
	public boolean equals(Object o) {
		if (!super.equals(o))
			return false;
		try {
			Pred p = (Pred) o;
			if (this.hasSubsetAnnot(p) && p.hasSubsetAnnot(this))
				return true;
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	public boolean equalsAsTerm(Object p) {
		return super.equals((Term) p);
	}

	public int compareTo(Object p) {
		int c;
		c = super.compareTo(p);
		if (c != 0)
			return c;
		if (annots.size() < ((Pred) p).getAnnots().size())
			return -1;
		if (annots.size() > ((Pred) p).getAnnots().size())
			return 1;
		return 0;
	}

	public Object clone() {
		return new Pred(this);
	}


	public String toString() {
		String s;
		s = super.toString();
		if (annots != null && !annots.isEmpty()) {
			s += annots.toString();
		}
		return s;
	}
}