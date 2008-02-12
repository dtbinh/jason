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
import static jasdl.util.Common.DEFINED_BY_ANNOTATION;
import static jasdl.util.Common.DOMAIN;
import static jasdl.util.Common.ONTOLOGY_ANNOTATION;
import static jasdl.util.Common.RANGE;
import static jasdl.util.Common.getAnnot;
import static jasdl.util.Common.getDatatypePropertyXSDDatatype;
import static jasdl.util.Common.strip;
import static jasdl.util.Common.surroundedBy;
import static jasdl.util.Common.termIsDirect;
import jasdl.automap.AutomapUtils;
import jasdl.util.JasdlException;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import joce.Joce;

import org.antlr.runtime.RecognitionException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Wraps around OntModel adding additional Jasdl specific information and functionality
 * 
 * Defined classes cannot have [direct] annotation - wouldn't make sense since they are implicitly direct
 * 
 * @author Tom Klapiscak
 *
 */
public class JasdlOntology{
	
	
	/**
	 * the namespace URI associated with entities defined in this ontology, representing this ontology's 'logical' namespace (as opposed to 'physical').
	 */  
	private URI logicalNs;
	
	/**
	 * physical namespace (i.e. the location) of the .owl file containing the (initial) definitions of this ontology
	 */
	private URI physicalNs;
	
	/**
	 * The OntModel this JasdlOntology wraps around. Used for, amongst other things, ontology manipulation and reasoning.
	 */
	private OntModel model;
	
	/**
	 * The alias used to refer to this ontology from Jason
	 */
	private String alias;
	
	/**
	 * Transpose mappings from ontological entitiy 'real' (local) names in the ontology to their aliases
	 */
	private HashMap<URI, Alias> realToAliasTransposeMap;
	
	/**
	 * Transpose mappings from ontological entity aliases to their 'real' (local) names in the ontology
	 */
	private HashMap<Alias, URI> aliasToRealTransposeMap;

	private OntologyManager manager;
	
	/**
	 * Maps classes defined at runtime (using jasdl.ia.define_class) to their class expression
	 * Used to set the expr annotation when sending messages whose propositional content contains reference to a runtime defined class
	 */
	
	/**
	 * Use OntologyManager.createJasdlOntology instead
	 * 
	 * @param physicalNs
	 * @param alias
	 * @see jasdl.ontology.OntologyManager#createJasdlOntology(URI, String)
	 */
	public JasdlOntology(URI physicalNs, String alias, OntologyManager manager) throws JasdlException{
		this.physicalNs = physicalNs;
		this.alias = alias;
		this.model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		this.manager = manager;
		model.read(physicalNs.toString());
		realToAliasTransposeMap = new HashMap<URI, Alias>();
		aliasToRealTransposeMap = new HashMap<Alias, URI>();
		logicalNs = URI.create(getModel().getNsPrefixURI(""));
	}
	
