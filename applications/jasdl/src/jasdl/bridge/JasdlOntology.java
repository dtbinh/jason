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
import static jasdl.util.Common.isReservedKeyword;
import static jasdl.util.Common.localName;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.DefinedAlias;
import jasdl.mapping.MappingStrategy;
import jasdl.util.InvalidSELiteralAxiomException;
import jasdl.util.JasdlException;
import jasdl.util.UnknownReferenceException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.antlr.runtime.RecognitionException;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.debugging.DebuggerDescriptionGenerator;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owl.model.RemoveAxiom;

import clexer.owlapi.Clexer;

import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.SatisfiabilityConverter;


/**
 * Wraps around an OWLOntology
 * Maintains:
 * - alias mappings
 * - annotations
 * 
 * TODO: Annotation gathering for all_different assertions should be independent of argument ordering (because it is a *set*, not a *list*!)
 * 
 * @author tom
 *
 */
public class JasdlOntology {	

	private OWLOntology owl;
	
	/**
	 * Reasoner instantiation used to reason over this ontology
	 */
	private Reasoner reasoner;
	
	/**
	 * Agent this ontology is associated with
	 */
	private JasdlAgent agent;
	
	/**
	 * Atomic label associated with this ontology (used in ontology annotation of se-literals)
	 */
	private Atom label;
	
	/**
	 * Instantiation of literal factory used to convert axioms into equivalent se-literal representation
	 */
	private LiteralFactory literalFactory;
	
	/**
	 * Instantiation of axiom factory used to convert se-literals into equivalent axiomatic representation
	 */
	private AxiomFactory axiomFactory;
	
	/**
	 * Maps aliases to OWLObjects
	 */
	private HashMap<Alias, OWLObject> aliasToObjectMap;	
	
	/**
	 * Maps OWLObjects to aliases
	 */
	private HashMap<OWLObject, Alias> objectToAliasMap;
	
	/**
	 * Maps aliases to previously-compiled class expressions
	 */
	private HashMap<Alias, String> aliasToExprMap;
	
	/**
	 * Maps previously compiled class expressions to their alias
	 */
	private HashMap<String, Alias> exprToAliasMap;
	
	/**
	 * The physical URI this ontology was loaded from
	 */
	private URI physicalURI;
	
	/**
	 * Maps (asserted) SE-literals to annotations
	 */
	private HashMap<Literal, ListTerm> annotationMap;
	
	
	
	private JasdlReasonerFactory reasonerFactory;

	public JasdlOntology(JasdlAgent agent, Atom label, URI physicalURI) throws OWLOntologyCreationException{
		this.agent = agent;
		this.label = label;
		this.physicalURI = physicalURI;
		this.owl = getAgent().getManager().loadOntologyFromPhysicalURI( physicalURI );	
		this.axiomFactory = new AxiomFactory(this);
		this.literalFactory = new LiteralFactory(this);
		this.reasonerFactory = new JasdlReasonerFactory(new Reasoner(agent.getManager()));
		aliasToObjectMap = new HashMap<Alias, OWLObject>();
		objectToAliasMap = new HashMap<OWLObject, Alias>();
		aliasToExprMap = new HashMap<Alias, String>();
		exprToAliasMap = new HashMap<String, Alias>();
		annotationMap = new HashMap<Literal, ListTerm>();
		
		PelletOptions.USE_TRACING = true;
		reasoner = new Reasoner(agent.getManager());
		
		Set<OWLOntology> importsClosure = getAgent().getManager().getImportsClosure(owl);
		reasoner.loadOntologies(importsClosure);
		reasoner.classify();		
		reasoner.getKB().setDoExplanation( true );
		
		
	}
	
	/**
	 * Returns the alias associated with an ontological resource identified by uri (if known).
	 * @param uri	URI of ontological resource to map to alias
	 * @return		alias mapped to ontological resource referred to by uri
	 * @throws UnknownReferenceException	if uri refers to an unknown ontological resource
	 */
	public Alias toAlias(URI uri) throws UnknownReferenceException{	
		// Create OWLObject from a resource URI and map to local alias
		return toAlias(toObject(uri));
	}	
	
