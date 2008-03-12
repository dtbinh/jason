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

import static jasdl.util.Common.ONTOLOGY_ANNOTATION;
import static jasdl.util.Common.ORIGIN_ANNOTATION;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.DefinedAlias;
import jasdl.bridge.xsd.XSDDataType;
import jasdl.bridge.xsd.XSDDataTypeUtils;
import jasdl.util.InvalidSELiteralAxiomException;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;

public class LiteralFactory {
	
	private JasdlOntology ont;

	public LiteralFactory(JasdlOntology ont){
		this.ont = ont;
	}
	
	/**
	 * Polymorphically applies appropriate factory method depending on specialisation of axiom
	 * @param axiom
	 * @return
	 * @throws JasdlException	if specialisation of axiom is not of an appropriate type for conversion to a SE-Literal
	 */
	public Literal toLiteral(OWLIndividualAxiom axiom) throws JasdlException{
		if(axiom instanceof OWLClassAssertionAxiom){
			return toLiteral((OWLClassAssertionAxiom)axiom);
			
		}else if(axiom instanceof OWLObjectPropertyAssertionAxiom){
			return toLiteral((OWLObjectPropertyAssertionAxiom)axiom);
			
		}else if(axiom instanceof OWLDataPropertyAssertionAxiom){
			return toLiteral((OWLDataPropertyAssertionAxiom)axiom);	
			
		}else{
			throw new InvalidSELiteralAxiomException(axiom+" is not of an appropriate type for conversion to a SE-Literal");
		}
	}
	
	/**
	 * Axiom must be ground
	 * @param axiom
	 * @return
	 */
	public Literal toLiteral(OWLClassAssertionAxiom axiom) throws JasdlException{
		
		OWLDescription c = axiom.getDescription();
		
		String functor = ont.toAlias(c).getName();
		boolean sign = true;
		
		if(functor.startsWith("~")){
			sign = false;
			functor = functor.substring(1);
		}		
		
		Literal l = new Literal(sign, functor);
			
			
		OWLIndividual i	= axiom.getIndividual();
		Atom l_0 = new Atom( ont.toAlias(i).getName() );
		
		l.addTerm(l_0);
		
		// unneccesary, since the names of negated aliases are now prefixed with "~"
		//if(c instanceof OWLObjectComplementOf){
		//	l.setNegated(false);
		//}
		
		//add other annotations
		// l.addAnnots(ont.retrieveAnnotations(l));
		
		addOntologyAnnotation( l );
		
		
		// add origin annotation (applies to classes only)
		Alias alias = ont.toAlias(c);//AliasFactory.create(l);
		if(alias.defined()){
			addOriginAnnotation(l, ((DefinedAlias)alias).getOrigin());
		}
		
		return l;
	}	
	
	public Literal toLiteral(OWLObjectPropertyAssertionAxiom axiom) throws JasdlException{
		OWLObjectProperty p = axiom.getProperty().asOWLObjectProperty();
		Literal l = new Literal( ont.toAlias(p).getName() );
		
		OWLIndividual s	= axiom.getSubject();
		Atom l_0 = new Atom( ont.toAlias(s).getName() );		
		l.addTerm(l_0);
		
		OWLIndividual o	= axiom.getObject();
		Atom l_1 = new Atom( ont.toAlias(o).getName() );		
		l.addTerm(l_1);
		
		//add other annotations
		//l.addAnnots(ont.retrieveAnnotations(l));
		
		addOntologyAnnotation( l );
		return l;
	}
	
	
	public Literal toLiteral(OWLDataPropertyAssertionAxiom axiom) throws JasdlException{
		OWLDataProperty p = axiom.getProperty().asOWLDataProperty();
		Literal l = new Literal( ont.toAlias(p).getName() );
		
		OWLIndividual s	= axiom.getSubject();
		Atom l_0 = new Atom( ont.toAlias(s).getName() );		
		l.addTerm(l_0);
		
		OWLConstant o = axiom.getObject();	
		
		
		Term l_1;
		if(o.isTyped()){ //TODO: tidy up datatype recognition with enumeration
			OWLTypedConstant ot = o.asOWLTypedConstant();
			XSDDataType xsd = XSDDataTypeUtils.get(ot.getDataType().toString());
			// surround with quotes if necessary for representation in AgentSpeak syntax
			if(XSDDataTypeUtils.isStringType(xsd)){
				l_1 = DefaultTerm.parse("\""+o.getLiteral().toString()+"\"");
			}else if(XSDDataTypeUtils.isBooleanType(xsd)){
				if(Boolean.parseBoolean(ot.getLiteral().toString())){
					l_1 = Literal.LTrue;
				}else{
					l_1 = Literal.LFalse;
				}				
			}else{
				l_1 = DefaultTerm.parse(o.getLiteral().toString());
			}
		}else{
			throw new JasdlException("JASDL does not support untyped data ranges such as: "+axiom);
		}
		
		
		l.addTerm(l_1);
		
		// add other annotations
		//l.addAnnots(ont.retrieveAnnotations(l));
		
		addOntologyAnnotation( l );
		return l;
	}
	
	
	
	public void addOntologyAnnotation(Literal l){
		Structure s = new Structure(ONTOLOGY_ANNOTATION);
		s.addTerm( ont.getLabel() );
		l.addAnnot( s );
	}	
	
	public void addOriginAnnotation(Literal l, Atom origin){
		Structure s = new Structure(ORIGIN_ANNOTATION);
		s.addTerm( origin );
		l.addAnnot( s );
	}		
}