	/**
	 * Defines a class at runtime, affects only in-memory TBox
	 * Class is associated with this ontology's logical namespace automatically
	 * 
	 * @param classname					the alias to be used to refer to this class
	 * @param expr						a protege-style class expression to be parsed (precompiled - refers to |uris|)
	 * @throws RecognitionException		if expression is invalid
	 */
	public void defineClass(String classname, String expr, String definedBy) throws JasdlException{
		
		
		
		 // prevents undesirable (but not fatal) name overlaps with underlying ontology
		if(model.containsResource(model.getResource(getLogicalNs()+classname))){
			throw new JasdlException("Resource with name "+classname+" already exists in "+this);
		}		
		// TODO: Do I really want it to redefine every time? throw exception here? Consider incoming messages!
		Resource res = model.getResource(getLogicalNs(definedBy)+classname);
		if(model.containsResource(res)){
			manager.getLogger().finest("Defined class name clash: "+res);
			if(!res.canAs(OntClass.class)){
				throw new JasdlException("User defined class "+res+" clashes with a non class!");
			}
			OntClass cls = (OntClass)res.as(OntClass.class);
			cls.remove();
			
//			 and remove alias definition
			Alias alias = new DefinedAlias(classname, definedBy, ""); // expr not important
			
			URI real = transposeToReal(alias);
	    	removeTransposeMapping(alias, real);
			
		}
		
		
		// we don't want pre compilation to happen here since incoming messages will already be precompiled
		// in fact, the only place this should happen is within the define_class internal action
		//expr = precompileExpression(expr);
		
		//logger.warn("Expression precompiled successfully to "+expr);
		OntClass cls;
		try {
			cls = Joce.parse(expr, model);
		} catch (RecognitionException e) {
			throw new JasdlException("Invalid class expression. Reason: "+e);
		}		
		
		if(cls.hasSuperClass(getNothing())){
			 // if this expression has nothing as a superclass then it is inconsistent - reject
			throw new JasdlException("Inconsistent class expression: "+expr);
		}
	
		URI real = URI.create(getLogicalNs(definedBy)+classname); // notice logical namespace transformation
    	model.createClass(real.toString()).addEquivalentClass(cls);   
    	
    	manager.getLogger().finest("Defining class: "+classname+" (defined by "+definedBy+") as "+expr+" with URI "+real);
    	
    	expr = postcompileExpression(expr); // post compilation won't hurt on incoming messages, and might help in future if 'correspodence' is created between two aquianted agents
    	
    	//logger.warn("Expression postcompiled successfully to "+expr);
    	
    	DefinedAlias alias = new DefinedAlias(classname, definedBy, expr);
    	addTransposeMapping(alias, real);
    	
	}
    /**
     * Replace full uris referring to defined classes with their expressions
     * Prepares an expression for sharing with other agents that might not know the meaning of runtme-defined terms
     * 
     * Split by |, take evens, tranpose uris to alias, if defined then replace with expression.
     * By induction, all expressions will be in postcompiled form
     * @param expr
     * @return
     */
    private String postcompileExpression(String expr){
    	String newExpr = expr;
		String[] tokens = expr.split("[|]");
		for(int i=1; i<tokens.length; i+=2){
			URI uri = URI.create(tokens[i]);			
			Alias alias = transposeToAlias(uri);
			if(alias instanceof DefinedAlias){
				newExpr = newExpr.replace("|"+uri.toString()+"|", "("+((DefinedAlias)alias).getExpr()+")");
			}
		}
		return newExpr;
    }	
	
	/**
	 * Convenience method to perform automaps on this ontology. Simply calls Automap.performAutomaps on itself.
	 * @param list
	 * @throws JasdlException
	 */
	public void performAutomaps(String list) throws JasdlException{
		AutomapUtils.performAutomaps(this, list);
	}
	
	
	/**
	 * Is the associated model consistent?
	 * @return	true if consistent else false
	 */
	public boolean isConsistent(){
		return model.validate().isValid();
	}
	
	/**
	 * Gets the namespace URI associated with entities defined in this ontology, representing this ontology's 'logical' namespace (as opposed to 'physical').
	 * 
	 * @return
	 */
	public URI getLogicalNs(){
		return logicalNs;
	}
	
	/**
	 * Transforms logical ns ...# to ...:agname# for referring to runtime-defined classes
	 * @param definedBy
	 * @return
	 */
	private URI getLogicalNs(String definedBy){
		String base = logicalNs.toString();
		return URI.create(base.substring(0, base.length()-1)+":"+definedBy+"#");
	}
	
		
	public URI getPhysicalNs() {
		return physicalNs;
	}

	/**
	 * Adds a tranpose mapping both ways (alias->real and real->alias)
	 * @param alias		alias to map to real
	 * @param real		real to map to alias
	 */
	public void addTransposeMapping(Alias alias, URI real){
		aliasToRealTransposeMap.put(alias, real);
		realToAliasTransposeMap.put(real, alias);
		manager.getLogger().finest("Adding mapping: "+alias+" <-> "+real);
	}
	
	/**
	 * Removes a tranpose mapping both ways (alias->real and real->alias)
	 * @param alias		alias to map to real
	 * @param real		real to map to alias
	 */
	public void removeTransposeMapping(Alias alias, URI real){
		aliasToRealTransposeMap.remove(alias);
		realToAliasTransposeMap.remove(real);
		manager.getLogger().finest("Removing mapping: "+alias+" <-> "+real);
	}	
	
	/**
	 * Get the OntModel that this JasdlOntology wraps around
	 * @return	the OntModel this JasdlOntology wraps around
	 */
	public OntModel getModel(){
		return model;
	}
	
