package jasdl.asSemantics;

import jasdl.asSyntax.SELiteralFactory;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.AxiomConverter;
import jasdl.bridge.alias.AliasManager;
import jasdl.bridge.label.LabelManager;
import jasdl.util.JasdlException;
import jasdl.util.UnknownMappingException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Atom;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.net.URI;
import java.util.Set;

import jmca.asSemantics.JmcaAgent;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class JasdlAgent extends JmcaAgent {	
	private OWLOntologyManager ontologyManager;
	private Reasoner reasoner;
	private AliasManager aliasManager;
	private LabelManager labelManager;
	private SELiteralFactory seLiteralFactory;
	
	private AxiomConverter axiomConverter;
	
	public JasdlAgent(){
		super();
		// instantiate managers
		aliasManager = new AliasManager();
		labelManager = new LabelManager();
		ontologyManager = OWLManager.createOWLOntologyManager();
		seLiteralFactory = new SELiteralFactory(this);
		axiomConverter = new AxiomConverter(this);
		
		// instantiate (Pellet) reasoner
		PelletOptions.USE_TRACING = true;
		reasoner = new Reasoner(ontologyManager);
		reasoner.getKB().setDoExplanation( true );
		
	}
	
	
	
	@Override
	public TransitionSystem initAg(AgArch arch, BeliefBase bb, String src, Settings stts) throws JasonException {
		if(!(bb instanceof JasdlBeliefBase)){
			throw new JasdlException("JASDL must be used in combination with the jasdl.bb.OwlBeliefBase class");
		}
		
		((JasdlBeliefBase)bb).setAgent(this);	
		
		
		// load .mas2j JASDL configuration
		JasdlConfigurator config = new JasdlConfigurator(this);
		config.configure(stts);
		return super.initAg(arch, bb, src, stts);
	}



	/**
	 * Convenience method to instantiate an ontology, load it into reasoner and map it to a label
	 * @param uri
	 * @throws JasdlException
	 */
	public void loadOntology(Atom label, URI uri) throws JasdlException{
		try{
			OWLOntology ont = ontologyManager.loadOntologyFromPhysicalURI(uri);
			Set<OWLOntology> imports = ontologyManager.getImportsClosure(ont);
			reasoner.loadOntologies(imports);
			reasoner.classify();
			labelManager.put(label, ont);
		}catch(OWLOntologyCreationException e){
			throw new JasdlException("Error loading ontology "+uri+". Reason: "+e);
		}		
	}



	public AliasManager getAliasManager() {
		return aliasManager;
	}



	public LabelManager getLabelManager() {
		return labelManager;
	}



	public OWLOntologyManager getOntologyManager() {
		return ontologyManager;
	}



	public Reasoner getReasoner() {
		return reasoner;
	}
	
	
	
	
	
	
	public SELiteralFactory getSELiteralFactory() {
		return seLiteralFactory;
	}
	
	



	public AxiomConverter getAxiomConverter() {
		return axiomConverter;
	}



	/**
	 * (Polymorphically) create an entity from resource URI (if known).
	 * TODO: where should this sit?
	 * @param uri	URI of resource to create entity from
	 * @return		entity identified by URI
	 * @throws UnknownReferenceException	if OWLObject not known
	 */
	public OWLEntity toEntity(OWLOntology ontology, URI uri) throws UnknownMappingException{
		// clumsy approach, but I can't find any way of achieving this polymorphically (i.e. retrieve an OWLObject from a URI) using OWL-API
		OWLEntity entity;
		
		/*
		// TODO: make from uri only version once OWLOntologyManager#getOntology(URI) is fixed
		URI ns = URI.create(uri.getScheme() + uri.getSchemeSpecificPart());		
		OWLOntology ontology;
		try {
			ontology = ontologyManager.getOntology(ns);
		} catch (UnknownOWLOntologyException e) {
			throw new UnknownMappingException("Unknown ontology URI "+ns);
		}
		*/
		if(ontology.containsClassReference(uri)){
			entity = ontologyManager.getOWLDataFactory().getOWLClass(uri);
		}else if (ontology.containsObjectPropertyReference(uri)){	
			entity = ontologyManager.getOWLDataFactory().getOWLObjectProperty(uri);
		}else if (ontology.containsDataPropertyReference(uri)){	
			entity = ontologyManager.getOWLDataFactory().getOWLDataProperty(uri);
		}else if (ontology.containsIndividualReference(uri)){
			entity = ontologyManager.getOWLDataFactory().getOWLIndividual(uri);
		}else{
			throw new UnknownMappingException("Unknown ontology resource URI: "+uri);
		}
		return entity;
	}	
}
