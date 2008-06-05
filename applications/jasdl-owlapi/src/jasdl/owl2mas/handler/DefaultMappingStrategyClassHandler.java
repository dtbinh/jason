package jasdl.owl2mas.handler;

import jasdl.JASDLParams;
import jasdl.util.JASDLCommon;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owl.model.OWLIndividual;

import owl2mas.OWL2MAS;
import owl2mas.exception.OWL2MASInvalidMASOntologyException;
import owl2mas.handler.ObjectPropertyHandler;

public class DefaultMappingStrategyClassHandler implements ObjectPropertyHandler{

	public void handle(OWLIndividual agent, Set<OWLIndividual> values, OWL2MAS owl2mas, HashMap<String, String> optionMap) throws OWL2MASInvalidMASOntologyException {
		Set<OWLIndividual> defaultMappingStrategyIs = owl2mas.getPellet().getRelatedIndividuals(
				agent, 
				owl2mas.getFactory().getOWLObjectProperty(URI.create(JASDLParams.JASDL_OWL_NS + "default_mapping_strategy")));
		
		String defaultMappingStrategies = "";
		
		for(OWLIndividual defaultMappingStrategyI : defaultMappingStrategyIs){
			String mappingStrategyClassName = owl2mas.getPellet().getRelatedValue(
					defaultMappingStrategyI, 
					owl2mas.getFactory().getOWLDataProperty(URI.create(owl2mas.getMAS_NS() + "hasClassName"))).getLiteral();
			
			defaultMappingStrategies += mappingStrategyClassName+",";
		}
		
		defaultMappingStrategies = JASDLCommon.dropLast(defaultMappingStrategies);
		
		optionMap.put("jasdl_default_mapping_strategies", "\""+defaultMappingStrategies+"\"");
	}
}
