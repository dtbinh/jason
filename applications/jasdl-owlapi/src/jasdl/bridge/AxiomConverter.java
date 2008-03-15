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
package jasdl.bridge;

import static jasdl.util.Common.RANGE;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.seliteral.SELiteralAllDifferentAssertion;
import jasdl.bridge.seliteral.SELiteralClassAssertion;
import jasdl.bridge.seliteral.SELiteralDataPropertyAssertion;
import jasdl.bridge.seliteral.SELiteralObjectPropertyAssertion;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLTypedConstant;
public class AxiomConverter {	
	
	private JasdlAgent agent;

	public AxiomConverter(JasdlAgent agent){
		this.agent = agent;
	}
	
	
	public Set<OWLIndividualAxiom> convertAndCheck(SELiteral sl) throws JasdlException{
		return convert(sl, true);
	}
	
	/**
	 * l must be ground
	 * @param l
	 * @return
	 * @throws JasdlException
	 */
	public OWLIndividualAxiom convert(SELiteral sl) throws JasdlException{
		if(!sl.isGround()){
			throw new JasdlException("Cannot create an axiom from unground SELiteral "+sl);
		}		
		Set<OWLIndividualAxiom> axioms = convert(sl, false);
		if(axioms.isEmpty()){
			throw new JasdlException("Error creating axiom from "+sl);
		}
		return (OWLIndividualAxiom)axioms.toArray()[0];
	}	
	
	/**
	 * TODO: Is there a better way to achieve this polymorphism?
	 * @param sl
	 * @param checkForExistence
	 * @return
	 * @throws JasdlException
	 */
	private Set<OWLIndividualAxiom> convert(SELiteral sl, boolean checkForExistence) throws JasdlException{
		if(sl instanceof SELiteralClassAssertion){
			return convert((SELiteralClassAssertion)sl, checkForExistence);
		}else if(sl instanceof SELiteralObjectPropertyAssertion){
			return create((SELiteralObjectPropertyAssertion)sl, checkForExistence);
		}else if(sl instanceof SELiteralDataPropertyAssertion){
			return create((SELiteralDataPropertyAssertion)sl, checkForExistence);
		}else if(sl instanceof SELiteralAllDifferentAssertion){
			return create((SELiteralAllDifferentAssertion)sl, checkForExistence);
		}else{
			throw new InvalidSELiteralException("JASDL does not know how to handle SELiterals like "+sl);
		}
	}
	
	
	private Set<OWLIndividualAxiom> convert(SELiteralClassAssertion sl, boolean checkForExistence) throws JasdlException{
		// TODO: Negated classes
		Set<OWLIndividual> is = new HashSet<OWLIndividual>();
		OWLDescription desc = sl.getOWLDescription();		
		if(sl.isGround()){
			OWLIndividual i = sl.getOWLIndividual();
			if(!checkForExistence || agent.getReasoner().hasType(i, desc)){
				is.add(i);
			}
		}else{
			is.addAll(agent.getReasoner().getIndividuals(desc, false));
		}		
		Set<OWLIndividualAxiom> axioms = new HashSet<OWLIndividualAxiom>();
		for(OWLIndividual i : is){
			axioms.add(agent.getOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(i, desc));
		}
		return axioms;
	}	
	
	public Set<OWLIndividualAxiom> create(SELiteralAllDifferentAssertion sl, boolean checkForExistence) throws JasdlException{
		OWLIndividual[] is = (OWLIndividual[])sl.getOWLIndividuals().toArray();		
    	// check they are mutually distinct (if we are checking for existence)
    	boolean distinct = true;
    	if(checkForExistence){	        	
        	for(int i=0; i<is.length; i++){ 		       		
        		for(int j=i+1; j<is.length; j++){
        			if(!agent.getReasoner().isDifferentFrom(is[i], is[j])){
        				distinct = false;
        				break;
        			}
        		}
        		if(!distinct) break;
        	}   
    	}       
    	Set<OWLIndividualAxiom> axioms = new HashSet<OWLIndividualAxiom>();
    	Set<OWLIndividual> different = new HashSet<OWLIndividual>();
    	if(!checkForExistence || distinct){
    		for(int i=0; i<is.length; i++){ 
        		different.add(is[i]);
        	}
        	OWLDifferentIndividualsAxiom axiom = agent.getOntologyManager().getOWLDataFactory().getOWLDifferentIndividualsAxiom(different);        	
        	axioms.add(axiom);
    	}
    	return axioms;
	}	
	
	public Set<OWLIndividualAxiom> create(SELiteralObjectPropertyAssertion sl, boolean checkForExistence) throws JasdlException{
		Set<OWLIndividual> os = new HashSet<OWLIndividual>();
		OWLIndividual s = sl.getSubject();
		OWLObjectProperty p = sl.getPredicate();
		if(sl.getTerm(RANGE).isGround()){
			OWLIndividual o = sl.getObject();
			if(!checkForExistence || agent.getReasoner().hasObjectPropertyRelationship(s, p, o)){
				os.add(o);
			}
		}else{
			os.addAll(agent.getReasoner().getRelatedIndividuals(s, p));
		}
		Set<OWLIndividualAxiom> axioms = new HashSet<OWLIndividualAxiom>();
		for(OWLIndividual o : os){
			axioms.add(agent.getOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(s, p, o));
		}
		return axioms;
	}
	
	public Set<OWLIndividualAxiom> create(SELiteralDataPropertyAssertion sl, boolean checkForExistence) throws JasdlException{
		Set<OWLConstant> os = new HashSet<OWLConstant>();
		OWLIndividual s = sl.getSubject();
		OWLDataProperty p = sl.getPredicate();
		if(sl.getTerm(RANGE).isGround()){
			OWLTypedConstant o = sl.getObject();
			if(!checkForExistence || agent.getReasoner().hasDataPropertyRelationship(s, p, o)){
				os.add(o);
			}
		}else{
			os.addAll(agent.getReasoner().getRelatedValues(s, p));
		}
		Set<OWLIndividualAxiom> axioms = new HashSet<OWLIndividualAxiom>();
		for(OWLConstant o : os){
			axioms.add(agent.getOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(s, p, o));
		}
		return axioms;
	}	
	
	
	
	
	
	
}