	/**
	 * Returns the alias mapped to the resource referred to by a SE-literal.
	 * <p> Negated literal aliases are prefixed with "~"
	 * <p> If literal has origin annotation, a defined alias is created
	 * <p> If literal lacks origin annotation but cannot be found, self-defined alias is returned if it can be found
	 * <p> Otherwise, primitive alias with functor of literal is returned
	 * @param l		literal to map to alias
	 * @return		alias mapped to literal
	 */
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
	
	/**
	 * Returns the alias mapped to an OWLObject (if known).
	 * @param object	OWLObject to map to alias
	 * @return			alias mapped to object
	 * @throws UnknownReferenceException	if object is not known
	 */
	public Alias toAlias(OWLObject object) throws UnknownReferenceException{
		Alias a = objectToAliasMap.get(object);
		if(a == null){
			throw new UnknownReferenceException("Unknown OWL object "+object);
		}
		return a;
	}
	
	/**
	 * Returns the alias associated with a previously compiled class-expression (if known).
	 * @param expr	class-expression to map to alias
	 * @return		alias mapped to class-expression
	 * @throws UnknownReferenceException	if class-expression is not known (i.e. not previously compiled)
	 */
	public Alias toAlias(String expr) throws UnknownReferenceException{
		Alias a = exprToAliasMap.get(expr);
		if(a == null){
			throw new UnknownReferenceException("Unknown class expression "+expr);
		}
		return a;
	}
	
	/**
	 * Returns the OWLObject associated with an alias (if known).
	 * @param alias		alias to map to OWLObject
	 * @return			OWLObject mapped to alias
	 * @throws UnknownReferenceException	if OWLObject is not known
	 */
	public OWLObject toObject(Alias alias) throws UnknownReferenceException{
		OWLObject obj = aliasToObjectMap.get(alias);
		if(obj == null){
			throw new UnknownReferenceException("Unknown alias "+alias);
		}
		return obj;
	}		
	
	/**
	 * (Polymorphically) create an OWLObject from resource URI (if known).
	 * @param uri	URI of resource to create OWLObject from
	 * @return		OWLObject identified by URI
	 * @throws UnknownReferenceException	if OWLObject not known
	 */
	public OWLObject toObject(URI uri) throws UnknownReferenceException{
		// clumsy approach, but I can't find any way of achieving this polymorphically (i.e. retrieve an OWLObject from a URI) using OWL-API
		OWLObject obj;
		if(getOwl().containsClassReference(uri)){
			obj = getAgent().getManager().getOWLDataFactory().getOWLClass(uri);
		}else if (getOwl().containsObjectPropertyReference(uri)){	
			obj = getAgent().getManager().getOWLDataFactory().getOWLObjectProperty(uri);
		}else if (getOwl().containsDataPropertyReference(uri)){	
			obj = getAgent().getManager().getOWLDataFactory().getOWLDataProperty(uri);
		}else if (getOwl().containsIndividualReference(uri)){
			obj = getAgent().getManager().getOWLDataFactory().getOWLIndividual(uri);
		}else{
			throw new UnknownReferenceException("Unknown ontology resource URI: "+uri);
		}
		return obj;
	}
	
	public OWLObject toObject(String expr) throws UnknownReferenceException{
		return toObject(toAlias(expr));
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
	
	public void addMapping(Alias alias, OWLObject o) throws JasdlException{
		// No! there is no reason to enforce this, and causes problems with classes defined at run time
		//if(isMapped(o)){ // can only map an entity once
		//	throw new JasdlException("Duplicate mapping on entity \""+o+"\"");
		//}
		if(isReservedKeyword(alias.getName())){
			throw new JasdlException("Cannot use reserved keyword "+alias.getName()+" as an alias");
		}		
		if(aliasToObjectMap.containsKey(alias)){
			throw new JasdlException("Ambiguous alias: "+alias);
		}
		getAgent().getLogger().finest("Added mapping: \""+alias+"\" to \""+o+"\"");
		aliasToObjectMap.put(alias, o);
		objectToAliasMap.put(o, alias);
	}
	
	public void addMapping(Alias alias, String expr) throws JasdlException{
		if(isReservedKeyword(alias.getName())){
			throw new JasdlException("Cannot use reserved keyword "+alias.getName()+" as an alias");
		}
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
				Literal l = getLiteralFactory().toLiteral(axiom);
				addAnnotations(l);				
				bels.add(l);	
			} catch (InvalidSELiteralAxiomException e) {
				// do nothing, this just means axiom is not a class or property axiom
			}
		}
		return bels;
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
	
