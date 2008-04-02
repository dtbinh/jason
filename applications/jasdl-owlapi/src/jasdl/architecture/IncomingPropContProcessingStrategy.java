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
import static jasdl.util.Common.strip;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.exception.DuplicateMappingException;
import jasdl.util.exception.JasdlException;
import jasdl.util.exception.NotEnrichedException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.net.URISyntaxException;

public class IncomingPropContProcessingStrategy implements PropContProcessingStrategy{

	public Literal process(Literal l, JasdlAgent agent, String src) throws JasdlException{		
		try{
			Literal result;
			agent.getLogger().fine("Processing incoming "+l);
			
			SELiteral sl = new SELiteral(l, agent);
			sl.unqualifyOntologyAnnotation();
			
			result = processAllDifferent(sl);
			if(result!=null) return result;
			
			result = processNamed(sl, agent);
			if(result!=null) return result;
			
			result = processAnon(sl, agent);
			if(result!=null) return result;

		}catch(NotEnrichedException e){
			// do nothing
		}
		return l;

	}

	private Literal processAllDifferent(SELiteral sl){
		if(sl.getLiteral().getFunctor().equals(AliasFactory.OWL_ALL_DIFFERENT_FUNCTOR.toString())){
			return sl.getLiteral();
		}else{
			return null;
		}
	}
	
	private Literal processNamed(SELiteral sl, JasdlAgent agent) throws JasdlException{
		ListTerm nameds = sl.getLiteral().getAnnots(NAMED_ANNOTATION_FUNCTOR);
		if(nameds.size() == 1){
			// get named annotation
			Term _named = nameds.get(0);			
			if(!(_named instanceof Structure)){
				throw new JasdlException("Invalid "+NAMED_ANNOTATION_FUNCTOR+" annotation: "+_named);
			}
			Structure named = (Structure)_named;
			if(named.getArity() != 1){
				throw new JasdlException("Invalid "+NAMED_ANNOTATION_FUNCTOR+" annotation arity: "+_named);
			}			
			
			// get expression
			Term expressionTerm = named.getTerm(0);
			if(!(expressionTerm instanceof StringTerm)){
				throw new JasdlException("Invalid "+NAMED_ANNOTATION_FUNCTOR+" annotation term: "+expressionTerm);
			}
			String expression = strip(expressionTerm.toString(), "\""); // quotes stripped			
			
			
			URI uri;
			try {
				uri = new URI(expression);
			} catch (URISyntaxException e) {
				throw new JasdlException("Invalid entity URI in "+expression);
			}
			Alias local = agent.getAliasManager().getLeft(agent.toEntity(uri));		// will already be present by definition	
			
			// create new (SE) Literal
			sl.mutateFunctor(local.getFunctor().toString());

			
			sl.getLiteral().delAnnot(named); // drop named, no longer needed
			return sl.getLiteral(); // not able to modify literal functors directly
		}else{
			return null;
		}
	}
	
	private Literal processAnon(SELiteral sl, JasdlAgent agent) throws JasdlException{
		ListTerm anons = sl.getLiteral().getAnnots(ANON_ANNOTATION_FUNCTOR);
		if(anons.size() == 1){									
			// Get Anon annotation
			Term _anon = anons.get(0);
			if(!(_anon instanceof Structure)){
				throw new JasdlException("Invalid "+ANON_ANNOTATION_FUNCTOR+" annotation: "+_anon);
			}
			Structure anon = (Structure)_anon;
			if(anon.getArity() != 2){
				throw new JasdlException("Invalid "+ANON_ANNOTATION_FUNCTOR+" annotation arity: "+_anon);
			}
			
			// Get expression
			Term expressionTerm = anon.getTerm(0);
			if(!(expressionTerm instanceof StringTerm)){
				throw new JasdlException("Invalid "+ANON_ANNOTATION_FUNCTOR+" annotation expression term: "+expressionTerm);
			}
			String expression = strip(expressionTerm.toString(), "\""); // quotes stripped
			
			// Parse prequisite ontology URIs
			Term prereqsTerm = anon.getTerm(1);
			if(!(prereqsTerm instanceof ListTerm)){
				throw new JasdlException("Invalid "+ANON_ANNOTATION_FUNCTOR+" annotation prereqs term: "+expressionTerm);
			}
			ListTerm prereqs = (ListTerm)prereqsTerm;						
			for(Term _prereqTerm : prereqs){
				if(!(_prereqTerm instanceof StringTerm)){
					throw new JasdlException("Invalid "+ANON_ANNOTATION_FUNCTOR+" prereq: "+_prereqTerm);
				}
				StringTerm prereqTerm = (StringTerm)_prereqTerm;
				String prereq = strip(prereqTerm.toString(), "\""); // quotes stripped								
				agent.getOntology(prereq);
			}
			
			// do we already know this expression?			
			Atom functor = new Atom(sl.getLiteral().getFunctor());			
			try{
				agent.defineClass(functor, sl.getOntologyLabel(), expression, agent.getManchesterURIDescriptionParser()); // Instantiate defined expression
			}catch(DuplicateMappingException e){
				// do nothing
			}
			
			// drop anon annotation, no longer needed
			sl.getLiteral().delAnnot(anon);

			return sl.getLiteral();
		}else{
			return null;
		}

	}


}
