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

import static jasdl.util.Common.ORIGIN_ANNOTATION;
import static jasdl.util.Common.getAnnot;
import static jasdl.util.Common.localName;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.DefinedAlias;
import jasdl.mapping.MappingStrategy;
import jasdl.util.InvalidSELiteralAxiomException;
import jasdl.util.JasdlException;
import jasdl.util.UnknownReferenceException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.RecognitionException;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.RemoveAxiom;

import clexer.owlapi.Clexer;


/**
 * Wraps around an OWLOntology
 * Maintains:
 * - alias mappings
 * - annotations
 * 
 * @author tom
 *
 */
public class JasdlOntology {	

	
	private OWLOntology owl;
	
	private Reasoner reasoner;	
	private JasdlAgent agent;
	
	private Atom label;
	
	private LiteralFactory literalFactory;
	private AxiomFactory axiomFactory;
	
	
	private HashMap<Alias, OWLObject> aliasToObjectMap;	
	private HashMap<OWLObject, Alias> objectToAliasMap;
	private HashMap<Alias, String> aliasToExprMap;
	
	/**
	 * Maps previously compiled class expressions to their alias, to prevent unecessary class expression parsing
	 */
	private HashMap<String, Alias> exprToAliasMap;
	
	private URI physicalURI;

	

	public JasdlOntology(JasdlAgent agent, Atom label, URI physicalURI) throws OWLOntologyCreationException{
		this.agent = agent;
		this.label = label;
		this.physicalURI = physicalURI;
		this.owl = getAgent().getManager().loadOntologyFromPhysicalURI( physicalURI );	
		this.axiomFactory = new AxiomFactory(this);
		this.literalFactory = new LiteralFactory(this);
		aliasToObjectMap = new HashMap<Alias, OWLObject>();
		objectToAliasMap = new HashMap<OWLObject, Alias>();
		aliasToExprMap = new HashMap<Alias, String>();
		exprToAliasMap = new HashMap<String, Alias>();
		
		PelletOptions.USE_TRACING = true;
		reasoner = new Reasoner(agent.getManager());
		Set<OWLOntology> importsClosure = getAgent().getManager().getImportsClosure(owl);
		reasoner.loadOntologies(importsClosure);
		reasoner.classify();		
		reasoner.getKB().setDoExplanation( true );
	}
	
	public URI getPhysicalURI(){
		return physicalURI;
	}
	
	public LiteralFactory getLiteralFactory(){
		return literalFactory;
	}
	
	public AxiomFactory getAxiomFactory(){
		return axiomFactory;
	}
	
	public JasdlAgent getAgent(){
		return agent;
	}
	
	/**
	 * Create OWLObject from a resource URI and map to local alias
	 * @param real
	 * @return
	 * @throws UnknownReferenceException
	 */
	public Alias toAlias(URI real) throws UnknownReferenceException{		
		return toAlias(toObject(real));
	}
	
	/**
	 * (Polymorphically) create an OWLObject from resource URI
	 * @param real
	 * @return
	 * @throws UnknownReferenceException
	 */
	public OWLObject toObject(URI real) throws UnknownReferenceException{
//		 clumsy approach, but I can't find any way of achieving this polymorphically (i.e. retrieve an OWLObject from a URI) using OWL-API
		OWLObject obj;
		if(getOwl().containsClassReference(real)){
			obj = getAgent().getManager().getOWLDataFactory().getOWLClass(real);
		}else if (getOwl().containsObjectPropertyReference(real)){	
			obj = getAgent().getManager().getOWLDataFactory().getOWLObjectProperty(real);
		}else if (getOwl().containsDataPropertyReference(real)){	
			obj = getAgent().getManager().getOWLDataFactory().getOWLDataProperty(real);
		}else if (getOwl().containsIndividualReference(real)){
			obj = getAgent().getManager().getOWLDataFactory().getOWLIndividual(real);
		}else{
			throw new UnknownReferenceException("Unknown ontology resource URI: "+real);
		}
		return obj;
	}
	
	
	public Alias toAlias(Literal l){
		Alias alias;
		String name = l.getFunctor();
		if(l.negated()){
			name = "~"+name;
		}		
		Structure origin = getAnnot(l, ORIGIN_ANNOTATION);
		if(origin == null){		
			alias = new Alias(name);
			if(!isMapped(alias)){
				// might be a self-defined class with implicit origin
				Alias definedAlias = new DefinedAlias(name, new Atom(agent.getAgentName()));
				if(isMapped(definedAlias)){ // only return self-defined if literal actually refers to a self-defined class
					alias = definedAlias;
				}
			}			
		}else{
			alias = new DefinedAlias(name, (Atom)origin.getTerm(0));
		}
		return alias;
	}
	
	
	public Alias toAlias(OWLObject object) throws UnknownReferenceException{
		Alias a = objectToAliasMap.get(object);
		if(a == null){
			throw new UnknownReferenceException("Unknown OWL object "+object);
		}
		return a;
	}
	
