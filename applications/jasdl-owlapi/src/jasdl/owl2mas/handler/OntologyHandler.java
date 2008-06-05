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

public class OntologyHandler implements ObjectPropertyHandler{


	public void handle(OWLIndividual agent, Set<OWLIndividual> values, OWL2MAS owl2mas, HashMap<String, String> optionMap) throws OWL2MASInvalidMASOntologyException {
	
		String labels = "";
		for(OWLIndividual ontology : values){
			String label = owl2mas.getPellet().getRelatedValue(
					ontology, 
					owl2mas.getFactory().getOWLDataProperty(URI.create(JASDLParams.JASDL_OWL_NS + "ontology_label"))).getLiteral();
			
			String uri = owl2mas.getPellet().getRelatedValue(
					ontology, 
					owl2mas.getFactory().getOWLDataProperty(URI.create(JASDLParams.JASDL_OWL_NS + "ontology_uri"))).getLiteral();
			
			labels+=label+",";
			
			// uri
			optionMap.put("jasdl_"+label+"_uri", "\""+uri+"\"");
			
			// manual mappings
			String manualMappings = "";
			Set<OWLIndividual> manualMappingIs = owl2mas.getPellet().getRelatedIndividuals(ontology, owl2mas.getFactory().getOWLObjectProperty(URI.create(JASDLParams.JASDL_OWL_NS + "ontology_manual_mapping")));
			
			if(!manualMappingIs.isEmpty()){
				for(OWLIndividual manualMappingI : manualMappingIs){
					String alias = owl2mas.getPellet().getRelatedValue(
							manualMappingI, 
							owl2mas.getFactory().getOWLDataProperty(URI.create(JASDLParams.JASDL_OWL_NS + "manual_mapping_alias"))).getLiteral();
					
					String fragment = owl2mas.getPellet().getRelatedValue(
							manualMappingI, 
							owl2mas.getFactory().getOWLDataProperty(URI.create(JASDLParams.JASDL_OWL_NS + "manual_mapping_fragment"))).getLiteral();
					
					manualMappings += alias+"="+fragment+",";
				}				
				manualMappings = JASDLCommon.dropLast(manualMappings);				
				optionMap.put("jasdl_"+label+"_manual_mappings", "\""+manualMappings+"\"");
			}
			
			// mapping strategies
			String mappingStrategies = "";
			Set<OWLIndividual> mappingStrategyIs = owl2mas.getPellet().getRelatedIndividuals(
					ontology, 
					owl2mas.getFactory().getOWLObjectProperty(URI.create(JASDLParams.JASDL_OWL_NS + "ontology_mapping_strategy")));
			
			if(!mappingStrategyIs.isEmpty()){
				for(OWLIndividual mappingStrategyI : mappingStrategyIs){
					String mappingStrategyClassName = owl2mas.getPellet().getRelatedValue(
							mappingStrategyI, 
							owl2mas.getFactory().getOWLDataProperty(URI.create(owl2mas.getMAS_NS() + "hasClassName"))).getLiteral();
					
					mappingStrategies += mappingStrategyClassName+",";
				}				
				mappingStrategies = JASDLCommon.dropLast(mappingStrategies);				
				optionMap.put("jasdl_"+label+"_mapping_strategies", "\""+mappingStrategies+"\"");
			}
		}
		
		labels = JASDLCommon.dropLast(labels);
		
		optionMap.put("jasdl_ontologies", "\""+labels+"\"");

	}
	



}
