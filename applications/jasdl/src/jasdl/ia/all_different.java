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

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.JasdlOntology;
import jasdl.bridge.alias.Alias;
import jasdl.util.JasdlException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;

/**
 * Accepts a list of atoms representing a set of individuals and an string representing an ontology alias
 * Asserts in the given ontology that all given individuals are different from one another.
 * 
 * Example:
 * jasdl.ia.all_different([tom, ben, mike], people) would assert that tom ben and mike all refer to different individuals
 * in the ontology referred to by the alias "people".
 * 
 * @author Tom Klapiscak
 *
 */
public class all_different extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("jasdl."+all_different.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	if(args.length != 2){
        		throw new JasdlException("Requires two arguments, a list of Atoms and an atom");
        	}
        	
        	if(!args[0].isList()){
        		throw new JasdlException("First argument must be a list of atoms. Given: "+args[0]);
        	}
        	if(!args[1].isAtom()){
        		throw new JasdlException("Second argument must be an atom. Given: "+args[1]);
        	}
        	if(!(ts.getAg() instanceof JasdlAgent)){
        		throw new JasdlException("jasdl.ia.add_different may only be used in conjuction with the JasdlAgent");
        	}
        	JasdlAgent ag = (JasdlAgent)ts.getAg();
        	
        	JasdlOntology ont = ag.getOntology((args[1].toString()));      	
        	
        	
        	
        	Set<OWLIndividual> different = new HashSet<OWLIndividual>();
        	List<Term> is = ((ListTerm)args[0]).getAsList();
        	for(Term i : is){
        		OWLObject obj = ont.toObject( new Alias(i.toString())); // individuals can't be defined classes
        		if(!(obj instanceof OWLIndividual)){
        			throw new JasdlException(i+" does not refer to an individual");
        		}        		
        		different.add((OWLIndividual)obj);
        	}   
        	
        	OWLDifferentIndividualsAxiom axiom = ag.getManager().getOWLDataFactory().getOWLDifferentIndividualsAxiom(different);
        	
        	ont.addAxiom(axiom);
        	
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'jasdl.ia.all_different'! "+e);
        }
        return false;
    }
}