	public Alias toAlias(String expr) throws UnknownReferenceException{
		Alias a = exprToAliasMap.get(expr);
		if(a == null){
			throw new UnknownReferenceException("Unknown class expression "+expr);
		}
		return a;
	}
	
	public OWLObject toObject(Alias alias) throws UnknownReferenceException{
		OWLObject obj = aliasToObjectMap.get(alias);
		if(obj == null){
			// might be an unmarked (with origin) self-defined class
			//obj = aliasToObjectMap.get(new DefinedAlias(alias.getName(), new Atom(agent.getAgentName())));
			//if(obj == null){
				throw new UnknownReferenceException("Unknown alias "+alias);
			//}
		}
		return obj;
	}	
	
	public OWLObject toObject(String expr) throws UnknownReferenceException{
		return toObject(toAlias(expr));
	}
	
	public void addMapping(Alias alias, OWLObject o) throws JasdlException{
		// No! there is no reason to enforce this, and causes problems with classes defined at run time
		//if(isMapped(o)){ // can only map an entity once
		//	throw new JasdlException("Duplicate mapping on entity \""+o+"\"");
		//}
		if(aliasToObjectMap.containsKey(alias)){
			throw new JasdlException("Ambiguous alias: "+alias);
		}
		getAgent().getLogger().finest("Added mapping: \""+alias+"\" to \""+o+"\"");
		aliasToObjectMap.put(alias, o);
		objectToAliasMap.put(o, alias);
	}
	
	public void addMapping(Alias alias, String expr) throws JasdlException{
		if(aliasToExprMap.containsKey(alias)){
			throw new JasdlException("Ambiguous defined alias: "+alias);
		}
		getAgent().getLogger().finest("Added expression mapping: \""+alias+"\" to \""+expr+"\"");
		aliasToExprMap.put(alias, expr);
		exprToAliasMap.put(expr, alias);
	}
	
	public void removeMappings(Alias alias){
		objectToAliasMap.remove( aliasToObjectMap.get(alias) );
		exprToAliasMap.remove( aliasToExprMap.get(alias) );
		aliasToObjectMap.remove(alias);
		aliasToExprMap.remove(alias);
	}
	
	/**
	 * Applies mappings to all resources in an ontology according to composition of supplied strategies
	 * @param strategies	composition chain of strategies
	 */
	public void applyMappingStrategies(List<MappingStrategy> strategies) throws JasdlException{
		List<OWLEntity> ents = new Vector<OWLEntity>();
		ents.addAll(reasoner.getClasses());
		ents.addAll(reasoner.getProperties());
		ents.addAll(reasoner.getIndividuals());
		for(OWLEntity e : ents){
			if(!isMapped(e)){ // only apply mapping strategies to entities that have not been manually mapped already
				String alias = localName(e.getURI());
				for(MappingStrategy strategy : strategies){
					alias = strategy.apply(alias);
				}
				addMapping(new Alias(alias) , e);
			}
		}
	}
	
	public Atom getLabel(){
		return label;
	}
	
	public OWLOntology getOwl(){
		return owl;
	}
	
