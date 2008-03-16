package jasdl.asSemantics;

import jasdl.asSyntax.JasdlPlanLibrary;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.ToAxiomConverter;
import jasdl.bridge.ToSELiteralConverter;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.AliasFactory;
import jasdl.bridge.alias.AliasManager;
import jasdl.bridge.label.LabelManager;
import jasdl.bridge.label.OntologyURIManager;
import jasdl.bridge.seliteral.SELiteralFactory;
import jasdl.util.JasdlException;
import jasdl.util.UnknownMappingException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Atom;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import jmca.asSemantics.JmcaAgent;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxDescriptionParser;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.expression.OWLEntityChecker;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLRuntimeException;
import org.semanticweb.owl.util.ShortFormProvider;

import uk.ac.manchester.cs.owl.OWLClassImpl;
import uk.ac.manchester.cs.owl.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class JasdlAgent extends JmcaAgent {	
	private OWLOntologyManager ontologyManager;
	private Reasoner reasoner;
	private AliasManager aliasManager;
	private LabelManager labelManager;
	private SELiteralFactory seLiteralFactory;
	private ToSELiteralConverter toSELiteralConverter;	
	private ToAxiomConverter toAxiomConverter;
	private OntologyURIManager logicalURIManager;
	private OntologyURIManager physicalURIManager;
	private ManchesterOWLSyntaxOWLObjectRendererImpl manchesterObjectRenderer;
	private ManchesterOWLSyntaxDescriptionParser manchesterDescriptionParser;
	

	
	public JasdlAgent(){
		super();
		// instantiate managers
		aliasManager = new AliasManager();
		labelManager = new LabelManager();
		ontologyManager = OWLManager.createOWLOntologyManager();
		physicalURIManager = new OntologyURIManager();
		logicalURIManager = new OntologyURIManager();
		
		seLiteralFactory = new SELiteralFactory(this);
		toAxiomConverter = new ToAxiomConverter(this);
		toSELiteralConverter = new ToSELiteralConverter(this);
		
		manchesterObjectRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		manchesterObjectRenderer.setShortFormProvider(new ShortFormProvider(){
			public void dispose() {			
			}
			public String getShortForm(OWLEntity entity) {
				return entity.getURI().toString();
			}			
		}); // we want fully qualified entity references
		
		manchesterDescriptionParser = new ManchesterOWLSyntaxDescriptionParser(ontologyManager.getOWLDataFactory(), new NSPrefixEntityChecker(this));
		
		
		// instantiate (Pellet) reasoner
		PelletOptions.USE_TRACING = true;
		reasoner = new Reasoner(ontologyManager);
		reasoner.getKB().setDoExplanation( true );
		
		// override plan library
		setPL( new JasdlPlanLibrary(this) );
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
			OWLOntology ontology = ontologyManager.loadOntologyFromPhysicalURI(uri);
			Set<OWLOntology> imports = ontologyManager.getImportsClosure(ontology);
			reasoner.loadOntologies(imports);
			reasoner.classify();
			labelManager.put(label, ontology);
			physicalURIManager.put(ontology, uri);
			logicalURIManager.put(ontology, ontology.getURI());
		}catch(OWLOntologyCreationException e){
			throw new JasdlException("Error loading ontology "+uri+". Reason: "+e);
		}		
	}
	
	
	public void createOntology(Atom label, URI uri) throws JasdlException{
		try{
			OWLOntology ontology = getOntologyManager().createOntology( uri );
			getLabelManager().put(label, ontology);
			getPhysicalURIManager().put(ontology, uri);
			getLogicalURIManager().put(ontology, uri);
			getReasoner().loadOntology(ontology);			
		} catch (OWLOntologyCreationException e) {
			throw new JasdlException("Error instantiating OWL ontology. Reason: "+e);
		}		
	}
	
	public Atom getPersonalOntologyLabel(){
		return new Atom("self");		
	}
	


	public URI getPersonalOntologyURI() {
		return URI.create("http://www.dur.ac.uk/t.g.klapiscak/"+getPersonalOntologyLabel()+".owl");
	}	
	
	
	
	public OWLDescription defineClass(Atom functor, String expression) throws JasdlException{
		OWLDescription desc;
		try{
    		desc = getManchesterDescriptionParser().parse(expression);       
    	}catch(Exception e){
    		e.printStackTrace();
    		throw new JasdlException("Could not parse expression "+expression+". Reason: "+e);
    	}
    	
    	Alias alias = AliasFactory.INSTANCE.create(functor, getPersonalOntologyLabel());
    	getAliasManager().put(alias, desc); 
    	
    	return desc;    	
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
	
	



	public OntologyURIManager getPhysicalURIManager() {
		return physicalURIManager;
	}

	
	

	public OntologyURIManager getLogicalURIManager() {
		return logicalURIManager;
	}



	public Reasoner getReasoner() {
		return reasoner;
	}
	
	
	
	
	
	
	public SELiteralFactory getSELiteralFactory() {
		return seLiteralFactory;
	}
	
	



	public ToAxiomConverter getToAxiomConverter() {
		return toAxiomConverter;
	}
	
	



	public ToSELiteralConverter getToSELiteralConverter() {
		return toSELiteralConverter;
	}



	public ManchesterOWLSyntaxOWLObjectRendererImpl getManchesterObjectRenderer() {
		return manchesterObjectRenderer;
	}



	public ManchesterOWLSyntaxDescriptionParser getManchesterDescriptionParser() {
		return manchesterDescriptionParser;
	}
	
	
	/**
	 * Convenience method to (polymorphically) create an entity from resource URI (if known).
	 * TODO: where should this sit?
	 * @param uri	URI of resource to create entity from
	 * @return		entity identified by URI
	 * @throws UnknownReferenceException	if OWLObject not known
	 */
	public OWLEntity toEntity(URI uri) throws JasdlException{
		URI ontURI;
		try {
			ontURI = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
		} catch (URISyntaxException e) {
			throw new JasdlException("Invalid entity URI "+uri);
		}
		OWLOntology ontology = getLogicalURIManager().getLeft(ontURI);;
		
		// clumsy approach, but I can't find any way of achieving this polymorphically (i.e. retrieve an OWLObject from a URI) using OWL-API
		OWLEntity entity;
		if(ontology.containsClassReference(uri)){
			entity = getOntologyManager().getOWLDataFactory().getOWLClass(uri);
		}else if (ontology.containsObjectPropertyReference(uri)){	
			entity = getOntologyManager().getOWLDataFactory().getOWLObjectProperty(uri);
		}else if (ontology.containsDataPropertyReference(uri)){	
			entity = getOntologyManager().getOWLDataFactory().getOWLDataProperty(uri);
		}else if (ontology.containsIndividualReference(uri)){
			entity = getOntologyManager().getOWLDataFactory().getOWLIndividual(uri);
		}else{
			throw new UnknownMappingException("Unknown ontology resource URI: "+uri);
		}
		return entity;
	}	
	
	
	
	
	private class NSPrefixEntityChecker implements OWLEntityChecker{
		
		private JasdlAgent agent;

		public NSPrefixEntityChecker(JasdlAgent agent){
			this.agent = agent;
		}

		public OWLClass getOWLClass(String name) {			
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLClass()){
				return entity.asOWLClass();
			}else{
				return null;
			}
		}

		public OWLDataProperty getOWLDataProperty(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLDataProperty()){
				return entity.asOWLDataProperty();
			}else{
				return null;
			}
		}

		public OWLDataType getOWLDataType(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLDataType()){
				return entity.asOWLDataType();
			}else{
				return null;
			}
		}

		public OWLIndividual getOWLIndividual(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLIndividual()){
				return entity.asOWLIndividual();
			}else{
				return null;
			}
		}

		public OWLObjectProperty getOWLObjectProperty(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLObjectProperty()){
				return entity.asOWLObjectProperty();
			}else{
				return null;
			}
		}
		
		private OWLEntity convert(String name){
			String[] tokens = name.split(":");
			try {
				Atom functor = new Atom(tokens[1]);
				Atom label = new Atom(tokens[0]);
				Alias alias = AliasFactory.INSTANCE.create(functor, label);
				return (OWLEntity)agent.getAliasManager().getRight(alias); // guaranteed to be an entity? Not for anonymous classes!
			} catch (Exception e) {
				return null;
			}			
		}
		
		
	}






}
