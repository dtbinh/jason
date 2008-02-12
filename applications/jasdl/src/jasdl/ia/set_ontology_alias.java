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

import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.logging.Logger;

public class set_ontology_alias extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("jasdl."+set_ontology_alias.class.getName());

    /**
     * Ontology alias setting. Usage:
     * jasdl.is.set_ontology_alias(uri, alias), where:
     * 	- old_alias: Atom representing alias of a known ontology for which to set the alias of
     *  - new_alias: Atom representing the ontology alias to apply to the ontology referred to by old_alias
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	if(args.length < 2){
        		throw new JasdlException("requires exactly 2 arguments");
        	} 
        	if(!args[0].isAtom()){
        		throw new JasdlException("first argument must be an Atom representing the alias of an existing OWL ontology");
        	}
        	if(!args[1].isGround()){
        		throw new JasdlException("second argument must be ground");
        	}
        	if(!args[1].isAtom()){
        		throw new JasdlException("second argument must be atomic");
        	}
        	
        	String old_alias = args[0].toString();
        	String new_alias = args[1].toString();
        	
        	OntologyManager manager = OntologyManager.getOntologyManager(ts.getAg());
            JasdlOntology ont = manager.getJasdlOntology(old_alias);
            
            ont.setAlias(new_alias);
        	
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'jasdl.ia.get_ontology_alias'! "+e);
        }
        return false;
    }
}
