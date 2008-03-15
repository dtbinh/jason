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

import static jasdl.util.Common.DOMAIN;
import static jasdl.util.Common.RANGE;
import static jasdl.util.Common.isAllDifferentAssertion;
import static jasdl.util.Common.isClassAssertion;
import static jasdl.util.Common.isDataPropertyAssertion;
import static jasdl.util.Common.isObjectPropertyAssertion;
import static jasdl.util.Common.strip;
import static jasdl.util.Common.surroundedBy;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.xsd.XSDDataType;
import jasdl.bridge.xsd.XSDDataTypeUtils;
import jasdl.util.JasdlException;
import jasdl.util.UnknownReferenceException;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLTypedConstant;
public class AxiomFactory {
	
	private JasdlOntology ont;
	
	public AxiomFactory(JasdlOntology ont){
		this.ont = ont;
	}	
	
	/**
	 * if l is ground, corresponding axiom is returned only if it can be inferred
	 * if l is unground, all inferred axioms are returned
	 * 
	 * Now creates literal referring to parent ontology (a bit messy because ontology information is contained within literal itself, but was necessary for ungrounded ontology annotations)
	 * @param l
	 * @return
	 * @throws JasdlException
	 */
	private List<OWLIndividualAxiom> get(Literal l, boolean mustExist) throws JasdlException{
		List<OWLIndividualAxiom> axioms = new Vector<OWLIndividualAxiom>();
		if(isAllDifferentAssertion(l, ont)){ // needs to take precedence over class expression checking
			
			if(l.negated()){
				throw new JasdlException("JASDL does not currently support negated all_different assertions such as "+l+", since OWL makes the UNA by default and JASDL doesn't allow this to be overridden");
			}
			
			Set<OWLIndividual> different = new LinkedHashSet<OWLIndividual>(); // must be ordered! (for ListTerm unification)
        	List<Term> is = ((ListTerm)l.getTerm(0)).getAsList();
        	
        	// check they are mutually distinct (if it must exist)
        	boolean distinct = true;
        	if(mustExist){	        	
	        	for(int i=0; i<is.size(); i++){ 		       		
	        		OWLIndividual x = toIndividual(ont, new Alias(is.get(i).toString()));  
	        		for(int j=i+1; j<is.size(); j++){
	        			OWLIndividual y = toIndividual(ont, new Alias(is.get(j).toString())); 
	        			if(!ont.getReasoner().isDifferentFrom(x, y)){
	        				distinct = false;
	        				break;
	        			}
	        		}
	        		if(!distinct) break;	        		
	        	}   
        	}        	
        	if(distinct || !mustExist){
        		for(int i=0; i<is.size(); i++){ 
	        		OWLIndividual x = toIndividual(ont, new Alias(is.get(i).toString())); 
	        		different.add(x);
	        	}
	        	OWLDifferentIndividualsAxiom axiom = ont.getAgent().getManager().getOWLDataFactory().getOWLDifferentIndividualsAxiom(different);        	
	        	axioms.add(axiom);
        	}       	
				
		}else if(isClassAssertion(l, ont)){
			OWLDescription o;
			Alias alias = ont.toAlias(l);
			try{
				o = (OWLDescription)ont.toObject(alias);
			}catch(UnknownReferenceException e){
				if(l.negated()){
					Literal clone = (Literal)l.clone();
					clone.setNegated(true);
					Alias unnegatedAlias = ont.toAlias(clone);
					o = ont.getAgent().getManager().getOWLDataFactory().getOWLObjectComplementOf( (OWLDescription)ont.toObject(unnegatedAlias) );
					ont.addMapping(alias, o);
					ont.addMapping(alias, "not ("+ont.toExpr( unnegatedAlias )+")");
				}else{
					throw e;
				}
			}
			List<OWLIndividual> ss = new Vector<OWLIndividual>();
			if(l.isGround()){
				OWLIndividual i = toIndividual( ont, new Alias(l.getTerm(DOMAIN).toString()) );
				if( ont.getReasoner().hasType(i, o) || !mustExist){
					ss.add( i );
				}
			}else{
				ss.addAll(ont.getReasoner().getIndividuals(o, false));
			}
			for(OWLIndividual s : ss){
				axioms.add( ont.getAgent().getManager().getOWLDataFactory().getOWLClassAssertionAxiom(s, o));
			}
			
		}else if(isObjectPropertyAssertion(l, ont)){
			if(l.negated()){
				throw new JasdlException("JASDL does not currently support negated object property assertions such as "+l);
			}
			if(!l.getTerm(DOMAIN).isGround()){
				throw new JasdlException("JASDL cannot handle left-unground object property assertions such as "+l);
			}			
			OWLIndividual     s = toIndividual( ont, new Alias(l.getTerm(DOMAIN).toString()) );
			OWLObjectProperty p = toObjectProperty( ont, ont.toAlias(l));			
			List<OWLIndividual> os = new Vector<OWLIndividual>();
			if(l.getTerm(RANGE).isGround()){
				OWLIndividual o = toIndividual(ont, new Alias(l.getTerm(RANGE).toString()) );
				if(ont.getReasoner().hasObjectPropertyRelationship(s, p, o) || !mustExist){
					os.add( o );
				}
			}else{
				os.addAll(ont.getReasoner().getRelatedIndividuals(s, p));
			}
			for(OWLIndividual o : os){
				OWLIndividualAxiom axiom;
				axiom = ont.getAgent().getManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(s, p, o);	
				axioms.add(axiom);
			}			
		}else if(isDataPropertyAssertion(l, ont)){
			if(l.negated()){
				throw new JasdlException("JASDL does not currently support negated data property assertions such as "+l);
			}
			if(!l.getTerm(DOMAIN).isGround()){
				throw new JasdlException("JASDL cannot handle left-unground data property assertions such as "+l);
			}
			OWLIndividual     s = toIndividual( ont, new Alias(l.getTerm(DOMAIN).toString()) );
			OWLDataProperty   p = toDataProperty( ont, ont.toAlias(l) );
			List<OWLConstant> os = new Vector<OWLConstant>();
			
			OWLDataType typ = (OWLDataType)p.getRanges(ont.getOwl()).toArray()[0]; // will this always return exactly 1 range? IF not, how should I deal with it
			
			if(l.getTerm(RANGE).isGround()){
				OWLTypedConstant o = toOWLTypedConstant(ont, l.getTerm(RANGE), typ);
				if(ont.getReasoner().hasDataPropertyRelationship(s, p, o) || !mustExist){
					os.add( o );
				}
			}else{
				os.addAll(ont.getReasoner().getRelatedValues(s, p));
			}
			
			for(OWLConstant o : os){
				OWLIndividualAxiom axiom;
				axiom = ont.getAgent().getManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(s, p, o);	
				axioms.add(axiom);
			}
		}
		return axioms;
	}
	