	public String getAlias(){
		return alias;
	}
	
	public OntologyManager getManager(){
		return manager;
	}
	
	/**
	 * Changes the alias associated with this ontology
	 * 
	 * @param alias				the alias to change to
	 * @throws JasdlException	if mapping fails (i.e. alias already in use)
	 */
	public void setAlias(String alias) throws JasdlException{
		manager.mapAliasToOntology(this, alias);
		this.alias = alias;
	}
	
	/**
	 * Gets the alias this real maps to. If none exists, real is simply returned (since real=alias in this case)
	 * @param real		real to transpose to alias
	 * @return			tranpose mapping of given real (an alias)
	 */
	public Alias transposeToAlias(URI real){
		Alias alias = realToAliasTransposeMap.get(real);
		if(alias == null){
			alias = new Alias(real);
		}
		return alias;
	}
	
	/**
	 * Gets the ns:real this alias maps to. If none exists, ns:alias is simply returned (since alias=real in this case)
	 * @param alias		alias to transpose to real
	 * @return			tranpose mapping of given alias (real)
	 */
	public URI transposeToReal(Alias alias){
		URI real = aliasToRealTransposeMap.get(alias);
		if(real == null){ // can't be a defined class !
			real = URI.create(getLogicalNs()+alias.getName());
		}
		return real;
	}

	/**
	 * Gets the contents of the defined_by annotation, or this agent's name if none present
	 * @param p
	 * @return
	 * @throws JasdlException
	 */
	public String getDefinedBy(Pred p) throws JasdlException{
		String definedBy = null;
		Term _definedBy = getAnnot(p, DEFINED_BY_ANNOTATION);
		if(_definedBy == null){
			definedBy = manager.getAgentName();
		}else{
			definedBy = ((Structure)_definedBy).getTerm(0).toString();
		}
		return definedBy;
	}	
	
	public Alias getAliasFromPred(Pred p) throws JasdlException{
		String classname = p.getFunctor();
		
		// check for defined by examining alias
		String definedBy = getDefinedBy(p);
		Alias alias = new DefinedAlias(classname, definedBy);
		
		// check if such a thing exists
		URI real = aliasToRealTransposeMap.get(alias); // if defined, then there will definitely be a mapping present
		
		if(real == null){ // can't be a defined class -- may or may not be a mapping
			alias = new Alias(classname);
		}else{
			alias = realToAliasTransposeMap.get(real); // we need to get the actual instance now, since it is associated with expression
		}
		
		return alias;
	}
	
	public URI getRealFromPred(Pred p) throws JasdlException{
		URI real = null;
		if(termIsDirect(p)){ // direct, just ns:functor
			real = URI.create(getLogicalNs() + p.getFunctor());
		}else{
			real = transposeToReal(getAliasFromPred(p));			
		}	
		return real;	
	}
	
	/**
	 * Convenience method for polymorphically returning the parents of a resource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List listOntResourceParents(OntResource res, boolean direct) throws JasdlException{
		if(res == null){
			throw new JasdlException("Unable to list parents of a non-existent resource");
		}
		if(res.canAs(OntProperty.class)){
			return res.asProperty().listSuperProperties(direct).toList();
		}else if(res.canAs(OntClass.class)){
			return res.asClass().listSuperClasses(direct).toList();
		}else{
			throw new JasdlException("Unable to list parents of resource "+res+" since it is of the wrong type");
		}
	}	
	
	/**
	 * Convenience method for polymorphically returning the children of a resource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List listOntResourceChildren(OntResource res, boolean direct) throws JasdlException{
		if(res == null){
			throw new JasdlException("Unable to list parents of a non-existent resource");
		}
		if(res.canAs(OntProperty.class)){
			return res.asProperty().listSubProperties(direct).toList();
		}else if(res.canAs(OntClass.class)){
			return res.asClass().listSubClasses(direct).toList();
		}else{
			throw new JasdlException("Unable to list parents of resource "+res+" since it is of the wrong type");
		}
	}		
	
	/* Various methods for translating Jason entities to ontological entities*/

	/**
	 * For Jason->Ontology translation
	 * 
	 * Must be a pred, since we need to check for defined_by annotation
	 */
	public OntResource getOntResourceFromPred(Pred s) throws JasdlException{	
		URI real = getRealFromPred(s);	
		return getModel().createOntResource(real.toString());		
	}
	
