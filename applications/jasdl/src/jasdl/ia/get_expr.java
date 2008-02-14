/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package jasdl.ia;

import jasdl.ontology.Alias;
import jasdl.ontology.DefinedAlias;
import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.logging.Logger;

/**
 * @author Tom Klapiscak
 * 
 * Usage jasdl.ia.get_expr(class alias, defined by, ontology label, expr)
 *
 */
public class get_expr extends DefaultInternalAction {
    private Logger logger = Logger.getLogger("jasdl."+get_expr.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try { 	
        	if(!args[0].isAtom()){
        		throw new JasdlException("First parameter must be an atomic class alias");
        	}
        	String alias = args[0].toString();
        	
        	if(!args[1].isAtom()){
        		throw new JasdlException("Second parameter must be an atomic agent name");
        	}
        	String defined_by = args[1].toString();
        	
        	if(!args[2].isAtom()){
        		throw new JasdlException("Second parameter must be an atomic ontology label");
        	}
        	String label = args[2].toString();
        	
        	OntologyManager manager = OntologyManager.getOntologyManager(ts.getAg());
        	JasdlOntology ont = manager.getJasdlOntology(label);
        	
        	URI real = ont.toReal(new DefinedAlias(alias, defined_by, ""));
        	Alias _r = ont.toAlias(real);
        	
        	if(!(_r instanceof DefinedAlias)){
        		throw new JasdlException(alias+":"+defined_by+" does not refer to a runtime defined class");
        	}
        	
        	DefinedAlias r = (DefinedAlias)_r;
        	
        	un.unifies(args[3], new StringTermImpl(r.getExpr())); 	
        	
        	
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'jasdl.ia.define_class'! "+e);
            return false;
        }       
    }
    
    

    
}