package jasdl.asSemantics;

import static jasdl.util.Common.strip;
import jasdl.asSemantics.parsing.NSPrefixEntityChecker;
import jasdl.asSemantics.parsing.URIEntityChecker;
import jasdl.asSyntax.JasdlPlanLibrary;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bb.revision.BeliefBaseSemiRevisor;
import jasdl.bb.revision.JasdlIncisionFunction;
import jasdl.bb.revision.JasdlReasonerFactory;
import jasdl.bb.revision.TBoxAxiomKernelsetFilter;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.factory.AxiomToSELiteralConverter;
import jasdl.bridge.factory.SELiteralFactory;
import jasdl.bridge.factory.SELiteralToAxiomConverter;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.mapping.aliasing.AliasManager;
import jasdl.bridge.mapping.aliasing.AllDifferentPlaceholder;
import jasdl.bridge.mapping.aliasing.DefinitionManager;
import jasdl.bridge.mapping.aliasing.MappingStrategy;
import jasdl.bridge.mapping.label.LabelManager;
import jasdl.bridge.mapping.label.OntologyURIManager;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.DuplicateMappingException;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jasdl.util.UnknownMappingException;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import jmca.asSemantics.JmcaAgent;

import org.apache.commons.logging.impl.Log4JLogger;
import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxDescriptionParser;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.ShortFormProvider;

