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

import static jasdl.util.Common.EXPR_ANNOTATION;
import static jasdl.util.Common.URI_ANNOTATION;
import static jasdl.util.Common.getAnnot;
import static jasdl.util.Common.getOntologyAnnotation;
import static jasdl.util.Common.mutateLiteral;
import static jasdl.util.Common.strip;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.JasdlOntology;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.DefinedAlias;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jasdl.util.JasdlMessageFormatException;
import jasdl.util.UnknownReferenceException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyCreationException;

public class IncomingPropContProcessingStrategy implements PropContProcessingStrategy{

	public Literal process(Literal l, JasdlAgent agent) throws JasdlException{
		// replace physical URI with ontology label if known, else instantiate and assign anonymous label
		Structure o;
		try{
			o = getOntologyAnnotation(l);	
		}catch(InvalidSELiteralException e){
			return l; // return as is if no ontology annotation present
			// note, l might not (yet) be a valid se-literal (mappings may be performed below) so agent.isSELiteral cannot be used
		}
		Term _uri = o.getTerm(0);
		if(!_uri.isString()){// assert o has StringTerm
			throw new JasdlMessageFormatException("Ontology annotation must have a String term. Supplied: "+_uri);
		}
		URI uri;
		try{
			uri = new URI(strip(_uri.toString(), "\"")); // quotes stripped
		}catch(URISyntaxException e){
			throw new JasdlMessageFormatException("Invalid ontology URI supplied. Reason: "+e);
		}
		JasdlOntology ont;
		try{
			ont = agent.getOntology(uri); // known ontology?
		}catch(UnknownReferenceException e){
			// instantiate unknown ontology (with anonymous label)
			try {
				ont = agent.createJasdlOntology(uri);
			} catch (OWLOntologyCreationException e1) {
				throw new JasdlException("Error instantiating supplied ontology "+uri+". Reason: "+e);
			}
		}
		// replace uri with label
		o.setTerm(0, ont.getLabel());
		
		// translate primitive alias mappings, instantiating unknown defined classes as required
		Alias alias = ont.toAlias(l);
		if(alias.defined()){
			// defined class
			Term _expr;
			try{
				_expr = getAnnot(l, EXPR_ANNOTATION).getTerm(0);
			}catch(NullPointerException e){
				throw new JasdlMessageFormatException("No expr annotation associated with defined class alias "+alias);
			}
			
			if(!_expr.isString()){
				throw new JasdlMessageFormatException("expr annotation must have a String term. Supplied: "+_expr);
			}
			String expr = strip(_expr.toString(), "\""); // quotes stripped
			OWLObject obj;
			try{
				// known class expression, just map its compilation to this alias
				// must remove any previous mappings, since origin's meaning of alias may have changed
				ont.removeMappings(alias); // remember: defined class mappings can be overwritten				
				obj = ont.toObject(expr);
				ont.addMapping(alias, obj);
				ont.addMapping(alias, expr);
			}catch(UnknownReferenceException e){
				// we need to compile this class expression, mapping is performed implicitly
				ont.defineClass(new Atom(alias.getName()), expr, ((DefinedAlias)alias).getOrigin());
			}
			
			// drop expr annotation, it's not needed anymore and shouldn't be exposed to user
			l.delAnnot(getAnnot(l, EXPR_ANNOTATION));
		}else{			
			// primitive resource
			Term _real;
			try{
				 _real = getAnnot(l, URI_ANNOTATION).getTerm(0);
			}catch(NullPointerException e){
				throw new JasdlMessageFormatException("No uri annotation associated with primitive resource alias "+alias);
			}			
			
			if(!_real.isString()){
				throw new JasdlMessageFormatException("uri annotation must have a String term. Supplied: "+_real);
			}			
			URI real;
			try {
				real = new URI(strip(_real.toString(), "\"")); // quotes stripped
			} catch (URISyntaxException e) {
				throw new JasdlException("Invalid ontology resource URI supplied. Reason: "+e);
			}
			
			// translate literal alias
			String newFunctor = ont.toAlias(real).getName();
			l = mutateLiteral(l, newFunctor);
			
			// drop uri annotation, it's not needed anymore and shouldn't be exposed to user
			l.delAnnot(getAnnot(l, URI_ANNOTATION));
		}
		
		
		
		
		return l; // not able to modify literal functors directly
	}

}
