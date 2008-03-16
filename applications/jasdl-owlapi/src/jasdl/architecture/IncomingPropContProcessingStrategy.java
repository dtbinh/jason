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

public class IncomingPropContProcessingStrategy implements PropContProcessingStrategy{

	public Literal process(Literal l, JasdlAgent agent) throws JasdlException{
		try{
			SELiteral sl = new SELiteral(l, agent); // note, l might not (yet) be a valid se-literal (mappings may be performed below) so factory cannot be used	
			// replace physical URI with ontology label if known, else instantiate and assign anonymous label
			sl.unqualifyOntologyAnnotation();
			
			
			
			return sl; // not able to modify literal functors directly
		}catch(NotEnrichedException e){
			// do nothing
			return l;
		}
	}

}
