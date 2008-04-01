package jasdl.bridge.seliteral;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.AliasFactory;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jasdl.util.UnknownMappingException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;

/**
 * Wraps around a Jason literal to provide ontology-related functionality. Follows the "Decorator" design pattern.
 * There is a specialisation of this class for each type of SELiteral JASDL supports, each adding functionality appropriate to its type.
 * 
 * TODO: This class could be made much more efficient by storing values to avoid recalculation
 * @author Tom Klapiscak
 *
 */
public class SELiteral{
	public static String ONTOLOGY_ANNOTATION_FUNCTOR = "o";
	
	/**
	 * Should results of various operations be cached for later usage?
	 * Tentatively, yes. May cause problems however;
	 */
	private static boolean USE_CACHING = true;
	
	protected Structure ontologyAnnotation = null;
	
	protected OWLIndividualAxiom axiom = null;
	protected Set<OWLIndividualAxiom> axioms = null;
	
	
	protected JasdlAgent agent;	
	
	protected Literal literal;	

	/**
	 * Construct an SELiteral from an existing Literal possesing valid constructs required for semantic enrichment.
	 * Existence of referenced ontological entities IS NOT checked.
	 * Ontology annotation can either be in atomic label or string physical uri format.
	 * Novel ontologies referenced by physical URIs will be instantiated and assigned an anonymous label.
	 * @param l		a Literal possesing valid constructs required for semantic enrichment
	 * @throws JasdlException	if the literal does not possess valid constructs required for semantic enrichment
	 */
	public SELiteral(Literal literal, JasdlAgent agent) throws JasdlException{
		this.literal = (Literal)literal.clone(); // TODO: for some reason, without cloning the literal we have trouble with annotations on incoming propsotional content?!
		this.agent = agent;	
	}
	
	private Structure getOntologyAnnotation() throws JasdlException{
		if(ontologyAnnotation == null || !USE_CACHING){
			ListTerm os = literal.getAnnots(ONTOLOGY_ANNOTATION_FUNCTOR);
			if(os.size() == 0){
				throw new NotEnrichedException("Not semantically-enriched");
			}
			if(os.size() > 1){
				throw new InvalidSELiteralException("Multiple ontology annotations present");
			}
			Term t = os.get(0);
			if(!(t instanceof Structure)){
				throw new InvalidSELiteralException("Invalid ontology annotation term");
			}
			ontologyAnnotation = (Structure)t;
			
			if(ontologyAnnotation.getArity() != 1){
				throw new InvalidSELiteralException("Invalid ontology annotation arity");
			}
		}
		return ontologyAnnotation;
	}
	
	public OWLOntology getOntology() throws JasdlException{		
		Structure o = getOntologyAnnotation();
		if(o.getTerm(0).isStructure()){ // Checking for atomicity directly does not seem to work
			return agent.getLabelManager().getRight((Atom)o.getTerm(0));
		}else if(o.getTerm(0).isString()){
			return agent.getOntology(o.getTerm(0).toString()); // may instantiate a new ontology
		}else{
			throw new InvalidSELiteralException("Invalid ontology annotation format on "+o);
		}		
	}
	
	public Atom getOntologyLabel() throws JasdlException{
		return agent.getLabelManager().getLeft(getOntology());
	}

		
	/**
	 * Convenience method, calls AliasFactory
	 * @return	the alias associated with this SELiteral
	 */
	public Alias toAlias() throws JasdlException{
		return AliasFactory.INSTANCE.create(this);
	}
	
	/**
	 * Convenience method, calls AliasManager to retrieve the ontological object referred to by the alias representing
	 * this SELiteral. Special case is made for unmapped strongly-negated class assertions. Such SELiterals are identified by 
	 * the "~" prefix to their functor. In this case the "unegated" ontological object is obtained, complemented and mapped
	 * to the negated alias for future use.
	 * @return	 the ontological object referred to by the alias representing this SELiteral
	 * @throws UnknownMappingException
	 */
	public OWLObject toOWLObject() throws JasdlException{
		try{
			return agent.getAliasManager().getRight(this.toAlias());
		}catch(UnknownMappingException e){
			Alias alias = toAlias();
			if(alias.getFunctor().toString().startsWith("~")){								
				Atom negatedFunctor = new Atom(alias.getFunctor().toString().substring(1));
				Alias negatedAlias = AliasFactory.INSTANCE.create( negatedFunctor, alias.getLabel());
				if(negatedAlias.equals(AliasFactory.OWL_THING) || negatedAlias.equals(AliasFactory.OWL_NOTHING)){
					throw new InvalidSELiteralException("owl:thing and owl:nothing should not be negated");
				}
				OWLDescription negated = agent.getOntologyManager().getOWLDataFactory().getOWLObjectComplementOf(
						(OWLClass)agent.getAliasManager().getRight(negatedAlias));
				agent.getAliasManager().put(alias, negated);
				return negated;
			}else{
				throw e;
			}
		}		
	}
	
