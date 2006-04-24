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
//   Revision 1.13  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.12  2006/01/03 00:17:05  jomifred
//   change in =.. (using two lists, list of terms and list of annots)
//
//   Revision 1.11  2006/01/02 14:50:40  jomifred
//   change in =.. (annots is the last arg)
//
//   Revision 1.10  2005/12/31 16:29:58  jomifred
//   add operator =..
//
//   Revision 1.9  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.7  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.JasonException;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A Literal is a Pred with strong negation (~).
 */
public class Literal extends Pred implements Cloneable {

	public static final boolean   LPos       = true;
    public static final boolean   LNeg       = false;
    public static final Literal   LTrue      = new Literal(LPos, new Pred("true"));
    public static final Literal   LFalse     = new Literal(LPos, new Pred("false"));

    static private Logger logger = Logger.getLogger(Literal.class.getName());

	boolean type = LPos;

	public Literal() {
	}

	/** if pos == true, the literal is positive, else it is negative */
	public Literal(boolean pos, Pred p) {
		super(p);
		type = pos;
	}

	public Literal(Literal l) {
		super((Pred) l);
		type = l.type;
	}
	
	public Literal(Term t) {
		super(t);
		type = LPos;
	}

	public static Literal parseLiteral(String sLiteral) {
		as2j parser = new as2j(new StringReader(sLiteral));
		try {
			return parser.l();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing literal " + sLiteral,e);
			return null;
		}
	}

	/** copy all attributes from literal <i>l</i> */
	/*
	public void set(Literal l) {
		super.set((Pred)l);
		type = l.type;
	}
	*/
		
	public boolean isInternalAction() {
		return (getFunctor().indexOf('.') >= 0);
	}
	
	public boolean isLiteral() {
		return true;
	}

	public boolean negated() {
		return (type == LNeg);
	}

	public boolean equals(Object o) {
		try {
			Literal l = (Literal) o;
			return (type == l.type && super.equals(l));
		} catch (Exception e) {
			return super.equals(o);
		}
	}

	public Object clone() {
		return new Literal(type, (Pred)this);
	}

	
	/** return [~] super.getFucntorArity */
	public String getFunctorArity() {
		if (functorArityBak == null) {
			functorArityBak = (type == LPos) ? super.getFunctorArity() : "~" + super.getFunctorArity(); 
		}
		return functorArityBak;
	}
	
	public int hashCode() {
		return getFunctorArity().hashCode();
	}

	/** returns this literal as a list [<functor>, <list of terms>, <list of annots>] */
	public ListTerm getAsListOfTerms() {
		ListTerm l = new ListTermImpl();
		l.add(new Literal(type, new Pred(getFunctor())));
		ListTerm lt = new ListTermImpl();
		if (getTerms() != null) {
			lt.addAll(getTerms());
		}
		l.add(lt);
		if (getAnnots() != null && !getAnnots().isEmpty()) {
			l.add(getAnnots().clone());
		} else {
			l.add(new ListTermImpl());
		}
		return l;
	}

	/** creates a literal from a list [<functor>, <list of terms>, <list of annots>] */
	public static Literal newFromListOfTerms(ListTerm lt) throws JasonException {
		try {
			Iterator i = lt.iterator();
			Literal l = (Literal)((Literal)i.next()).clone();
			if (i.hasNext()) {
				l.addTerms((ListTerm)((ListTerm)i.next()).clone());
			}
			if (i.hasNext()) {
				l.setAnnots((ListTerm)((ListTerm)i.next()).clone());
			}
			return l;
		} catch (Exception e) {
			throw new JasonException("Error creating literal from "+lt);
		}
	}
	
	public String toString() {
		if (type == LPos)
			return super.toString();
		else
			return "~" + super.toString();
	}

}