	public boolean isMapped(OWLObject o){
		return objectToAliasMap.containsKey(o);
	}
	
	public boolean isMapped(Alias alias){
		return aliasToObjectMap.containsKey(alias);
	}	

	public Reasoner getReasoner() {
		return reasoner;
	}

	public String toExpr(Alias alias) throws UnknownReferenceException{
		if(alias.defined()){
			return aliasToExprMap.get((DefinedAlias)alias);
		}else{
			//throw new UnknownReferenceException("Alias "+alias+" does not refer to a defined class");
			// below is allowed, to allow us to polymorphically (defined or primitive) get an expression describing a class (resource?)
			return "|"+((OWLNamedObject)toObject(alias)).getURI()+"|"; // guaranteed to be named?
		}
	}
	
	public URI toURI(Alias alias) throws UnknownReferenceException{
		if(alias.defined()){
			throw new UnknownReferenceException("Alias "+alias+" does not refer to a primitive class");
		}else{
			return ((OWLNamedObject)toObject(alias)).getURI(); // guaranteed to be named?
		}
	}
	
	public OWLDescription defineClass(Atom className, String expr, Atom origin) throws JasdlException{
		OWLDescription c;
		try {
			c = Clexer.parse(expr, getAgent().getManager().getOWLDataFactory());
		} catch (RecognitionException e) {
			throw new JasdlException("Invalid class expression. Reason: "+e);
		}
		agent.getLogger().finest("Defined new class "+c);
		
		Alias alias = new DefinedAlias(className.toString(), origin);
		
		removeMappings(alias); // remember, defined class mappings can be overwritten
		addMapping(alias, c);
		addMapping(alias, expr);
		
		return c;
	}
	
	
	public List<OWLObject> generalise(Alias alias) throws JasdlException{
		List<OWLObject> os = new Vector<OWLObject>();		
		OWLObject o = toObject(alias);
		if(o instanceof OWLClass){
			os.addAll(OWLReasonerAdapter.flattenSetOfSets(getReasoner().getAncestorClasses((OWLClass)o)));
		}else if(o instanceof OWLObjectProperty){
			os.addAll(OWLReasonerAdapter.flattenSetOfSets(getReasoner().getAncestorProperties((OWLObjectProperty)o)));
		}else if(o instanceof OWLDataProperty){
			os.addAll(OWLReasonerAdapter.flattenSetOfSets(getReasoner().getAncestorProperties((OWLDataProperty)o)));
		}	
		return os;		
	}
	
	
	
	public boolean addAxiom(OWLIndividualAxiom axiom) throws JasdlException{
		try {
			agent.getLogger().finest("Add axiom "+axiom);
			AddAxiom change = new AddAxiom(owl, axiom);				
			agent.getManager().applyChange(change);
			getReasoner().refresh();				
			if(!getReasoner().isConsistent()){
				// keep track of inconsistency here for belief revision
				removeAxiom(axiom);
				return false;
			}
			return true;
		} catch (OWLOntologyChangeException e) {
			throw new JasdlException("Unable to add axiom "+axiom+". Reason: "+e);
		}
	}
	
	public boolean removeAxiom(OWLIndividualAxiom axiom) throws JasdlException{
		try {
			agent.getLogger().finest("Remove axiom "+axiom);
			RemoveAxiom change = new RemoveAxiom(owl, axiom);			
			agent.getManager().applyChange(change);
			getReasoner().refresh();
			return true;
		}catch(OWLOntologyChangeException e){
			throw new JasdlException("Unable to remove axiom "+axiom+". Reason: "+e);
		}
	}
	
	
	public List<Literal> getABoxState() throws JasdlException{
		List<Literal> bels = new Vector<Literal>();
		for(OWLIndividualAxiom axiom : owl.getIndividualAxioms()){				
			try {
				bels.add(getLiteralFactory().toLiteral(axiom));	
			} catch (InvalidSELiteralAxiomException e) {
				// do nothing, this just means axiom is not a class or property axiom
			}
		}
		return bels;
	}
	
	
	
	

	
	
	
}