	/**
	 * Ensures p refers to an OntClass and is unary
	 * Complements resultant if p is strongly negated.
	 * 
	 * 
	 * TODO: what about unground defined_by annotations?
	 * @param p
	 * @return
	 * @throws JasdlException
	 */
	public OntClass getOntClassFromPred(Pred p) throws JasdlException{
		if(p.getArity()!=1){
			throw new JasdlException(p+" cannot refer to a class since it is not unary");
		}
		OntResource res = getOntResourceFromPred(p);
		if(!res.isClass()){
			throw new JasdlException(res+" does not refer to a class");
		}
		OntClass c = res.asClass();
		
		// we need to generate a unique (for this complement class) uri that ends with transposed name
		// TODO: best way for this is perhaps another type of Alias: ComplementAlias
		Alias alias = transposeToAlias(URI.create(c.getURI()));
		
		String ns = c.getURI().toString();
		ns = ns.substring(0, ns.lastIndexOf("#")); // trim to last #
		ns += ":complement#"+alias.getName();
		
		
		if(p.isLiteral() && ((Literal)p).negated()){
			c = res.getOntModel().createComplementClass(ns, c);//TODO: not entirely sure why I require the tilde here. Where is the first character being stripped?!
		}
		return c;
	}
	
	/**
	 * Ensures p refers to an OntProperty and is binary
	 * TODO: negated properties? - waiting for OWL 1.1 support in Jena
	 * @param p
	 * @return
	 * @throws JasdlException
	 */
	public OntProperty getOntPropertyFromPred(Pred p) throws JasdlException{
		if(p.getArity()!=2){
			throw new JasdlException(p+" cannot refer to a class since it is not unary");
		}
		OntResource res = getOntResourceFromPred(p);
		if(!res.canAs(OntProperty.class)){
			throw new JasdlException(p+" does not refer to a property");
		}
		// property negation belongs here come OWL 1.1
		return res.asProperty();
	}
	
	/**
	 * Like getOntResourceFromStructure except unground structure results in null and structure must have an arity 0 
	 * Will return null (representing a wildcard for statement searching) if structure is not ground
	 * Will create individual if doesn't exist.
	 * For Jason->Ontology translation
	 * 
	 * @param s
	 * @return an OntResource if s is ground, else null (representing a wildcard for statement searching)
	 * @throws JasdlException
	 */
	public Resource getIndividualFromPred(Pred s) throws JasdlException{
		if(!s.isGround()){
			return null;
		}
		if(s.getArity()!=0){
			throw new JasdlException(s+" cannot refer to an individual since individuals must be represented as structures of arity 0");
		}
		return getOntResourceFromPred(s);
	}	
	
	/**
	 * Retrieves a typed literal given a term and a type.
	 * For Jason->Ontology translation
	 * @param ont
	 * @param term
	 * @param type
	 * @return
	 * @throws JasdlException
	 */
	public com.hp.hpl.jena.rdf.model.Literal getTypedLiteralFromTerm(Term term, XSDDatatype type) throws JasdlException {
		if(!term.isGround()){
			return null;
		}
		try{
			// force strings to be surrounded by quotes
			if(type == XSDDatatype.XSDstring && !surroundedBy(term.toString(), "\"")){
				throw new DatatypeFormatException("String literals must be surrounded by quotes");
			}
			return getModel().createTypedLiteral((Object)strip(term.toString(), "\""), type);
		}catch(DatatypeFormatException e){
			throw new JasdlException(term+" cannot refer to a typed literal of type "+type+". Reason: "+e);
		}
	}
	
	
	
