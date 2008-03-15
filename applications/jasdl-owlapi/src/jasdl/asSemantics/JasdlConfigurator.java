package jasdl.asSemantics;

import static jasdl.util.Common.DELIM;
import static jasdl.util.Common.strip;
import jasdl.bridge.AllDifferentPlaceholder;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.AliasFactory;
import jasdl.bridge.alias.MappingStrategy;
import jasdl.util.DuplicateMappingException;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;
import jason.runtime.Settings;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;

public class JasdlConfigurator {
	private static String MAS2J_PREFIX					= "jasdl";
	private static String MAS2J_ONTOLOGIES 				= "_ontologies";
	private static String MAS2J_URI						= "_uri";
	private static String MAS2J_MAPPING_STRATEGIES		= "_mapping_strategies";
	private static String MAS2J_MAPPING_MANUAL			= "_mapping_manual";
	
	
	private JasdlAgent agent;

	public JasdlConfigurator(JasdlAgent agent){
		this.agent = agent;
	}
	
	public void configure(Settings stts) throws JasdlException{
		try{
			loadOntologies(stts);
			applyManualMappings(stts);
			applyMiscMappings();
			applyMappingStrategies(stts);
			
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
			URI physicalURI;
			try {
				physicalURI = new URI(prepareUserParameter( stts, MAS2J_PREFIX + "_" + label + MAS2J_URI ));
				agent.loadOntology(new Atom(label), physicalURI);
			} catch (Exception e) {
				throw new JasdlException("Unable to instantiate ontology \""+label+"\". Reason: ");
			}			
		}
	}
	
	private void applyManualMappings(Settings stts) throws JasdlException{
		for(Atom label : agent.getLabelManager().getLabels()){
			String[] mappings = splitUserParameter(stts, MAS2J_PREFIX + "_" + label + MAS2J_MAPPING_MANUAL);
			for(String mapping : mappings){
				OWLOntology ontology = agent.getLabelManager().get(label);
				String[] split = mapping.split("=");
				if(split.length == 2){
					Alias alias = AliasFactory.INSTANCE.create(new Atom(split[0].trim()), label);
					URI uri = URI.create(ontology.getURI() + "#" + split[1].trim());
					OWLEntity entity = agent.toEntity(ontology, uri);
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
	private void applyMappingStrategies(Settings stts) throws JasdlException{
		for(Atom label : agent.getLabelManager().getLabels()){
			List<MappingStrategy> strategies = new Vector<MappingStrategy>();
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
			
			OWLOntology ontology = agent.getLabelManager().get(label);
			
			// we need to construct a reasoner specifically for this to isolate entities from just one ontology
			Reasoner reasoner = new Reasoner(agent.getOntologyManager());			
			Set<OWLOntology> imports = agent.getOntologyManager().getImportsClosure(ontology);
			reasoner.loadOntologies(imports);
			
			List<OWLEntity> entities = new Vector<OWLEntity>();
			entities.addAll(reasoner.getClasses());
			entities.addAll(reasoner.getProperties());
			entities.addAll(reasoner.getIndividuals());
			
			for(OWLEntity entity : entities){
				try{
					String _functor = entity.getURI().getFragment();					
					for(MappingStrategy strategy : strategies){
						_functor = strategy.apply(_functor);
					}
					Atom functor = new Atom(_functor);
					agent.getAliasManager().put(AliasFactory.INSTANCE.create(functor, label), entity);
				}catch(DuplicateMappingException e){
					// only apply mapping strategies to entities that have not been manually mapped already
				}
			}

		}		
	}	
	
	/**
	 * Maps thing and nothing. Each ontology has its own thing and nothing alias mappings, but they all map to owl:thing or owl:nothing respectively.
	 * This prevents us from needing to use a special label to refer to owl's concept of thing (e.g. thing(x)[o(owl)]).
	 * 
	 * @throws JasdlException
	 */
	private void applyMiscMappings() throws JasdlException{
		for(Atom label : agent.getLabelManager().getLabels()){
			agent.getAliasManager().put( AliasFactory.INSTANCE.thing(label), agent.getOntologyManager().getOWLDataFactory().getOWLThing());
			agent.getAliasManager().put( AliasFactory.INSTANCE.nothing(label), agent.getOntologyManager().getOWLDataFactory().getOWLNothing());
			agent.getAliasManager().put( AliasFactory.INSTANCE.all_different(label), new AllDifferentPlaceholder());
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
