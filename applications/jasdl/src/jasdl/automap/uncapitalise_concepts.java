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

import jasdl.ontology.JasdlOntology;
import jasdl.util.JasdlException;

import java.util.logging.Logger;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Automatically maps all capitalised individuals to uncapitalised aliases so they can be referred to by the functors of Jason Literals

 * @author Tom Klapiscak
 * 
 */
public class uncapitalise_concepts extends Automap{
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass().getName());	
	
	public void map(JasdlOntology ont) throws JasdlException{
		ExtendedIterator it = ont.getModel().listClasses();
		while(it.hasNext()){
			OntClass c = (OntClass)it.next();
			if(!c.isAnon()){
				uncapitaliseAndMap(c.getLocalName(), ont);
			}			
		}
	}
	

}
