package jasdl.asSemantics;

import static jasdl.util.Common.DELIM;
import static jasdl.util.Common.strip;
import jasdl.bridge.AllDifferentPlaceholder;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.AliasFactory;
import jasdl.bridge.alias.MappingStrategy;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;
import jason.runtime.Settings;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;

public class JasdlConfigurator {
	private static String MAS2J_PREFIX					= "jasdl";
	private static String MAS2J_ONTOLOGIES 				= "_ontologies";
	private static String MAS2J_URI						= "_uri";
	private static String MAS2J_MAPPING_STRATEGIES		= "_mapping_strategies";
	private static String MAS2J_MAPPING_MANUAL			= "_mapping_manual";	
	private static String MAS2J_AGENT_NAME				= "_agent_name";
	
	public static List<Atom> reservedOntologyLabels = Arrays.asList( new Atom[] {new Atom("default"), new Atom("self")});
	
	
	private JasdlAgent agent;

	public JasdlConfigurator(JasdlAgent agent){
		this.agent = agent;
	}
	
	public void configure(Settings stts) throws JasdlException{
		try{
			// load agent name
			agent.setAgentName(prepareUserParameter( stts, MAS2J_PREFIX  + MAS2J_AGENT_NAME ));
			
			// load default mapping strategies
			agent.setDefaultMappingStrategies(getMappingStrategies(stts, new Atom("default"))); //implication "default" is a reserved ontology label
			
			loadOntologies(stts);
			applyManualMappings(stts);
			applyMiscMappings();		
			
			
		}catch(JasdlException e){
			throw new JasdlException("JASDL agent encountered error during configuration. Reason: "+e);
		}
	}
	
	/**
	 * Load ontologies as specified in .mas2j settings
	 * @param stts	.mas2j settings
	 * @throws JasdlException	if instantiation of an ontology fails
	 */
	private void loadOntologies(Settings stts) throws JasdlException{
		String[] labels = splitUserParameter( stts, MAS2J_PREFIX + MAS2J_ONTOLOGIES );
		for(String label : labels){
			if(reservedOntologyLabels.contains(label)){
				throw new JasdlException(label+" is a reserved ontology label");
			}			
			URI physicalURI;
			try {
				physicalURI = new URI(prepareUserParameter( stts, MAS2J_PREFIX + "_" + label + MAS2J_URI ));
				agent.loadOntology(new Atom(label), physicalURI, getMappingStrategies(stts, new Atom(label)));
			} catch (Exception e) {
				throw new JasdlException("Unable to instantiate ontology \""+label+"\". Reason: "+e);
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
		}
	}
	

	
	
	/**
	 * Extracts mapping strategy related entries from .mas2j settings and applies to relevant ontologies.
	 * Applies mappings to all resources in an ontology according to composition of supplied strategies.
	 * @param stts	.mas2j settings
	 */
	private List<MappingStrategy> getMappingStrategies(Settings stts, Atom label) throws JasdlException{
		List<MappingStrategy> strategies = new Vector<MappingStrategy>();
		String[] strategyNames = splitUserParameter(stts, MAS2J_PREFIX + "_" + label + MAS2J_MAPPING_STRATEGIES);
		for(String strategyName : strategyNames){
			if(strategyName.length() > 0){
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
		}
		if(strategies.size() == 0){
			strategies = agent.getDefaultMappingStrategies();
		}
		return strategies;	
	}	
	
	/**
	 * Maps thing and nothing. Must use "owl" label to refer to these, e.g. thing(x)[o(owl)]
	 * 
	 * @throws JasdlException
	 */
	private void applyMiscMappings() throws JasdlException{
		
		// create a "placeholder" ontology so we can safely map thing and nothing without actually loading the ontology
		agent.createOntology(AliasFactory.OWL_THING.getLabel(), URI.create("http://www.w3.org/2002/07/owl"));
		
		// create a personal ontology for (axioms that reference) run-time defined class
		agent.createOntology(agent.getPersonalOntologyLabel(), agent.getPersonalOntologyURI());
		
		agent.getAliasManager().put( AliasFactory.OWL_THING, agent.getOntologyManager().getOWLDataFactory().getOWLThing());
		agent.getAliasManager().put( AliasFactory.OWL_NOTHING, agent.getOntologyManager().getOWLDataFactory().getOWLNothing());
		
		for(Atom label : agent.getLabelManager().getLefts()){
			agent.getAliasManager().put( AliasFactory.INSTANCE.all_different(label), new AllDifferentPlaceholder(label)); // must be new instance to avoid duplicate mapping exceptions
		}
	}	
	
	
	
	
	
	
	
	
	/**
	 * Convenience method to strip quotes and trim a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to prepare
	 * @return
	 */
	private String prepareUserParameter(Settings stts, String name){
		String p = stts.getUserParameter(name);
		if(p == null){
			return "";
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
	private String[] splitUserParameter(Settings stts, String name, String delim){
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
	private String[] splitUserParameter(Settings stts, String name){
		return splitUserParameter(stts, name, DELIM);
	}	
	
	
	
	
			
}
