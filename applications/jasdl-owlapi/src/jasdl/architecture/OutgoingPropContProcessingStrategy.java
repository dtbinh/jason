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
package jasdl.architecture;

import static jasdl.architecture.JasdlAgArch.ANON_ANNOTATION_FUNCTOR;
import static jasdl.architecture.JasdlAgArch.NAMED_ANNOTATION_FUNCTOR;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.seliteral.SELiteralAllDifferentAssertion;
import jasdl.util.exception.JasdlException;
import jasdl.util.exception.NotEnrichedException;
import jasdl.util.exception.UnknownMappingException;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;

public class OutgoingPropContProcessingStrategy implements PropContProcessingStrategy {

	
	/**
	 * Only applies processing if l is SE-Literal, otherwise l is returned as is
	 * Replaces ontology label with physical uri
	 * Adds expr annotation unambiguously describing what is meant by alias
	 * Adds origin annotation if literal refers to defined class
	 * @param l
	 * @throws JasdlException
	 */
	public Literal process(Literal l, JasdlAgent agent, String src) throws JasdlException {
		agent.getLogger().fine("Processing outgoing "+l);
		
		Literal result = l;
		try{
			SELiteral sl = agent.getSELiteralFactory().construct(l);				

			//	qualify o
			sl.qualifyOntologyAnnotation();
			
			OWLEntity entity = (OWLEntity)sl.toOWLObject();			
			String expression = normaliseExpression(agent.getManchesterObjectRenderer().render(entity), agent);
			StringTerm expressionTerm = new StringTermImpl(expression);
			
			if(entity.isOWLClass() && agent.getDefinitionManager().isKnownLeft(entity.asOWLClass())){	 // we have an anonymous run-time defined class
				
				// construct anon annotation
				Structure anon = new Structure(ANON_ANNOTATION_FUNCTOR);
				anon.addTerm(expressionTerm);				
				
				// add set of prerequisite ontologies
				Set<OWLOntology> prereqs = new HashSet<OWLOntology>();
				String[] tokens = expression.toString().split("[ |\n]");
				for(String token : tokens){
					try {
						URI entityURI = new URI(token);
						URI ontologyURI = new URI(entityURI.getScheme(), entityURI.getSchemeSpecificPart(), null);
						prereqs.add(agent.getLogicalURIManager().getLeft(ontologyURI));
					} catch (URISyntaxException e) {	
						// do nothing, probably a keyword
					} catch(UnknownMappingException e){
						// do nothing, probably a keyword
					}
				}
				ListTerm list = new ListTermImpl();
				for(OWLOntology prereq : prereqs){
					list.add( new StringTermImpl(agent.getPhysicalURIManager().getRight(prereq).toString()) );
				}
				anon.addTerm(list);
				
				sl.getLiteral().addAnnot(anon);	
				
			}else{
				
				// unambiguously refer to named entity				
				if(!(sl instanceof SELiteralAllDifferentAssertion)){// not required if all_different
					Structure named = new Structure(NAMED_ANNOTATION_FUNCTOR);			
					named.addTerm(expressionTerm);			
					sl.getLiteral().addAnnot(named);
				}	
							
			}			
			result = sl.getLiteral();
		}catch(NotEnrichedException e){
			// do nothing
		}
		return result;
	}
	
	/**
	 * Returns an expression in which all references to run-time defined classes have been (recursuvely) replaced 
	 * with the rendering of their anonymous descriptions, thus ensuring this rendering only refers to predefined classes. 
	 * @param expression		expression to normalise
	 * @param agent
	 * @return					normalised form of expression
	 * @throws JasdlException
	 */
	private String normaliseExpression(String expression, JasdlAgent agent) throws JasdlException{
		String[] tokens = expression.toString().split("[ |\n]");
		String newExpression = "";
		for(String token : tokens){
			try {
				URI entityURI = new URI(token);
				OWLEntity entity = agent.toEntity(entityURI);
				if(entity.isOWLClass()){
					try{
						OWLDescription desc = agent.getDefinitionManager().getRight(entity.asOWLClass());
						String rendering =  agent.getManchesterObjectRenderer().render(desc);
						newExpression += "("+normaliseExpression(rendering, agent)+")";	
					}catch(UnknownMappingException e1){
						// this is a predefined class
						newExpression += token;
					}									
				}else{
					// this is a predefined non-class entity (property, individual, etc)
					newExpression += token;
				}
			}catch(URISyntaxException e){
				// this is (probably) a keyword
				newExpression += " " + token + " ";
			}catch(UnknownMappingException e2){
				// this is (probably) a keyword
				newExpression += " " + token + " ";
			}
		
		}
		return newExpression;
	}

}
