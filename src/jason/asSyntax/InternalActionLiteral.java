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

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A particular type of literal used to represent internal actions (has a "." in the functor).
 */
public class InternalActionLiteral extends Literal {

	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(InternalActionLiteral.class.getName());

	/** creates a positive literal */
	public InternalActionLiteral(String functor) {
		super(functor);
	}

	public InternalActionLiteral(InternalActionLiteral l) {
		super((Literal) l);
	}

	public InternalActionLiteral(Pred p) {
        super(true,p);
    }

    @Override
	public boolean isInternalAction() {
		return true;
	}
	
    @SuppressWarnings("unchecked")
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        try {
        	// clone terms array
            Term[] clone = getTermsArray();
            for (int i=0; i<clone.length; i++) {
                clone[i] = (Term)clone[i].clone();
                clone[i].apply(un);
            }

        	// calls execute
            Object oresult = ag.getIA(this).execute(ag.getTS(), un, clone);
            if (oresult instanceof Boolean && (Boolean)oresult) {
                return LogExpr.createUnifIterator(un);
            } else if (oresult instanceof Iterator) {
                return ((Iterator<Unifier>)oresult);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, getErrorMsg() + ": " +	e.getMessage(), e);
        }
        return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    public String getErrorMsg() {
    	String line = (getSrcLine() >= 0 ? ":"+getSrcLine() : "");
        return "Error in internal action '"+this+"' ("+ getSrc() + line + ")";    	
    }
    
	public Object clone() {
        InternalActionLiteral c = new InternalActionLiteral(this);
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        c.hashCodeCache = this.hashCodeCache;
        return c;
	}

    
    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        u.setAttribute("ia", isInternalAction()+"");
        return u;
    }    
}