	/**
	 * Constructs a (subject, predicate, object) triple representing a statement that can be used to reference the resource referred to by the supplied predicate
	 * For Jason-> Ontology translation
	 * 
	 * Behaviour branches based on arity of supplied literal
	 * - arity 1 (unary/class assertion) results in triples of the form (base:individual, rdf:type, base:class)
	 * - arity 2 (binary/object or datatype property assertion) results in triples of the form (base:individual, base:property, base:individual/xsd:literal)
	 * 
	 * Can't use Statement since it does not permit null values	 
	 * Individuals created either side of Object/Datatype property if they don't exist
	 * @param uri
	 * @param l
	 * @return
	 * @throws JasdlException
	 */	
	public StatementTriple constructTripleFromPred(Pred p) throws JasdlException{		
		int arity = p.getArity();				
		Resource subject = getIndividualFromPred((Pred)p.getTerm(DOMAIN));// subject if always first argument regardless of whether this pred is a class, object property or datatype property assertion		
		Property predicate;	
		RDFNode object;
		switch(arity){
		case 1:			
			predicate = RDF.type;
			object = getOntClassFromPred(p);
			if(object == null){				
				throw new JasdlException("Undefined class: "+object); //TODO: perhaps just instantiate under owl:Thing?
			}
			break;			
		case 2:
			predicate = getOntPropertyFromPred(p);	
			if(predicate == null){
				throw new JasdlException("Undefined property: "+predicate);
			}
			object = null;
			if(((OntProperty)predicate).isDatatypeProperty()){
				XSDDatatype type = getDatatypePropertyXSDDatatype((OntProperty)predicate);
				object = getTypedLiteralFromTerm(p.getTerm(RANGE), type);
			}else{
				object = getIndividualFromPred((Pred)p.getTerm(RANGE));
			}
			break;
			
		default:
			throw new JasdlException("Invalid semantically-enriched predicate: "+p);//should never happen
		}
		return new StatementTriple(subject, predicate, object);
	}
	
	/*
	 * To correspond to exact semantics of Jason's bb.getRelevant method
	public StatementTriple constructTripleFromPred2(Pred p) throws JasdlException{		
		int arity = p.getArity();				
		Resource subject = getIndividualFromPred((Pred)p.getTerm(DOMAIN));// subject if always first argument regardless of whether this pred is a class, object property or datatype property assertion		
		Property predicate;	
		RDFNode object;
		switch(arity){
		case 1:			
			predicate = RDF.type;
			object = getOntClassFromPred(p);
			if(object == null){				
				throw new JasdlException("Undefined class: "+object); //TODO: perhaps just instantiate under owl:Thing?
			}
			subject = null;
			break;			
		case 2:
			predicate = getOntPropertyFromPred(p);	
			if(predicate == null){
				throw new JasdlException("Undefined property: "+predicate);
			}
			object = null;
			if(((OntProperty)predicate).isDatatypeProperty()){
				XSDDatatype type = getDatatypePropertyXSDDatatype((OntProperty)predicate);
				object = getTypedLiteralFromTerm(p.getTerm(RANGE), type);
			}else{
				object = getIndividualFromPred((Pred)p.getTerm(RANGE));
			}			
			
			subject = null;
			object = null;
			break;
			
		default:
			throw new JasdlException("Invalid semantically-enriched predicate: "+p);//should never happen
		}
		return new StatementTriple(subject, predicate, object);
	}
	*/	
	
	/* Various methods for translating ontological entities to Jason entities*/
	
	/**
	 * Create a 0-ary literal from a resource.
	 * e.g. just the functor of a class assertion
	 */
	public Literal constructLiteralFromResource(OntResource res, boolean negated){
		Literal l = null;
		if(res instanceof OntClass || res instanceof OntProperty){
			Alias alias = transposeToAlias(URI.create(res.getURI()));
			l = new Literal(!negated, alias.getName());
			if(!alias.isBase()){
				l.addAnnot( Literal.parseLiteral(DEFINED_BY_ANNOTATION+"("+((DefinedAlias)alias).getDefinedBy()+")"));
			}
			
			// add ontology annotation
			((Pred)l).addAnnot( Literal.parseLiteral(constructAnnotation()) );
		}else if(res instanceof Individual){
			Alias alias = transposeToAlias(URI.create(res.getURI()));
			l = Literal.parseLiteral(alias.getName());
		}
		//perhaps add direct annotation here?
		return l;
	}
	
	
	
	public String constructAnnotation(){
		String annot = ONTOLOGY_ANNOTATION+"(";
		annot+=getAlias();
		annot+=")]";
		return annot;
	}
	
	
	public Resource getNothing(){
		return model.getResource(model.getNsPrefixURI("owl")+"Nothing");
	}
	
	public Resource getThing(){
		return model.getResource(model.getNsPrefixURI("owl")+"Thing");
	}
	
	
	public String toString(){
		return "Ont: "+alias;
	}
	
}
