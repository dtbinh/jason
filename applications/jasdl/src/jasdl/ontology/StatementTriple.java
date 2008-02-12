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
package jasdl.ontology;

import jasdl.util.JasdlException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Used instead of Statement for passing triples since Statement does not permit null elements
 * Null elements are required for unground queries to the ontology
 * @author Tom Klapiscak
 *
 */
public class StatementTriple {
	private Resource subject;
	private Property predicate;
	private RDFNode object;
	
	public StatementTriple(Resource subject, Property predicate, RDFNode object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public Resource getSubject() {
		return subject;
	}

	public void setSubject(Resource subject) {
		this.subject = subject;
	}

	public Property getPredicate() {
		return predicate;
	}

	public void setPredicate(Property predicate) {
		this.predicate = predicate;
	}

	public RDFNode getObject() {
		return object;
	}

	public void setObject(RDFNode object) {
		this.object = object;
	}
	
	public boolean isGround(){
		return !(subject == null || predicate == null || object == null);
	}
	
	/**
	 * StatementTriple must be ground
	 * @param model
	 * @return
	 * @throws JasdlException	if statement is unground
	 */
	public Statement toStatement(OntModel model) throws JasdlException{
		if(!isGround()){
			throw new JasdlException("Cannot create statement from unground StatementTriple: "+this);
		}
		return model.createStatement(subject, predicate, object);
	}
	
	public String toString(){
		return "("+subject+", "+predicate+", "+object+")";
	}
	
	
	
	
}
