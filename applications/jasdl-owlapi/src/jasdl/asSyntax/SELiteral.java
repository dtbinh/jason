package jasdl.asSyntax;

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
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntology;


public class SELiteral extends Literal{
	
	protected JasdlAgent agent;
	
	private static String ONTOLOGY_ANNOTATION_FUNCTOR = "o";
	
	protected Atom ontologyLabel;

	/**
	 * Construct an SELiteral from an existing Literal possesing valid constructs required for semantic enrichment
	 * Existence of referenced ontological entities IS checked.
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
		Structure s = (Structure)t;
		if(s.getArity() != 1){
			throw invalid;
		}
		if(!s.getTerm(0).isAtom()){
			throw invalid;
		}
		ontologyLabel = (Atom)s.getTerm(0);
		
		
		
		
		if(!agent.getAliasManager().isKnown( toAlias() )){
			throw new UnknownMappingException(l+" refers to an unknown resource");
		}

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
	 * Convenience method, calls AliasManager
	 * @return
	 * @throws UnknownMappingException
	 */
	public OWLEntity toEntity() throws UnknownMappingException{
		return agent.getAliasManager().get(this.toAlias());
	}
	
	/**
	 * Convenience method, calls AxiomFactory
	 * @return
	 */
	public OWLIndividualAxiom createAxiom() throws JasdlException{
		return agent.getAxiomConverter().convert(this);
	}
	/**
	 * Convenience method, calls AxiomFactory
	 * @return
	 */
	public Set<OWLIndividualAxiom> getAxioms() throws JasdlException{
		return agent.getAxiomConverter().convertAndCheck(this);
	}
	
	public Atom getOntologyLabel(){
		return ontologyLabel;
	}
	
	/**
	 * Remains dynamic incase label<->ontology mapping changes
	 * @return
	 */
	public OWLOntology getOntology() throws UnknownMappingException{
		return agent.getLabelManager().get(ontologyLabel);
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
			i = (OWLIndividual)agent.getAliasManager().get(alias);
		} catch (UnknownMappingException e) {
			// Instantiate and map the individual if not known
			OWLOntology ontology = agent.getLabelManager().get(ontologyLabel);
			// Clashes (with different types of resource) don't matter thanks to OWL1.1's punning features
			//TODO: what about clashes with individuals (different alias, same uri)
			URI uri = URI.create(ontology.getURI() + "#" + atom);
			i = agent.getOntologyManager().getOWLDataFactory().getOWLIndividual(uri);
		} catch(ClassCastException e){
			throw new InvalidSELiteralException(atom+" does not refer to an individual");
		}
		
		return i;			
	}
	
}
