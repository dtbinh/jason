package jasdl.asSemantics;

import static jasdl.util.Common.DELIM;
import static jasdl.util.Common.getCurrentDir;
import static jasdl.util.Common.strip;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.mapping.aliasing.DecapitaliseMappingStrategy;
import jasdl.bridge.mapping.aliasing.MappingStrategy;
import jasdl.util.exception.JasdlConfigurationException;
import jasdl.util.exception.JasdlException;
import jason.asSyntax.Atom;
import jason.runtime.Settings;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class JasdlConfigurator {
	public static String MAS2J_PREFIX					= "jasdl";
	public static String MAS2J_ONTOLOGIES 				= "_ontologies";
	public static String MAS2J_URI						= "_uri";
	public static String MAS2J_MAPPING_STRATEGIES		= "_mapping_strategies";
	public static String MAS2J_MAPPING_MANUAL			= "_mapping_manual";
	public static String MAS2J_TRUSTRATING				= "_trustRating";
	public static String MAS2J_KNOWNAGENTS				= "_knownAgents";
	public static String MAS2J_USEBELIEFREVISION		= "_useBeliefRevision";
	public static String MAS2J_USEANNOTATIONGATHERING	= "_useAnnotationGathering";
	public static String MAS2J_REASONERCLASS			= "_reasonerClass";
	
	public static List<MappingStrategy> DEFAULT_MAPPING_STRATEGIES = Arrays.asList( new MappingStrategy[] { new DecapitaliseMappingStrategy()} );
	public static String DEFAULT_REASONER_CLASS = "org.mindswap.pellet.owlapi.Reasoner";
	public static boolean DEFAULT_USEBELIEFREVISION = false;
	public static boolean DEFAULT_USEANNOTATIONGATHERING = false;
	
	
	/**
	 * List of reserved ontology labels. Currently:
	 * <ul>
	 * 	<li> "default" - allows jasdl_default_mapping_strategies to be conveniently used to define the default mapping strategies performed
	 * 		by this agent on unknown incoming ontologies and predefined ontologies lacking mapping strategy definition</li>
	 * <li> "self" - refers to the "personal" ontology used by this agent to store axioms referencing run-time defined classes and individuals </li>
	 * </ul>
	 */
	public static List<Atom> reservedOntologyLabels = Arrays.asList( new Atom[] {new Atom("default"), new Atom("self")});
	
	
	
	
	private JasdlAgent agent;

	public JasdlConfigurator(JasdlAgent agent){
		this.agent = agent;
	}
	
	public void configure(Settings stts) throws JasdlException{
		loadReasoner(stts);
		loadDefaultMappingStrategies(stts);
		setUseBeliefRevision(stts);			
		setUseAnnotationGathering(stts);
		loadOntologies(stts);
		applyManualMappings(stts);
		loadKnownAgents(stts);
		loadTrustRatings(stts);			
	}
	
	@SuppressWarnings("unchecked")
	private void loadReasoner(Settings stts) throws JasdlException{
		String reasonerClass;
		try{
			reasonerClass = prepareUserParameter( stts, MAS2J_PREFIX + MAS2J_REASONERCLASS);
		}catch(JasdlConfigurationException e){
			reasonerClass = DEFAULT_REASONER_CLASS;
		}	
		try {
			Class cls = Class.forName(reasonerClass);
			Constructor ct = cls.getConstructor(new Class[] {OWLOntologyManager.class});
			OWLReasoner reasoner = (OWLReasoner)ct.newInstance(new Object[] {agent.getOntologyManager()});
			if(reasoner == null){
				throw new JasdlException("Unknown reasoner class: "+reasonerClass);
			}else{
				agent.setReasoner(reasoner);
				agent.setReasonerLogLevel(org.apache.log4j.Level.FATAL);
			}
		}catch (Throwable e) {
			throw new JasdlException("Error instantiating reasoner "+reasonerClass+". Reason: "+e);
		}
	}
	
	private void loadDefaultMappingStrategies(Settings stts) throws JasdlException{
		agent.setDefaultMappingStrategies(getMappingStrategies(stts, new Atom("default"))); //implication "default" is a reserved ontology label
	}
	
	private void setUseBeliefRevision(Settings stts) throws JasdlException{
		try{
			// set whether to use belief revision or not
			String useBeliefRevision = prepareUserParameter(stts, MAS2J_PREFIX + MAS2J_USEBELIEFREVISION);
			agent.setBeliefRevisionEnabled(Boolean.parseBoolean(useBeliefRevision));
		}catch(JasdlConfigurationException e){
			agent.setBeliefRevisionEnabled(DEFAULT_USEBELIEFREVISION);
		}
	}
	
	private void setUseAnnotationGathering(Settings stts) throws JasdlException{
		try{
			// set whether to use belief revision or not
			String useAnnotationGathering = prepareUserParameter(stts, MAS2J_PREFIX + MAS2J_USEANNOTATIONGATHERING);
			agent.setAnnotationGatheringEnabled(Boolean.parseBoolean(useAnnotationGathering));
		}catch(JasdlConfigurationException e){
			agent.setAnnotationGatheringEnabled(DEFAULT_USEANNOTATIONGATHERING);
		}
	}	
	
	
	/**
	 * Load ontologies as specified in .mas2j settings
	 * @param stts	.mas2j settings
	 * @throws JasdlException	if instantiation of an ontology fails
	 */
	private void loadOntologies(Settings stts) throws JasdlConfigurationException{
		String[] labels = splitUserParameter( stts, MAS2J_PREFIX + MAS2J_ONTOLOGIES );
		for(String label : labels){
			if(reservedOntologyLabels.contains(label)){
				throw new JasdlConfigurationException(label+" is a reserved ontology label");
			}			
			String _uri = prepareUserParameter( stts, MAS2J_PREFIX + "_" + label + MAS2J_URI );
			URI uri = null;
			try {
				try{
					uri = new URI(_uri);					
					if(!uri.isAbsolute()){
						throw new URISyntaxException("", "");
					}					
				}catch(URISyntaxException urie){					
					_uri = "file://"+getCurrentDir()+_uri;					
					uri = new URI(_uri); // try relative path
					agent.getLogger().fine("Loaded ontology "+_uri+" from relative uri");
				}
				agent.loadOntology(new Atom(label), uri, getMappingStrategies(stts, new Atom(label)));
			} catch (Exception e) {
				String msg = "Unable to instantiate ontology \""+_uri+"\" ("+label+")";
				agent.getLogger().log(Level.SEVERE, msg, e);
				throw new JasdlConfigurationException(msg+" - "+e);
			}			
		}
	}
	
	/**
	 * Overrides auto mappings!
	 * @param stts
	 * @throws JasdlException
	 */
	private void applyManualMappings(Settings stts) throws JasdlException{
		for(Atom label : agent.getLabelManager().getLefts()){
			try{
				String[] mappings = splitUserParameter(stts, MAS2J_PREFIX + "_" + label + MAS2J_MAPPING_MANUAL);
				for(String mapping : mappings){
					OWLOntology ontology = agent.getLabelManager().getRight(label);
					String[] split = mapping.split("=");
					if(split.length == 2){
						Alias alias = AliasFactory.INSTANCE.create(new Atom(split[0].trim()), label);
						URI uri = URI.create(ontology.getURI() + "#" + split[1].trim());
						OWLEntity entity = agent.toEntity(uri);
						
						if(agent.getAliasManager().isKnownRight(entity)){ // manual mappings override automatic ones
							agent.getAliasManager().removeByRight(entity);
						}
						
						agent.getAliasManager().put(alias, entity);
					}
				}
			}catch(JasdlConfigurationException e){
				// manual mappings are optional
			}
		}
	}
	
	private void loadKnownAgents(Settings stts) throws JasdlException{
		try{
			String[] names = (splitUserParameter(stts, MAS2J_PREFIX + MAS2J_KNOWNAGENTS));
			for(String name : names){
				agent.addKnownAgentName(name);
			}
		}catch(JasdlConfigurationException e){
			// optional parameter, default to empty			
		}
	}
	
	
	// TODO: set trust ratings for assertions predefined in ontology schema (maybe annotated with self)
	private void loadTrustRatings(Settings stts) throws JasdlException{
		// agent trusts itself completely!
		agent.setTrustRating(new Atom("self"), 1f);			
		for(String knownAgent : agent.getKnownAgentNames()){
			// load trust ratings (mandatory for known agents)
			String _trustRating = prepareUserParameter(stts, MAS2J_PREFIX + "_" + knownAgent + MAS2J_TRUSTRATING);
			Float trustRating;
			try{
				trustRating = Float.parseFloat(_trustRating);
			}catch(NumberFormatException e){
				throw new JasdlException("Invalid trust rating for "+knownAgent+". Reason: "+e);
			}
			agent.setTrustRating(new Atom(knownAgent), trustRating);
		}
	}
	

	
	
	/**
	 * Extracts mapping strategy related entries from .mas2j settings and applies to relevant ontologies.
	 * Applies mappings to all resources in an ontology according to composition of supplied strategies.
	 * @param stts	.mas2j settings
	 */
	@SuppressWarnings("unchecked")
	private List<MappingStrategy> getMappingStrategies(Settings stts, Atom label) throws JasdlException{
		List<MappingStrategy> strategies = new Vector<MappingStrategy>();
		try{
			String[] strategyNames = splitUserParameter(stts, MAS2J_PREFIX + "_" + label + MAS2J_MAPPING_STRATEGIES);
			for(String strategyName : strategyNames){
				try {
					Class cls = Class.forName(strategyName);
					Constructor ct = cls.getConstructor(new Class[] {});
					MappingStrategy strategy = (MappingStrategy)ct.newInstance(new Object[] {});
					if(strategy == null){
						throw new JasdlException("Unknown mapping strategy class: "+strategy);
					}else{
						strategies.add(strategy);
					}
				}catch (Throwable e) {
					throw new JasdlException("Error instantiating mapping strategy "+strategyName+". Reason: "+e);
				}
			}
		}catch(JasdlConfigurationException e){
			strategies = agent.getDefaultMappingStrategies(); // mapping strategies optional, use defaults if not set
		}
		return strategies;	
	}	
	
	
	
	
	
	
	
	/**
	 * Convenience method to strip quotes and trim a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to prepare
	 * @return
	 */
	private String prepareUserParameter(Settings stts, String name) throws JasdlConfigurationException{
		String p = stts.getUserParameter(name);
		if(p == null){
			throw new JasdlConfigurationException(name+" is not set!");
		}else{
			return strip(p, "\"").trim();
		}
	}

	/**
	 * Convenience method to strip quotes, trim and split (by default delimiter) a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to split
	 * @return
	 */
	private String[] splitUserParameter(Settings stts, String name, String delim) throws JasdlConfigurationException{
		String[] elems = prepareUserParameter(stts, name).split(delim);
		for(int i=0; i<elems.length; i++){
			elems[i] = strip(elems[i], "\"").trim();
		}
		return elems;
	}
	
	/**
	 * Convenience method to strip quotes, trim and split (by default delimiter) a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to split
	 * @return
	 */
	private String[] splitUserParameter(Settings stts, String name) throws JasdlConfigurationException{
		return splitUserParameter(stts, name, DELIM);
	}	
	
	
	
	
			
}
