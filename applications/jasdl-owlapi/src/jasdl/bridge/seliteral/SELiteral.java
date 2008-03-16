package jasdl.bridge.seliteral;

import static jasdl.util.Common.strip;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;

/**
 * Extends a Jason literal to provide ontology-related functionality. There is a specialisation of this class
 * for each type of SELiteral JASDL supports.
 * @author Tom Klapiscak
 *
 */
public class SELiteral extends Literal{
	
	protected JasdlAgent agent;
	
	public static String ONTOLOGY_ANNOTATION_FUNCTOR = "o";
	public static String EXPR_ANNOTATION_FUNCTOR = "expr";
	
	protected Structure ontologyAnnotation;
	
	protected Atom ontologyLabel;
	protected OWLOntology ontology;
	

	

	/**
	 * Construct an SELiteral from an existing Literal possesing valid constructs required for semantic enrichment.
	 * Existence of referenced ontological entities IS NOT checked.
	 * Ontology annotation can either be in atomic label or string physical uri format.
	 * Novel ontologies referenced by physical URIs will be instantiated and assigned an anonymous label.
	 * @param l		a Literal possesing valid constructs required for semantic enrichment
	 * @throws JasdlException	if the literal does not possess valid constructs required for semantic enrichment
	 */
	public SELiteral(Literal l, JasdlAgent agent) throws JasdlException{
		super(l);
		this.agent = agent;
		ListTerm os = getAnnots(ONTOLOGY_ANNOTATION_FUNCTOR);
		if(os.size() == 0){
			throw new NotEnrichedException(l+" is not semantically-enriched");
		}
		InvalidSELiteralException invalid = new InvalidSELiteralException("Invalid ontology annotation on "+l);
		if(os.size() > 1){
			throw invalid;
		}
		Term t = os.get(0);
		if(!(t instanceof Structure)){
			throw invalid;
		}
		ontologyAnnotation = (Structure)t;
		if(ontologyAnnotation.getArity() != 1){
			throw invalid;
		}
		if(ontologyAnnotation.getTerm(0).isAtom()){
			ontologyLabel = (Atom)ontologyAnnotation.getTerm(0);
			ontology = agent.getLabelManager().getRight(ontologyLabel);
		}else if(ontologyAnnotation.getTerm(0).isString()){
			// Incoming SELiteral
			parseOntologyURI(ontologyAnnotation.getTerm(0).toString());			
		}else{
			throw invalid;
		}
		
		// look for expr annotation
		// Do I really want expressions to be limited within a single ontology??
		// would make things easier!
		//Structure exprAnnotation = getAnnots(EXPR_ANNOTATION_FUNCTOR);
				
		
		
		
		//if(!agent.getAliasManager().isKnownLeft( toAlias() )){
		//	throw new UnknownMappingException(l+" refers to an unknown resource");
		//}

	}
	
	private void parseOntologyURI(String _uri) throws JasdlException{
		URI uri;
		try {
			uri = new URI( strip(_uri, "\"")); // quotes stripped
		} catch (URISyntaxException e) {
			throw new InvalidSELiteralException("Invalid physical ontology URI in "+ontologyAnnotation+". Reason: "+e);
		}
		try{
			ontology = agent.getPhysicalURIManager().getLeft(uri);
			ontologyLabel = agent.getLabelManager().getLeft(ontology);
		}catch(UnknownMappingException e){
			// instantiate novel ontology
		}
	}
	
	/**
	 * Sets the ontology annotation of this SELiteral to be the fully-qualified physical namespace of the associated ontology
	 */
	public void qualifyOntologyAnnotation(){
		ontologyAnnotation.setTerm(0, new StringTermImpl(agent.getOntologyManager().getPhysicalURIForOntology(ontology).toString()));
	}
	
	/**
	 * Sets the ontology annotation of this SELiteral to be the label of the associated ontology
	 */
	public void unqualifyOntologyAnnotation(){
		ontologyAnnotation.setTerm(0, ontologyLabel);
	}	
	
	
	/**
	 * Prevents uneccessary re-processing steps
	 * @param l
	 * @throws JasdlException
	 */
	public SELiteral(SELiteral l) throws JasdlException{
		super(l);
		this.agent = l.agent;
		this.ontologyLabel = l.ontologyLabel;		
	}
		
	/**
	 * Convenience method, calls AliasFactory
	 * @return	the alias associated with this SELiteral
	 */
	public Alias toAlias(){
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
		return agent.getToAxiomConverter().create(this);
	}
	/**
	 * Convenience method, calls AxiomFactory
	 * @return
	 */
	public Set<OWLIndividualAxiom> getAxioms() throws JasdlException{
		return agent.getToAxiomConverter().retrieve(this);
	}
	
	public Atom getOntologyLabel(){
		return ontologyLabel;
	}
	
	/**
	 * Remains dynamic incase label<->ontology mapping changes
	 * @return
	 */
	public OWLOntology getOntology() throws UnknownMappingException{
		return agent.getLabelManager().getRight(ontologyLabel);
	}
	
	
	/**
	 * Placed here for convenient (varying) usage by subclasses
	 * Validates since terms are mutable
	 * @return
	 * @throws UnknownMappingException
	 */
	public OWLIndividual getOWLIndividual(int term) throws JasdlException{
		return getOWLIndividual(getTerm(term));
	}
	
	/**
	 * Placed here for convenient (varying) usage by subclasses
	 * Validates since terms are mutable
	 * @return
	 * @throws UnknownMappingException
	 */
	public OWLIndividual getOWLIndividual(Term term) throws JasdlException{	
		if(!(term instanceof Atom)){
			throw new InvalidSELiteralException(term+" must be atomic");
		}
		Atom atom = (Atom)term;
		Alias alias = AliasFactory.INSTANCE.create(atom, ontologyLabel);
		OWLIndividual i;
		try {
			i = (OWLIndividual)agent.getAliasManager().getRight(alias);
		} catch (UnknownMappingException e) {
			// Instantiate and map the individual if not known
			OWLOntology ontology = getOntology();
			// Clashes (with different types of resource) don't matter thanks to OWL1.1's punning features
			//TODO: what about clashes with individuals (different alias, same uri)
			URI uri = URI.create(ontology.getURI() + "#" + atom);
			i = agent.getOntologyManager().getOWLDataFactory().getOWLIndividual(uri);
			agent.getAliasManager().put(alias, i);
		} catch(ClassCastException e){
			throw new InvalidSELiteralException(atom+" does not refer to an individual");
		}
		
		return i;			
	}
	
	
	
}
