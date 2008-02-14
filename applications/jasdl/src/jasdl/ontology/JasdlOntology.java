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
import static jasdl.util.Common.EXPR_ANNOTATION;
import static jasdl.util.Common.ONTOLOGY_ANNOTATION;
import static jasdl.util.Common.getAnnot;
import static jasdl.util.Common.getDatatypePropertyXSDDatatype;
import static jasdl.util.Common.getDefinedBy;
import static jasdl.util.Common.stripAll;
import static jasdl.util.Common.surroundedBy;
import jasdl.automap.AutomapUtils;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import joce.Joce;

import org.antlr.runtime.RecognitionException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
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
	private String label;
	
	/**
	 * Transpose mappings from ontological entitiy 'real' (local) names in the ontology to their aliases
	 */
	private HashMap<URI, Alias> realToAliasMap;
	
	/**
	 * Transpose mappings from ontological entity aliases to their 'real' (local) names in the ontology
	 */
	private HashMap<Alias, URI> aliasToRealMap;

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
	public JasdlOntology(URI physicalNs, String label, OntologyManager manager) throws JasdlException{
		this.physicalNs = physicalNs;
		this.label = label;
		this.model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		this.manager = manager;
		model.read(physicalNs.toString());
		realToAliasMap = new HashMap<URI, Alias>();
		aliasToRealMap = new HashMap<Alias, URI>();
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
			
			// and remove alias definition
			Alias alias = new DefinedAlias(classname, definedBy, ""); // expr not important
			
			URI real = toReal(alias);
	    	removeAliasMapping(alias, real);
			
		}		
		
		// we don't want pre compilation to happen here since incoming messages will already be precompiled
		// in fact, the only place this should happen is within the define_class internal action

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

    	
    	DefinedAlias alias = new DefinedAlias(classname, definedBy, expr);
    	addAliasMapping(alias, real);    	
	}

	/**
	 * Transforms logical ns ...# to ...:agname# for referring to runtime-defined classes
	 * Used only when defining classes to map its alias to its real
	 * @param definedBy
	 * @return
	 */
	private URI getLogicalNs(String definedBy){
		String base = logicalNs.toString();
		return URI.create(base.substring(0, base.length()-1)+":"+definedBy+"#");
	}
	
	/**
     * Replace full uris referring to defined classes with their expressions
     * Prepares an expression for sharing with other agents that might not know the meaning of runtme-defined terms
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
			Alias alias = toAlias(uri);
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
		
	public URI getPhysicalNs() {
		return physicalNs;
	}
	

	
	public Alias unrename(Alias alias){
		Alias clone = (Alias)alias.clone();
		clone.setName(alias.getName().substring(0, alias.getName().lastIndexOf("_")));
		return clone;
	}

	/**
	 * Adds a tranpose mapping both ways (alias->real and real->alias).
	 * If alias mapping clashes with a real, map to alias0. If some alias[0..n] already exists, map to alias[n+1]
	 * @param alias		alias to map to real
	 * @param real		real to map to alias
	 */
	public void addAliasMapping(Alias alias, URI real, boolean verbose){
		alias = ensureUnique(alias, verbose);
		aliasToRealMap.put(alias, real);
		realToAliasMap.put(real, alias);
		manager.getLogger().finest("Adding mapping: "+alias+" <-> "+real);
	}
	
	public void addAliasMapping(Alias alias, URI real){
		addAliasMapping(alias, real, false);
	}
	
	public Alias ensureUnique(Alias alias){
		return ensureUnique(alias, false);
	}
	public Alias ensureUnique(Alias alias, boolean verbose){
		String orig = alias.getName();
		Alias clone = (Alias)alias.clone();
		int n = 0;		
		while(true){
			if(!isAliasUnique(clone)){
				if(verbose) manager.getLogger().warning("Name clash on alias "+clone+"...");			
				clone.setName(orig+"_"+n);
				n++;
			}else{
				break;
			}
		}
		if(n>0 && verbose) manager.getLogger().warning("..."+clone+" now refers to "+toReal(alias));
		return clone;
	}
	
	/**
	 * Unique iff not a key in alias map and there is no resource with the same local name present
	 * @param alias
	 * @return
	 */
	public boolean isAliasUnique(Alias alias){
		// is mapped
		if(aliasToRealMap.containsKey(alias)) return false;
		// isn't mapped, but there is a resource of the same name present
		URI real = toReal(alias);
		if(model.containsResource(model.createResource(real.toString()))) return false;		
		return true;
	}
	
	/**
	 * Removes a tranpose mapping both ways (alias->real and real->alias)
	 * @param alias		alias to map to real
	 * @param real		real to map to alias
	 */
	public void removeAliasMapping(Alias alias, URI real){
		aliasToRealMap.remove(alias);
		realToAliasMap.remove(real);
		manager.getLogger().finest("Removing mapping: "+alias+" <-> "+real);
	}	
	
	/**
	 * Get the OntModel that this JasdlOntology wraps around
	 * @return	the OntModel this JasdlOntology wraps around
	 */
	public OntModel getModel(){
		return model;
	}
	
	public String getLabel(){
		return label;
	}
	
	public OntologyManager getManager(){
		return manager;
	}
	
	/**
	 * Changes the alias associated with this ontology
	 * 
	 * @param label				the alias to change to
	 * @throws JasdlException	if mapping fails (i.e. alias already in use)
	 */
	public void setLabel(String label) throws JasdlException{
		manager.mapLabelToOntology(this, label);
		this.label = label;
	}
	
	/**
	 * Gets the alias this real maps to. If none exists, real is simply returned (since real=alias in this case)
	 * @param real		real to transpose to alias
	 * @return			tranpose mapping of given real (an alias)
	 */
	public Alias toAlias(URI real){
		Alias alias = realToAliasMap.get(real);
		if(alias == null){
			alias = new Alias(real); // will be set to equal just the part after the # in real
		}
		return alias;
	}
	
	/**
	 * Gets the ns:real this alias maps to. If none exists, ns:alias is simply returned (since alias=real in this case)
	 * @param alias		alias to transpose to real
	 * @return			tranpose mapping of given alias (real)
	 */
	public URI toReal(Alias alias){
		URI real = aliasToRealMap.get(alias);
		if(real == null){ // can't be a defined class !
			real = URI.create(getLogicalNs()+alias.getName());
		}
		return real;
	}
	
	public Alias getAlias(Term _p) throws JasdlException{
		if(!(_p instanceof Structure)) throw new JasdlException("Cannot get alias of "+_p);
		Structure p = (Structure)_p;
		String classname = p.getFunctor();
		
		// check for defined by examining alias - needs to be done since defined_by annotation might be omitted on self defined class
		String definedBy = getManager().getAgentName(); // might be implicitly intended to mean the class defined by myself
		if(p.isPred()){
			String df = getDefinedBy((Pred)p);
			if(df != null){
				definedBy = df;
			}
		}
		Alias alias = new DefinedAlias(classname, definedBy);
		
		// check if such a thing exists
		URI real = aliasToRealMap.get(alias); // if defined, then there will definitely be a mapping present
		
		if(real == null){ // can't be a defined class -- may or may not be a mapping
			alias = new Alias(classname);
		}else{
			alias = realToAliasMap.get(real); // we need to get the actual instance now, since it is associated with expression
		}
		
		return alias;
	}
	
	public URI getReal(Term _p) throws JasdlException{
		if(!(_p instanceof Structure)) throw new JasdlException("Cannot get URI for "+_p);
		Structure p = (Structure)_p;		
		return toReal(getAlias(p));	
	}
	
	public Alias getAlias(Resource r){
		return toAlias(URI.create(r.getURI()));
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
		

	public Resource getNothing(){
		return model.getResource(model.getNsPrefixURI("owl")+"Nothing");
	}
	
	public Resource getThing(){
		return model.getResource(model.getNsPrefixURI("owl")+"Thing");
	}
		
	public String toString(){
		return "Ont: "+label;
	}
	

	
	
	
	public Selector toSelector(Literal l) throws JasdlException{
		Resource	s = toIndividual(l.getTerm(0));
		Property	p = null;
		RDFNode		o = null;		
		if(isClassAssertion(l)){
			p = RDF.type;
			o = toOntClass(l);
			if(o == null){
				throw new JasdlException("Undefined class "+l.getFunctor());
			}
			if(l.negated()){				
				// local name of complement class must equal the alias of its complement
				// negation of literal will be dealt with by complement detection during statement->literal translation
				// will this deal correctly with defined classes? NO, alias mapping is required for this to work
				// Solution: rename local name to something guaranteed to be unique, map, and strip upon statement->literal translation
				OntClass oc = (OntClass)o.as(OntClass.class);
				Alias alias = getAlias(oc);
				String real = oc.getNameSpace().substring(0, oc.getNameSpace().length()-1)+"_jasdl_complement_#"+alias.getName();
				o = model.getComplementClass(real);				
				if(o == null){ // we haven't yet created and mapped this complement class
					o = model.createComplementClass(real, oc);
					addAliasMapping(alias, URI.create(real)); // won't be unique
				}
			}			
		}else if(isPropertyAssertion(l)){
			if(l.negated()){
				throw new JasdlException(l+" is a strongly-negated binary SE-literal. These should not be used as they cannot yet be handled correctly by JASDL");
			}
			if(!l.getTerm(0).isGround() && l.getTerm(1).isGround()){
				throw new JasdlException("JASDL no longer supports left-unground right-ground property queries such as "+l);
			}
			p = toOntProperty(l);
			if(p == null){
				throw new JasdlException("Undefined property "+l.getFunctor());
			}
			if(p.canAs(ObjectProperty.class)){
				o = toIndividual(l.getTerm(1));
			}else if(p.canAs(DatatypeProperty.class)){
				o = toDatatypeLiteral(l.getTerm(1), getDatatypePropertyXSDDatatype((DatatypeProperty)p.as(DatatypeProperty.class)));
			}else{
				throw new JasdlException("Invalid SELiteral "+l);
			}
		}else{
			throw new JasdlException("Invalid SELiteral "+l);
		}		
		Selector sel = new SimpleSelector(s, p, o){
			public String toString(){
				return getSubject()+", "+getPredicate()+", "+getObject();
			}
		};
		return sel;
	}
	
	public Statement toStatement(Literal l) throws JasdlException{
		// re-use getSelectorFromLiteral but ensure it is ground
		Selector sel = toSelector(l);
		if(sel.getSubject() == null || sel.getPredicate() == null || sel.getObject() == null){
			throw new JasdlException("Statements must be ground. Supplied: "+sel);
		}
		return model.createStatement(sel.getSubject(), sel.getPredicate(), sel.getObject());
	}

	
	public OntClass toOntClass(Literal l) throws JasdlException{
		if(!isClassAssertion(l)) throw new JasdlException(l+" cannot be a class assertion");
		return model.getOntClass(getReal(l).toString()); // initially just a member of owl:Thing	
	}
	
	public OntProperty toOntProperty(Literal l) throws JasdlException{
		if(!isPropertyAssertion(l)) throw new JasdlException(l+" cannot be a property assertion");
		return model.getOntProperty(getReal(l).toString());	
	}	

	public Individual toIndividual(Term t) throws JasdlException{
		if(t.isGround()){
			return model.createIndividual(getReal(t).toString(), getThing()); // initially just a member of owl:Thing
		}else{
			return null;
		}
	}
	
	private com.hp.hpl.jena.rdf.model.Literal toDatatypeLiteral(Term t, XSDDatatype type) throws JasdlException{
		if(t.isGround()){
			// force strings to be surrounded by quotes
			if(type == XSDDatatype.XSDstring && !surroundedBy(t.toString(), "\"")){
				throw new JasdlException("String literals must be surrounded by quotes");
			}
			return model.createTypedLiteral(stripAll(t.toString(), "\""), type);
		}else{
			return null;
		}
	}
	
	
	
	

	
	/**
	 * Note: Statements must be ground, accordingly, this cannot create an unground literal
	 * @param s
	 * @return
	 */
	public Literal fromStatement(Statement s) throws JasdlException{
		Alias alias;
		List<Term> terms = new Vector<Term>();
		boolean sign = true;
		
		// first term will always be an individual
		terms.add(fromIndividual(s.getSubject()));

		if(isClassAssertion(s)){
			OntClass c = (OntClass)s.getObject().as(OntClass.class);
			alias = getAlias(c);			
			// check if complement class (which will result in a strongly negated literal)
			if(c.isComplementClass() && !c.equals(getThing())){ // since thing is ~nothing. Not a problem, since never retrieve ~thing since this is by definition empty (everything is a thing!)
				sign = false;
				// strip the renaming ending up with non-complemented class's alias
				try{
					alias = unrename(alias);
				}catch(StringIndexOutOfBoundsException e){
					throw new JasdlException("Mistakenly identified "+c+" as a complement class");
				}
			}
		}else if(isPropertyAssertion(s)){			
			OntProperty p = (OntProperty)s.getPredicate().as(OntProperty.class);
			// add rhs -dependent on type of property (object/datatype)
			if(isObjectPropertyAssertion(s)){				
				terms.add(fromIndividual(s.getObject()));
			}else if(isDatatypePropertyAssertion(s)){
				terms.add(fromDatatypeLiteral(s.getObject()));
			}else{
				throw new JasdlException("Cannot construct SE-literal from statement "+s);
			}
			alias = getAlias(p);
		}else{
			throw new JasdlException("Cannot construct SE-literal from statement "+s);
		}
		
		Literal l = new Literal(sign, alias.getName());
		l.addTerms(terms);
		
		// add ontology annotation
		l.addAnnot(constructOntologyAnnotation());
		
		// add defined_by annotation if necessary
		if(!alias.isBase()){
			l.addAnnot(constructDefinedByAnnotation(((DefinedAlias)alias).getDefinedBy()));
		}
		
		return l;
	}
	
	public Atom fromIndividual(RDFNode n){
		return new Atom(getAlias((Individual)n.as(Individual.class)).getName());
	}
	
	public Term fromDatatypeLiteral(RDFNode _l){
		com.hp.hpl.jena.rdf.model.Literal l = (com.hp.hpl.jena.rdf.model.Literal)_l.as(com.hp.hpl.jena.rdf.model.Literal.class);
		
		RDFDatatype datatype = l.getDatatype();
		String value = l.getValue().toString();				
		
		// surround with quotes if necessary for datatype representation in Jason
		if(datatype == XSDDatatype.XSDstring || datatype == XSDDatatype.XSDdate || datatype == XSDDatatype.XSDdateTime || datatype == XSDDatatype.XSDtime){
			//discard -T at start if time (not sure why Jena puts this in) //TODO: is this in fact the proper way of representing a time in owl?			
			if(datatype == XSDDatatype.XSDtime){
				value = value.replace("-T", "");
			}
			return new StringTermImpl(value);
		}
		
		if(datatype == XSDDatatype.XSDboolean){
			if(Boolean.parseBoolean(value)){
				return Literal.LTrue;
			}else{
				return Literal.LFalse;
			}
		}
				
		return new NumberTermImpl(value);
	}
	

	
	
	public static boolean isSELiteral(Literal l) throws JasdlException{
		Term annot = getAnnot(l, ONTOLOGY_ANNOTATION);
		if(annot == null) return false;
		return true;
	}
	
	public static boolean isClassAssertion(Literal l) throws JasdlException{
		if(!isSELiteral(l)) return false;
		if(l.getArity()!=1) return false;				// must be a unary literal
		return true;
	}
	
	public static boolean isClassAssertion(Statement s) throws JasdlException{
		if(!s.getSubject().canAs(Individual.class)) return false;
		if(!s.getPredicate().equals(RDF.type)) return false;
		if(!s.getObject().canAs(OntClass.class)) return false;
		return true;
	}
	
	public static boolean isPropertyAssertion(Literal l) throws JasdlException{
		if(!isSELiteral(l)) return false;
		if(l.getArity()!=2) return false;				// must be a binary literals
		return true;
	}
	
	public static boolean isPropertyAssertion(Statement s) throws JasdlException{
		if(!s.getPredicate().canAs(OntProperty.class)) return false;
		return true;
	}

	public static boolean isObjectPropertyAssertion(Statement s) throws JasdlException{
		if(!s.getSubject().canAs(Individual.class)) return false;
		if(!s.getPredicate().canAs(ObjectProperty.class)) return false;
		if(!s.getObject().canAs(Individual.class)) return false;
		return true;
	}
	
	public static boolean isDatatypePropertyAssertion(Statement s) throws JasdlException{
		if(!s.getSubject().canAs(Individual.class)) return false;
		if(!s.getPredicate().canAs(DatatypeProperty.class)) return false;
		if(!s.getObject().isLiteral()) return false;
		return true;
	}	
	
	
	/**
	 * Because the ontology annotation is never stored
	 * @return
	 */
	public Structure constructOntologyAnnotation(){
		Structure s = new Structure(ONTOLOGY_ANNOTATION);
		s.addTerm(new Atom(getLabel()));
		return s;
	}
	
	public static Structure constructDefinedByAnnotation(String definedBy){
		Structure s = new Structure(DEFINED_BY_ANNOTATION);
		s.addTerm(new Atom(definedBy));
		return s;
	}
	
	public static Structure constructExprAnnotation(String expr){
		Structure s = new Structure(EXPR_ANNOTATION);
		s.addTerm(new StringTermImpl(expr));
		return s;
	}
	
}
