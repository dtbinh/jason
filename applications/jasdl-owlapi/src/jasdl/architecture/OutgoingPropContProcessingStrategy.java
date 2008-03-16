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

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;

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
		try{
			SELiteral sl = new SELiteral(l, agent); // note, l might not (yet) be a valid se-literal (mappings may be performed below) so factory cannot be used	
			sl.qualifyOntologyAnnotation();
			
			Structure expr = new Structure("expr");			
			expr.addTerm(new StringTermImpl( agent.getManchesterObjectRenderer().render(sl.toOWLObject()) ));			
			sl.addAnnot(expr);
			
			return sl;
		}catch(NotEnrichedException e){
			// do nothing
			return l;
		}
	}

}