	public Logger getLogger(){
		return getAgent().getLogger();
	}	
	
	
	/**
	 * Adds all (asserted and inferred) non-JASDL annotations to supplied literal
	 * @param l		literal to which we are adding all non-JASDL annotations
	 * @throws JasdlException
	 */
	public void addAnnotations(Literal l) throws JasdlException{
		addAssertedAnnotations(l);
		addInferredAnnotations(l);
	}
	
	/**
	 * Adds asserted annotations to the supplied literal (therefore, l must have been asserted for this to have any effect)
	 * @param l		literal to which we are adding asserted non-JASDL annotations
	 * @throws JasdlException
	 */
	public void addAssertedAnnotations(Literal l) throws JasdlException{
		Literal clone = (Literal)l.clone();
		clone.clearAnnots(); // because our hashcode mustn't rely on annotations
		ListTerm annotations = annotationMap.get(clone);
		if(annotations != null){
			l.addAnnots(annotations);
		}
	}	
	
	
	/**
	 * Adds annotations associated with all assertions that entail the axiom associated with this SE-Literal
	 * @param l	the SE-Literal which to add annotations of all entailing assertions to
	 */
	public void addInferredAnnotations(Literal l) throws JasdlException{
		Set<Literal> explanations = explain(l);
		for(Literal explanation : explanations){
			addAssertedAnnotations(explanation);
			l.importAnnots(explanation);
		}
	}
		
	/**
	 * Update the explicit annotations associated with an assertion
	 * @param l		literal to update explicit (asserted) annotations for 
	 * @throws JasdlException
	 */
	public void storeAnnotations(Literal l) throws JasdlException{
		addAssertedAnnotations(l); // make sure asserted annotations of literal are complete
		Literal clone = (Literal)l.clone();
		clone.clearAnnots(); // because our hashcode mustn't rely on annotations
		annotationMap.remove(clone);
		annotationMap.put(clone, l.getAnnots());
	}	
	
	
	
	/**
	 * Return the set all of SE-Literal representations of those (asserted) axioms that entail the axiom corresponding to the SE-Literal l.
	 * Doesn't use HSTExplantationGenerator's set of minimal explanation sets generation functionality - it is very slow (however, will probably be required for BRF!)
	 * @param l		the SE-Literal of which to return all entailing assertions
	 * @return		the set all of SE-Literal representations of those (asserted) axioms that entail the axiom corresponding to the SE-Literal l
	 * @throws JasdlException
	 */
	public Set<Literal> explain(Literal l) throws JasdlException{
		Set<Literal> explanationSet = new HashSet<Literal>();		
		List<OWLIndividualAxiom> axioms = getAxiomFactory().get(l);		
		for(OWLIndividualAxiom axiom : axioms){			
	        DebuggerDescriptionGenerator gen = new DebuggerDescriptionGenerator(agent.getManager().getOWLDataFactory());
	        axiom.accept(gen);	
	        SatisfiabilityConverter satCon = new SatisfiabilityConverter(agent.getManager().getOWLDataFactory());	        
	        OWLDescription desc = satCon.convert(axiom);	
	        BlackBoxExplanation bbexp = new BlackBoxExplanation(getAgent().getManager());	       	        
	        bbexp.setOntology(owl);
	        bbexp.setReasoner(reasoner);		       
	        bbexp.setReasonerFactory(reasonerFactory);	
	        HSTExplanationGenerator hstGen = new HSTExplanationGenerator(bbexp);	        
	        //Set<OWLAxiom> explanation = OWLReasonerAdapter.flattenSetOfSets(hstGen.getExplanations(desc));
	        Set<OWLAxiom> explanation = hstGen.getExplanation(desc);
	        for(OWLAxiom expAxiom : explanation){
	        	if(expAxiom instanceof OWLClassAssertionAxiom || expAxiom instanceof OWLPropertyAssertionAxiom || expAxiom instanceof OWLDifferentIndividualsAxiom){
	        		// we are only interested in explanations that can be converted to se-literals
	        		//TODO: are we not interested in all-different assertions? represent these as se-literals rather than internal action?
	        		Literal expL = getLiteralFactory().toLiteral((OWLIndividualAxiom)expAxiom);
	        		explanationSet.add(expL);
	        	}
	        }
	        
		}
		
		return explanationSet;
	}
	
	
	
	
}