	/**
	 * Convenience method, calls AxiomFactory
	 * @return
	 */
	public OWLIndividualAxiom createAxiom() throws JasdlException{
		if(axiom == null || !USE_CACHING){
			axiom = agent.getToAxiomConverter().create(this);
		}
		return axiom;
	}
	/**
	 * Convenience method, calls AxiomFactory
	 * @return
	 */
	public Set<OWLIndividualAxiom> getAxioms() throws JasdlException{
		if(axioms == null || !USE_CACHING){
			axioms = agent.getToAxiomConverter().retrieve(this);
		}
		return axioms;
	}
	
	/**
	 * Placed here for convenient (varying) usage by subclasses
	 * Validates since terms are mutable
	 * @return
	 * @throws UnknownMappingException
	 */
	public OWLIndividual getOWLIndividual(int i) throws JasdlException{
		return getOWLIndividual(literal.getTerm(i));
	}
	
	/**
	 * Attempts to fetch individual from "parent" ontologies (i.e. that referred to by ontology label of enclosing SELiteral).
	 * If not present, attempts to fetch from personal ontology.
	 * If not present, instantiates in personal ontology.
	 * Placed here for convenient (varying) usage by subclasses.
	 * Validates and doesn't cache since terms are mutable.
	 * @return
	 * @throws UnknownMappingException
	 */
	public OWLIndividual getOWLIndividual(Term term) throws JasdlException{	
		Atom atom = new Atom(term.toString());
		OWLIndividual i;
		try {
			i = (OWLIndividual)agent.getAliasManager().getRight(AliasFactory.INSTANCE.create(atom, agent.getLabelManager().getLeft(getOntology())));
		} catch (UnknownMappingException e1) {
			Alias alias = AliasFactory.INSTANCE.create(atom, agent.getPersonalOntologyLabel());
			try {
				i = (OWLIndividual)agent.getAliasManager().getRight(alias);
			} catch (UnknownMappingException e2) {
				// Instantiate and map the individual if not known
				OWLOntology ontology = getOntology();
				// Clashes (with different types of resource) don't matter thanks to OWL1.1's punning features
				//TODO: what about clashes with individuals (different alias, same uri)
				URI uri = URI.create(ontology.getURI() + "#" + atom);
				i = agent.getOntologyManager().getOWLDataFactory().getOWLIndividual(uri);
				agent.getAliasManager().put(alias, i);
			}
		} catch(ClassCastException e){
			throw new InvalidSELiteralException(atom+" does not refer to an individual");
		}		
		return i;			
	}
	
	
	public Literal getLiteral(){
		return literal;
	}
		
	
	public SELiteralClassAssertion asClassAssertion() throws JasdlException{
		return new SELiteralClassAssertion(literal, agent);
	}
	public SELiteralObjectPropertyAssertion asObjectPropertyAssertion() throws JasdlException{
		return new SELiteralObjectPropertyAssertion(literal, agent);
	}		
	public SELiteralDataPropertyAssertion asDataPropertyAssertion() throws JasdlException{
		return new SELiteralDataPropertyAssertion(literal, agent);
	}	
	public SELiteralAllDifferentAssertion asAllDifferentAssertion() throws JasdlException{
		return new SELiteralAllDifferentAssertion(literal, agent);
	}	
	
	/**
	 * Returns all non-JASDL annotations to this literal
	 * @return
	 * @throws JasdlException
	 */
	public ListTerm getSemanticallyNaiveAnnotations() throws JasdlException{
		ListTerm annotsClone = (ListTerm)literal.getAnnots().clone(); // clone so as not to affect original literal
		annotsClone.remove(getOntologyAnnotation());
		return annotsClone;		
		//TODO: drop anon and named? uneccessary I think since they are isolated to architecture level.
	}
	
	
	// *** Mutators ***
	
	/**
	 * Sets the ontology annotation of this SELiteral to be the fully-qualified physical namespace of the associated ontology
	 */
	public void qualifyOntologyAnnotation() throws JasdlException{
		getOntologyAnnotation().setTerm(0, new StringTermImpl(agent.getPhysicalURIManager().getRight((getOntology())).toString()));
	}
	
	/**
	 * Sets the ontology annotation of this SELiteral to be the label of the associated ontology.
	 * Will instantiate ontology if unknown.
	 */
	public void unqualifyOntologyAnnotation() throws JasdlException{		
		getOntologyAnnotation().setTerm(0, getOntologyLabel());
	}	
	
	/**
	 * Clones the literal associated with this SELiteral, replacing its functor with the suppleid
	 * @param newFunctor	functor to replace the original functor with
	 * @return
	 */
	public void mutateFunctor(String newFunctor) throws JasdlException{
		Literal mutated = new Literal(!literal.negated(), newFunctor); // negation dealt with by ~ prefix
		mutated.addTerms(literal.getTerms());
		mutated.addAnnots(literal.getAnnots());
		literal = mutated;
	}	

	//	*************	
	
	
	
	
	public String toString(){
		return literal.toString();
	}
	
	
}
