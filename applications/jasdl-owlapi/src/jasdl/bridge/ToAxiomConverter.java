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
import jasdl.bb.revision.IndividualAxiomToDescriptionConverter;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.seliteral.SELiteralAllDifferentAssertion;
import jasdl.bridge.seliteral.SELiteralClassAssertion;
import jasdl.bridge.seliteral.SELiteralDataPropertyAssertion;
import jasdl.bridge.seliteral.SELiteralObjectPropertyAssertion;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLTypedConstant;


/**
 * Accepts an arbitrary SEliteral and, depending on its type, creates an OWLIndividualAxiom encoding of it.
 * This factory can be used in one of two ways: for <i>creation</i> or for <i>retrieval</i>.<p>
 * 
 * Creation will always return a encoding regardless of whether the axiom is entailed by the ontology.
 * In this case, the SELiteral must be ground and only a <b>single</b> SELiteral can be returned.<p>
 *
 * Retrieval, on the other hand, will result in an empty list if the axiom is not entailed by the ontology, otherwise
 * a list containing SELiteral encodings of <b>all</b> entailments is returned. The retrieval operation can deal
 * with unground SELiterals, in which case unground terms will be treated as "wildcards" that match any (suitable) resource.<p> 
 * 
 * Creation example:
 * <ul>
 * 	<li>hotel(hilton)[o(travel)] -> ClassAssertion(Hotel hilton)</li>
 *  <li>hasRating(hilton,threeStarRating)[o(travel)] -> ObjectPropertyAssertion(hasRating hilton ThreeStarRating)</li>
 *  <li>all_different([hilton,fourSeasons])[o(travel)] -> DifferentIndividuals( fourSeasons hilton )</li>  
 * </ul><p>
 * 
 * Retrieval example:
 * <ul>
 * 	<li>hotel(hilton)[o(travel)] -> [ClassAssertion(Hotel hilton)]</li>
 *  <li>hasRating(hilton,X)[o(travel)] -> [ObjectPropertyAssertion(hasRating hilton OneStarRating), ObjectPropertyAssertion(hasRating hilton TwoStarRating), ObjectPropertyAssertion(hasRating hilton ThreeStarRating)]</li>
 * 	<li>hotel(threeStarRating)[o(travel)] -> []</li>
 *  <li>~hotel(threeStarRating)[o(travel)] -> [ObjectComplementOf(ClassAssertion(Hotel threeStarRating))]</li>
 * </ul>
 * @author Tom Klapiscak
 *
 */
public class ToAxiomConverter {	
	
	private JasdlAgent agent;

	public ToAxiomConverter(JasdlAgent agent){
		this.agent = agent;
	}
	
	
	public Set<OWLIndividualAxiom> retrieve(SELiteral sl) throws JasdlException{
		return convert(sl, true);
	}
	
	/**
	 * <b>Create</b> an axiomatic encoding of sl. Specific type of axiom dependent on specific type of sl.
	 * sl must be <b>ground</b>
	 * @param sl		the SE-Literal to encode
	 * @return			a single encoding of sl - regardless of whether it is entailed or not
	 * @throws JasdlException
	 */
	public OWLIndividualAxiom create(SELiteral sl) throws JasdlException{
		if(!sl.getLiteral().isGround()){
			throw new JasdlException("Cannot create an axiom from unground SELiteral "+sl);
		}		
		Set<OWLIndividualAxiom> axioms = convert(sl, false);
		if(axioms.isEmpty()){
			throw new JasdlException("Error creating axiom from "+sl);
		}
		return (OWLIndividualAxiom)axioms.toArray()[0];
	}	
	
