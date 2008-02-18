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
package jasdl.automap;

import jasdl.ontology.Alias;
import jasdl.ontology.JasdlOntology;
import jasdl.util.JasdlException;

import java.net.URI;

/**
 * Standard interface implemented by classes providing automapping operations
 * @author Tom Klapiscak
 *
 */
public abstract class Automap {	
	/**
	 * Apply this automapping operation to the ontology
	 * @param o		the ontology to apply the automapping operation to
	 */
	abstract void map(JasdlOntology ont) throws JasdlException;
	
	/* Common automapping utility methods */
	protected static void uncapitaliseAndMap(String name, JasdlOntology ont) throws JasdlException{
		String x = name.substring(0, 1);
		if(x.equals(x.toUpperCase())){//if first letter is upper case
			// check for Thing and Nothing, change ns accordingly
			String ns = ont.getLogicalNs().toString();
			if(name.equals("Thing") || name.equals("Nothing")){
				ns = ont.getModel().getNsPrefixURI("owl");
			}			
			URI real = URI.create(ns+name);
			Alias alias = new Alias(x.toLowerCase()+(name.substring(1)));			
			ont.addAliasMapping(alias, real);
		}
	}
}