	public OWLObjectProperty toObjectProperty(JasdlOntology ont, Alias alias) throws JasdlException{
		return (OWLObjectProperty)ont.toObject(alias);
	}
	
	public OWLDataProperty toDataProperty(JasdlOntology ont, Alias alias) throws JasdlException{
		return (OWLDataProperty)ont.toObject(alias);
	}
	
	public List<OWLIndividualAxiom> get(Literal l) throws JasdlException{
		return get(l, true);
	}
	
	/**
	 * l must be ground
	 * @param l
	 * @return
	 * @throws JasdlException
	 */
	public OWLIndividualAxiom create(Literal l) throws JasdlException{
		if(!l.isGround()){
			throw new JasdlException("Cannot create an axiom from unground literal "+l);
		}		
		List<OWLIndividualAxiom> axioms = get(l, false);
		if(axioms.isEmpty()){
			throw new JasdlException("Error creating axiom from "+l);
		}
		return axioms.get(0);
	}
		
	
	
	/**
	 * Creates and maps individual if unknown
	 * @param alias		alias (and local name if individual to be created)
	 * @return			a (possibly new) individual
	 * @throws JasdlException
	 */
	public OWLIndividual toIndividual(JasdlOntology ont, Alias alias) throws JasdlException{
		try{
			OWLObject object = ont.toObject(alias);
			if(!(object instanceof OWLIndividual)){
				throw new JasdlException("\""+object+"\" does not refer to an individual");
			}
			return (OWLIndividual)object;
		}catch(UnknownReferenceException e){
			// individual does not exist, create a new one
			OWLIndividual i = ont.getAgent().getManager().getOWLDataFactory().getOWLIndividual(URI.create(ont.getOwl().getURI() + "#" + alias.getName()));
			ont.addMapping(alias, i);
			return i;
		}
	}	

	public OWLTypedConstant toOWLTypedConstant(JasdlOntology ont, Term t, OWLDataType typ) throws JasdlException{
		XSDDataType xsd = XSDDataTypeUtils.get(typ.toString());
		// force string datatypes to be surrounded with quotes
		if(XSDDataTypeUtils.isStringType(xsd)){
			if(!surroundedBy(t.toString(), "\"")){
				throw new JasdlException(t+" cannot be of type "+typ);
			}
		}		
		return ont.getAgent().getManager().getOWLDataFactory().getOWLTypedConstant(strip(t.toString(), "\""), typ);
	}
	
}