	/**
	 * Polymorphically apply a factory method dependent on the specific type of sl
	 * @param sl				the SE-Literal to encode
	 * @param checkForExistence	if true, results will only be returned in they are entailed - if false, a successful encoding will guarantee a result
	 * @return					a set of entailments matching sl
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
	
	/**
	 * Convert a unary SELiteral (asserting a class membership of an individual) to its axiomatic encoding
	 * @param sl				the SELiteral to convert
	 * @param checkForExistence	if true, results will only be returned in they are entailed - if false, a successful encoding will guarantee a result
	 * @return					an axiomatic encoding of sl
	 * @throws JasdlException
	 */
	private Set<OWLIndividualAxiom> convert(SELiteralClassAssertion sl, boolean checkForExistence) throws JasdlException{
		try {
			Set<OWLIndividual> is = new HashSet<OWLIndividual>();		
			OWLDescription desc = sl.getOWLDescription();		
			
			if(sl.getLiteral().isGround()){
				OWLIndividual i = sl.getOWLIndividual();
				if(!checkForExistence || agent.getReasoner().hasType(i, desc, false)){
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
		} catch (OWLReasonerException e) {
			throw new JasdlException("Unable to convert "+sl+" to axiom. Reason: "+e);
		}
	}	
	
	/**
	 * Convert a unary all_different SELiteral (asserting distinctness of a set of individuals) to its axiomatic encoding
	 * @param sl				the SELiteral to convert
	 * @param checkForExistence	if true, results will only be returned in they are entailed - if false, a successful encoding will guarantee a result
	 * @return					an axiomatic encoding of sl
	 * @throws JasdlException
	 */
	public Set<OWLIndividualAxiom> create(SELiteralAllDifferentAssertion sl, boolean checkForExistence) throws JasdlException{
		try {
			Set<OWLIndividual> _is = sl.getOWLIndividuals();
			Object[] is = _is.toArray();	
			if(is.length == 0){
				throw new JasdlException("All different assertion must contain some individuals! "+sl);
			}
	    	// check they are mutually distinct (if we are checking for existence)
	    	boolean distinct = true;
	    	if(checkForExistence){	        	
	        	for(int i=0; i<is.length; i++){ 		       		
	        		for(int j=i+1; j<is.length; j++){
	        			// create a description that is satisfiable iff the two individuals are different. TODO: request in-built OWL-API support for this
	        			OWLDifferentIndividualsAxiom axiom = agent.getOntologyManager().getOWLDataFactory().getOWLDifferentIndividualsAxiom(_is);
	        			IndividualAxiomToDescriptionConverter conv = new IndividualAxiomToDescriptionConverter(agent.getOntologyManager().getOWLDataFactory());
	        			axiom.accept(conv);        			
						if(!agent.getReasoner().isSatisfiable(conv.getDescription())){//.isDifferentFrom((OWLIndividual)is[i], (OWLIndividual)is[j])){
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
	        		different.add((OWLIndividual)is[i]);
	        	}
	        	OWLDifferentIndividualsAxiom axiom = agent.getOntologyManager().getOWLDataFactory().getOWLDifferentIndividualsAxiom(different);        	
	        	axioms.add(axiom);
	    	}
	    	return axioms;
		} catch (OWLReasonerException e) {
			throw new JasdlException("Unable to convert "+sl+" to axiom. Reason: "+e);
		}
	}	
	
	/**
	 * Convert a binary SELiteral (asserting that two individuals are related by an object property) to its axiomatic encoding
	 * @param sl				the SELiteral to convert
	 * @param checkForExistence	if true, results will only be returned in they are entailed - if false, a successful encoding will guarantee a result
	 * @return					an axiomatic encoding of sl
	 * @throws JasdlException
	 */
	public Set<OWLIndividualAxiom> create(SELiteralObjectPropertyAssertion sl, boolean checkForExistence) throws JasdlException{
		try {
			Set<OWLIndividual> os = new HashSet<OWLIndividual>();
			OWLIndividual s = sl.getSubject();
			OWLObjectProperty p = sl.getPredicate();
			if(sl.getLiteral().getTerm(RANGE).isGround()){
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
		} catch (OWLReasonerException e) {
			throw new JasdlException("Unable to convert "+sl+" to axiom. Reason: "+e);
		}
	}
	
	/**
	 * Convert a binary SELiteral (asserting that an individual is related to a datatype literal) to its axiomatic encoding
	 * @param sl				the SELiteral to convert
	 * @param checkForExistence	if true, results will only be returned in they are entailed - if false, a successful encoding will guarantee a result
	 * @return					an axiomatic encoding of sl
	 * @throws JasdlException
	 */
	public Set<OWLIndividualAxiom> create(SELiteralDataPropertyAssertion sl, boolean checkForExistence) throws JasdlException{
		try {
			Set<OWLConstant> os = new HashSet<OWLConstant>();
			OWLIndividual s = sl.getSubject();
			OWLDataProperty p = sl.getPredicate();
			if(sl.getLiteral().getTerm(RANGE).isGround()){
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
		} catch (OWLReasonerException e) {
			throw new JasdlException("Unable to convert "+sl+" to axiom. Reason: "+e);
		}
	}	
	
	
	
	
	
	
}