import uk.ac.manchester.cs.owl.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class JasdlAgent extends JmcaAgent{	
	
	
	private OWLOntologyManager ontologyManager;
	private OWLReasoner reasoner;
	private AliasManager aliasManager;
	private LabelManager labelManager;
	private SELiteralFactory seLiteralFactory;
	private AxiomToSELiteralConverter axiomToSELiteralConverter;	
	private SELiteralToAxiomConverter SELiteralToAxiomConverter;
	private OntologyURIManager logicalURIManager;
	private OntologyURIManager physicalURIManager;
	private ManchesterOWLSyntaxOWLObjectRendererImpl manchesterObjectRenderer;
	
	private ManchesterOWLSyntaxDescriptionParser manchesterNsPrefixDescriptionParser;
	private ManchesterOWLSyntaxDescriptionParser manchesterURIDescriptionParser;	
	
	
	public static String ANON_LABEL_PREFIX = "anon_label_";	
	public static String ANON_ALIAS_PREFIX = "anon_alias_";
	
	private DefinitionManager definitionManager;
	
	private List<String> knownAgentNames;
	private HashMap<Atom, Float> trustMap;
	
	private boolean beliefRevisionEnabled;
	private List<MappingStrategy> defaultMappingStrategies = JasdlConfigurator.DEFAULT_MAPPING_STRATEGIES;

	
	public JasdlAgent(){
		super();
		
		// instantiate managers
		aliasManager = new AliasManager();
		labelManager = new LabelManager();
		ontologyManager = OWLManager.createOWLOntologyManager();
		physicalURIManager = new OntologyURIManager();
		logicalURIManager = new OntologyURIManager();
		definitionManager = new DefinitionManager();
		
		seLiteralFactory = new SELiteralFactory(this);
		SELiteralToAxiomConverter = new SELiteralToAxiomConverter(this);
		axiomToSELiteralConverter = new AxiomToSELiteralConverter(this);
		
		knownAgentNames = new Vector<String>();
		trustMap = new HashMap<Atom, Float>();
		
		manchesterObjectRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		manchesterObjectRenderer.setShortFormProvider(new ShortFormProvider(){
			public void dispose() {			
			}
			public String getShortForm(OWLEntity entity) {
				return entity.getURI().toString();
			}			
		}); // we want fully qualified entity references
		
		manchesterNsPrefixDescriptionParser = new ManchesterOWLSyntaxDescriptionParser(ontologyManager.getOWLDataFactory(), new NSPrefixEntityChecker(this));
		manchesterURIDescriptionParser = new ManchesterOWLSyntaxDescriptionParser(ontologyManager.getOWLDataFactory(), new URIEntityChecker(this));
				
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
		
		TransitionSystem ts =  super.initAg(arch, bb, src, stts);	
		
		// *** The following must be performed after super.initAg since we require an agent name to be set ***	
		
		// create a personal ontology for (axioms that reference) run-time defined class
		createOntology(getPersonalOntologyLabel(), getPersonalOntologyURI(), true);		
		
		// create a "placeholder" ontology so we can safely map thing and nothing without actually loading the ontology
		createOntology(AliasFactory.OWL_THING.getLabel(), URI.create("http://www.w3.org/2002/07/owl"), false);	
		getAliasManager().put( AliasFactory.OWL_THING, getOntologyManager().getOWLDataFactory().getOWLThing());
		getAliasManager().put( AliasFactory.OWL_NOTHING, getOntologyManager().getOWLDataFactory().getOWLNothing());
		
		return ts;
	}
	
	public List<Literal> getABoxState() throws JasdlException{
		List<Literal> bels = new Vector<Literal>();		
		for(OWLOntology ontology : ontologyManager.getOntologies()){
			for(OWLIndividualAxiom axiom : ontology.getIndividualAxioms()){				
				Literal l = axiomToSELiteralConverter.convert(axiom).getLiteral();			
				bels.add(l);	
			}
		}
		return bels;
	}
	
	
	
	
	
	@Override
	public Event selectEvent(Queue<Event> events) {
		getLogger().finest("Events: "+events);
		return super.selectEvent(events);
	}





	public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i)  throws RevisionFailedException {
		// TODO: what annotations should revision contractions contain? all! (or none? - same effect)
		// No! the same. -a[x] only undermines assertions leading to a[x]!
		// if we are performing belief-revision all annotations will be gathered (shortcut - use none?) ensuring axiom will be obliterated
		// annotations never solely lead to conflicts.
		
		if(!isBeliefRevisionEnabled()){ // if experimental feature is disabled
			return super.brf(beliefToAdd, beliefToDel, i);
		}
		
		List<Literal> addList = new Vector<Literal>();
		List<Literal> removeList = new Vector<Literal>();
		
		// For efficiency reasons: no need to check beliefs to be contracted if established by BB revisor (since they are implicitly grounded)
		boolean revisionApplied = false;
		
		// just accept beliefToDel. Assumption: Deletions never lead to inconsistencies?!
		if(beliefToDel != null){		
			getLogger().finest("Revise: -"+beliefToDel);			
			removeList.add(beliefToDel);
		}else if(beliefToAdd != null){
			try{				
				getLogger().fine("Revise: +"+beliefToAdd);	
				SELiteral sl = getSELiteralFactory().construct(beliefToAdd);
				OWLAxiom axiomToAdd = sl.createAxiom();				
				BeliefBaseSemiRevisor bbrev = new BeliefBaseSemiRevisor(axiomToAdd, getOntologyManager(), new JasdlReasonerFactory(), getLogger());
				List<OWLAxiom> contractList = bbrev.revise(new TBoxAxiomKernelsetFilter(), new JasdlIncisionFunction(this, sl));
				
				// will only have reached here if new belief is accepted.
				addList.add(beliefToAdd);
				for(OWLAxiom contract : contractList){
					removeList.add(axiomToSELiteralConverter.convert((OWLIndividualAxiom)contract).getLiteral());
				}
				revisionApplied = true;
			}catch(RevisionFailedException e){
				throw e; // propagate upwards
			}catch(NotEnrichedException e){
				// can't perform DL-based belief revision on SN-Literals
				addList.add(beliefToAdd);
			}catch(Exception e){
				getLogger().warning("Error performing belief revision. Reason:");
				e.printStackTrace();
			}
		}else{
			throw new RuntimeException("Unexpected behaviour, both beliefToAdd and beliefToDel are non-null...?");
		}
		List<Literal>[] toReturn = null;
		
		
		// Need to perform removals before additions so our ontology instance never becomes inconsistent		
		for(Literal removed : removeList){
			boolean ok = false;
			if(revisionApplied){
				ok = true;
			}else{
				// we need to ground unground removals
				Unifier u = null;
	            try {
	                u = i.peek().getUnif(); // get from current intention
	            } catch (Exception e) {
	                u = new Unifier();
	            }
	            ok = believes(removed, u);
	            if(ok) removed.apply(u);
			}			
            if(ok){	
				if(getBB().remove(removed)){
					if(toReturn == null){
						toReturn = new List[2];
						toReturn[0] = new Vector<Literal>();
						toReturn[1] = new Vector<Literal>();
					}
					toReturn[1].add(removed);					
				}
            } 
		}
					

		// affect BB
		for(Literal added : addList){			
			if(getBB().add(added)){
				if(toReturn == null){
					toReturn = new List[2];
					toReturn[0] = new Vector<Literal>();
					toReturn[1] = new Vector<Literal>();
				}
				toReturn[0].add(added);
			}
		}
				

		
		
		return toReturn;
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
		OWLOntology ontology = getLogicalURIManager().getLeft(ontURI);		
		// clumsy approach, but I can't find any way of achieving this polymorphically (i.e. retrieve an OWLEntity from a URI) using OWL-API
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

	/**
	 * Convenience method to parse a string into a URI and return the associated ontology.
	 * Ontology is instantiated and assigned a unique anonymous label if unknown.
	 * @param uri
	 * @return
	 * @throws JasdlException
	 */	
	public OWLOntology getOntology(String _uri) throws JasdlException{
		URI uri;
		try {
			uri = new URI( strip(_uri, "\"")); // quotes stripped
		} catch (URISyntaxException e) {
			throw new InvalidSELiteralException("Invalid physical ontology URI "+_uri+". Reason: "+e);
		}
		return getOntology(uri);
	}
	
	/**
	 * Convenience method to return the ontology associated with a URI.
	 * Ontology is instantiated and assigned a unique anonymous label if unknown.
	 * @param uri
	 * @return
	 * @throws JasdlException
	 */
	public OWLOntology getOntology(URI uri) throws JasdlException{
		OWLOntology ontology;		
		try{
			ontology = getPhysicalURIManager().getLeft(uri);			
		}catch(UnknownMappingException e){
			// instantiate novel ontology
			// create a guaranteed unique anonymous label
			String label;
			Atom labelAtom;
			int i = 0;
			while(true){
				label = ANON_LABEL_PREFIX+i;
				labelAtom = new Atom(label);
				if(!getLabelManager().isKnownLeft(labelAtom)){
					break;
				}
				i++;
			}
			ontology = loadOntology(labelAtom, uri);
		}
		return ontology;
	}

	/**
	 * Calls loadOntology with default mapping strategies.
	 * For incoming ontologies and those with unspecified mapping strategies.
	 * @param label
	 * @param uri
	 * @return
	 * @throws JasdlException
	 */
	public OWLOntology loadOntology(Atom label, URI uri) throws JasdlException{
		return loadOntology(label, uri, defaultMappingStrategies);
	}

	/**
	 * Convenience method to instantiate an ontology, load it into reasoner and map it to a label.
	 * @param uri
	 * @throws JasdlException
	 */
	public OWLOntology loadOntology(Atom label, URI uri, List<MappingStrategy> strategies) throws JasdlException{
		try{
			OWLOntology ontology = ontologyManager.loadOntologyFromPhysicalURI(uri);
			try{
				OWLOntology alreadyKnown = logicalURIManager.getLeft(ontology.getURI());
				return alreadyKnown;
			}catch(UnknownMappingException e){}
			Set<OWLOntology> imports = ontologyManager.getImportsClosure(ontology);
			try {
				reasoner.loadOntologies(imports);
				reasoner.classify();
			} catch (OWLReasonerException e) {
				throw new JasdlException("Unable to load "+uri+". Reason: "+e);
			}						
			initOntology(ontology, label, uri, ontology.getURI(), false);  // (successfully) loaded ontologies never personal			
			applyMappingStrategies(ontology, strategies);
			getLogger().fine("Loaded ontology from "+uri+" and assigned label "+label);			
			return ontology;
		}catch(OWLOntologyCreationException e){
			getLogger().warning("Placeholder personal ontology substituted for unreachable "+uri);
			// can't load it, just create a blank (for unqualification of ontology annotations for example)	
			// personal by definition
			return createOntology(label, uri, true);	
		}		
	}
	

	
	/**
	 * Creates and fully maps a blank ontology. Used for example for "owl" ontology, containing
	 * axioms referencing "owl:thing" and "owl:nothing", and for personal ontologies containing
	 * axioms referencing run-time defined anonymous classes.
	 * @param label
	 * @param uri
	 * @throws JasdlException
	 */
	public OWLOntology createOntology(Atom label, URI uri, boolean isPersonal) throws JasdlException{
		try{
			OWLOntology ontology = getOntologyManager().createOntology( uri );
			try{
			getReasoner().loadOntologies(Collections.singleton(ontology));
			} catch (OWLReasonerException e) {
				throw new JasdlException("Unable to load "+uri+". Reason: "+e);
			}	
			initOntology(ontology, label, uri, uri, isPersonal);
			return ontology;
		} catch (OWLOntologyCreationException e) {
			throw new JasdlException("Error instantiating OWL ontology. Reason: "+e);
		}		
	}
	
	/**
	 * Common ontology initialisation functionality, sets up various mappings.
	 * @param ontology
	 * @param label
	 * @param physicalURI
	 * @param logicalURI
	 * @param isPersonal
	 * @throws JasdlException
	 */
	private void initOntology(OWLOntology ontology, Atom label, URI physicalURI, URI logicalURI, boolean isPersonal) throws JasdlException{
		labelManager.put(label, ontology, isPersonal);
		
		physicalURIManager.put(ontology, physicalURI);
		logicalURIManager.put(ontology, logicalURI);
		// create the AllDifferent placeholder entity for this ontology
		getAliasManager().put( AliasFactory.INSTANCE.all_different(label), new AllDifferentPlaceholder(label)); // must be new instance to avoid duplicate mapping exceptions
	}	
	
	/**
	 * When a duplicate mapping is encountered, an anonymous alias is created for the offending resource
	 * @param ontology
	 * @param strategies
	 * @throws JasdlException
	 */
	private void applyMappingStrategies(OWLOntology ontology, List<MappingStrategy> strategies) throws JasdlException{
		// we need to construct a reasoner specifically for this to isolate entities from just one ontology
		Reasoner reasoner = new Reasoner(getOntologyManager());			
		Set<OWLOntology> imports = getOntologyManager().getImportsClosure(ontology);
		reasoner.loadOntologies(imports);
		
		List<OWLEntity> entities = new Vector<OWLEntity>();
		entities.addAll(reasoner.getClasses());
		entities.addAll(reasoner.getProperties());
		entities.addAll(reasoner.getIndividuals());
		
		Atom label = getLabelManager().getLeft(ontology);
		
		for(OWLEntity entity : entities){
			try{
				String _functor = entity.getURI().getFragment();					
				for(MappingStrategy strategy : strategies){
					_functor = strategy.apply(_functor);
				}
				Atom functor = new Atom(_functor);				
				getAliasManager().put(AliasFactory.INSTANCE.create(functor, label), entity);
			}catch(DuplicateMappingException e){
				// generate an anonymous alias for this entity
				String functor;
				Alias anonAlias;
				int i = 0;				
				while(true){
					functor = ANON_ALIAS_PREFIX + i;
					anonAlias = AliasFactory.INSTANCE.create(new Atom(functor), label);
					if(!getAliasManager().isKnownLeft(anonAlias)){
						break;
					}
					i++;
				}
				getAliasManager().put(anonAlias, entity);
			}
		}
	}
	

	
	public OWLDescription defineClass(Atom functor, String expression, ManchesterOWLSyntaxDescriptionParser parser) throws JasdlException{
		return defineClass(functor, getPersonalOntologyLabel(), expression, parser);
	}
	
	
	public OWLDescription defineClass(Atom functor, Atom label, String expression, ManchesterOWLSyntaxDescriptionParser parser) throws JasdlException{
		Alias alias = AliasFactory.INSTANCE.create(functor, label);		
		if(getAliasManager().isKnownLeft(alias)){
			throw new DuplicateMappingException("New class definition with alias "+alias+" overlaps with an existing ontological entity");
		}	
		OWLDescription desc;
		try{
    		desc = parser.parse(expression);       
    	}catch(Exception e){    		
    		throw new JasdlException("Could not parse expression "+expression+". Reason: "+e);
    	} 
    	
    	// We need this class to be named for parsing.
    	// Create an equivalent class and add this instead with alias as fragment.
    	// Clashes shouldn't be an issue here (thanks to distinct personal ontologies).
    	OWLOntology ontology = getLabelManager().getRight(label);
    	String _uri = getLogicalURIManager().getRight(ontology).toString();
    	_uri+="#"+functor;
    	URI uri = URI.create(_uri);
    	OWLClass naming = ontologyManager.getOWLDataFactory().getOWLClass(uri);
    	OWLEquivalentClassesAxiom axiom = ontologyManager.getOWLDataFactory().getOWLEquivalentClassesAxiom(naming, desc);
    	try {
			ontologyManager.addAxioms(ontology, Collections.singleton(axiom));
		} catch (OWLOntologyChangeException e) {
			throw new JasdlException("Error adding "+uri+" naming of "+desc+" to "+label);
		}
		getLogger().fine("Adding named class "+uri+" for "+desc+" in "+label);
    	getAliasManager().put(alias, naming);  // consequence, we can no longer easily distinguish run-time defined classes from pre-defined - need to maintain a map
    	getDefinitionManager().put(naming, desc);
    	
    	refreshReasoner();
    	
    	getLogger().fine("Defined new class with alias "+alias);
    	return desc;    	
	}
	
	public void refreshReasoner() throws JasdlException{
		try {
			if(reasoner instanceof uk.ac.manchester.cs.factplusplus.owlapi.Reasoner){
				((uk.ac.manchester.cs.factplusplus.owlapi.Reasoner)reasoner).classify();
			}else if(reasoner instanceof org.mindswap.pellet.owlapi.Reasoner){
				((org.mindswap.pellet.owlapi.Reasoner)reasoner).refresh();
			}else{
				reasoner.classify();
			}
		} catch (OWLReasonerException e) {
			throw new JasdlException("Unable to refresh reasoner. Reason: "+e);
		}
	}
	
	
	public void setReasonerLogLevel(org.apache.log4j.Level level){
		if(reasoner instanceof org.mindswap.pellet.owlapi.Reasoner){
			org.mindswap.pellet.owlapi.Reasoner pellet = (org.mindswap.pellet.owlapi.Reasoner)reasoner;		
			Log4JLogger abox_logger = (Log4JLogger)pellet.getKB().getABox().log;
			abox_logger.getLogger().setLevel(level);
	
			Log4JLogger taxonomy_logger = (Log4JLogger)pellet.getKB().getTaxonomy().log;
			taxonomy_logger.getLogger().setLevel(level);
	
			Log4JLogger kb_logger = (Log4JLogger)pellet.getKB().log;
			kb_logger.getLogger().setLevel(level);
		}
	}	
	
	/**
	 * *Must* be unique within society!
	 * @return
	 */
	public String getAgentName(){
		return getTS().getUserAgArch().getAgName();
	}
	
	public List<MappingStrategy> getDefaultMappingStrategies() {
		return defaultMappingStrategies;
	}

	public void setDefaultMappingStrategies(List<MappingStrategy> defaultMappingStrategies) {
		this.defaultMappingStrategies = defaultMappingStrategies;
	}

	public Atom getPersonalOntologyLabel(){
		return new Atom("self");		
	}
	
	public URI getPersonalOntologyURI() {
		return getPersonalOntologyURI(new Atom(getAgentName()));
	}

	public URI getPersonalOntologyURI(Atom label) {
		return URI.create("http://www.dur.ac.uk/t.g.klapiscak/self"+label+".owl");
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
	
	public DefinitionManager getDefinitionManager() {
		return definitionManager;
	}

	public OWLReasoner getReasoner() {
		return reasoner;
	}
		
	public SELiteralFactory getSELiteralFactory() {
		return seLiteralFactory;
	}
	
	public SELiteralToAxiomConverter getSELiteralToAxiomConverter() {
		return SELiteralToAxiomConverter;
	}
	
	public AxiomToSELiteralConverter getAxiomToSELiteralConverter() {
		return axiomToSELiteralConverter;
	}

	public ManchesterOWLSyntaxOWLObjectRendererImpl getManchesterObjectRenderer() {
		return manchesterObjectRenderer;
	}
	
	public ManchesterOWLSyntaxDescriptionParser getManchesterNsPrefixDescriptionParser() {
		return manchesterNsPrefixDescriptionParser;
	}	
	
	public ManchesterOWLSyntaxDescriptionParser getManchesterURIDescriptionParser() {
		return manchesterURIDescriptionParser;
	}
	
	public boolean isBeliefRevisionEnabled() {
		return beliefRevisionEnabled;
	}

	public void setBeliefRevisionEnabled(boolean beliefRevisionEnabled) {
		this.beliefRevisionEnabled = beliefRevisionEnabled;
	}
	
	public OWLDataFactory getOWLDataFactory(){
		return getOntologyManager().getOWLDataFactory();
	}
	
	public void setReasoner(OWLReasoner reasoner){
		this.reasoner = reasoner;
	}	
	
	public void addKnownAgentName(String name){
		knownAgentNames.add(name);
	}
	
	public List<String> getKnownAgentNames(){
		return knownAgentNames;
	}
	
	public void setTrustRating(Atom name, float trust){
		trustMap.remove(name);
		trustMap.put(name, trust);
	}
	
	public Float getTrustRating(Atom name){
		return trustMap.get(name);
	}

	
	

}
