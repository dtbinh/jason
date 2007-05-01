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

import java.io.Serializable;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for all terms.
 */
public abstract class DefaultTerm implements Term, Serializable {

	private static final long serialVersionUID = 1L;

    protected Integer hashCodeCache = null;
	protected int           srcLine = -1; // the line this literal appears in the source

    static private Logger logger = Logger.getLogger(Term.class.getName());
    
    public static Term parse(String sTerm) {
        as2j parser = new as2j(new StringReader(sTerm));
        try {
            return parser.term();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing term " + sTerm,e);
            return null;
        }
    }
    
    public int hashCode() {
        if (hashCodeCache == null) {
            hashCodeCache = calcHashCode();
        }
        return hashCodeCache;
    }
    
    abstract protected int calcHashCode();
    
    /** remove the valued cached for hashCode */
    public void resetHashCodeCache() {
        hashCodeCache = null;
    }
    
    public int compareTo(Term t) {
        return this.toString().compareTo(t.toString());
    }
        
    public boolean isVar() {
        return false;
    }
    public boolean isLiteral() {
        return false;
    }
    public boolean isRule() {
        return false;
    }
    public boolean isList() {
        return false;
    }
    public boolean isString() {
        return false;
    }
    public boolean isInternalAction() {
        return false;
    }
    public boolean isArithExpr() {
        return false;
    }
    public boolean isNumeric() {
        return false;
    }
    public boolean isPred() {
        return false;
    }
    
    public boolean isStructure() {
        return false;
    }

	public boolean isAtom() {
		return false;
	}
    
    public boolean isGround() {
        return true;
    }
    
    public boolean hasVar(Term t) {
        return false;
    }

    abstract public Object clone();
    
    public boolean apply(Unifier u) {
    	return false;
    }

    public void setSrcLine(int i) {
		srcLine = i;
	}
    public int getSrcLine() {
    	return srcLine;
    }


}
