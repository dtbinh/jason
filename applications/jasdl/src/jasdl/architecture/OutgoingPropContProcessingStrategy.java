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
import static jasdl.util.Common.ORIGIN_ANNOTATION;
import static jasdl.util.Common.URI_ANNOTATION;
import static jasdl.util.Common.getAnnot;
import static jasdl.util.Common.getOntologyAnnotation;
import static jasdl.util.Common.isReservedKeyword;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.JasdlOntology;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.DefinedAlias;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

public class OutgoingPropContProcessingStrategy implements PropContProcessingStrategy {

	
	/**
	 * Only applies processing if l is SE-Literal, otherwise l is returned as is
	 * Replaces ontology label with physical uri
	 * Adds expr annotation unambiguously describing what is meant by alias
	 * Adds origin annotation if literal refers to defined class
	 * @param l
	 * @throws JasdlException
	 */
	public Literal process(Literal l, JasdlAgent agent) throws JasdlException {
		
		if(agent.isSELiteral(l)){
			// replace ontology label with physical uri
			Structure o = getOntologyAnnotation(l);
			JasdlOntology ont = agent.getOntology((Atom)o.getTerm(0));		
			o.setTerm(0, new StringTermImpl(ont.getPhysicalURI().toString()));	
		
			if(!isReservedKeyword(l.getFunctor())){ // avoid parsing, e.g. all_different
			
				Alias alias = ont.toAlias(l);
				
				if(alias.defined()){ // defined class, add expr,origin pair
					Term existing = getAnnot(l, EXPR_ANNOTATION);
					if(existing!=null) l.delAnnot(existing);
					
					existing = getAnnot(l, ORIGIN_ANNOTATION);
					if(existing!=null) l.delAnnot(existing);
					
					// add expr annotation			
					Structure expr = new Structure(EXPR_ANNOTATION);
					expr.addTerm(new StringTermImpl(ont.toExpr(alias)));
					l.addAnnot(expr);
				
					// add origin annotation		
					Structure origin = new Structure(ORIGIN_ANNOTATION);
					origin.addTerm(((DefinedAlias)alias).getOrigin());
					l.addAnnot(origin);
				}else{	// primitive class, add uri
					// add uri expression
					Structure uri = new Structure(URI_ANNOTATION);
					uri.addTerm(new StringTermImpl(ont.toURI(alias).toString()));
					l.addAnnot(uri);
				}
			}
		}
		
		return l;
	}

}
