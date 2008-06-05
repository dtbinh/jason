package jasdl.owl2mas.handler;

import jasdl.JASDLParams;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owl.model.OWLIndividual;

import owl2mas.OWL2MAS;
import owl2mas.exception.OWL2MASInvalidMASOntologyException;
import owl2mas.handler.ObjectPropertyHandler;

public class TrustRatingHandler implements ObjectPropertyHandler{
	public void handle(OWLIndividual agent, Set<OWLIndividual> values, OWL2MAS owl2mas, HashMap<String, String> optionMap) throws OWL2MASInvalidMASOntologyException {
		
		Set<OWLIndividual> trustRatingIs = owl2mas.getPellet().getRelatedIndividuals(
				agent, 
				owl2mas.getFactory().getOWLObjectProperty(URI.create(JASDLParams.JASDL_OWL_NS + "trust_rating")));
		
		
		for(OWLIndividual trustRatingI : trustRatingIs){
			OWLIndividual trustRatingAgent = owl2mas.getPellet().getRelatedIndividual(
					trustRatingI, 
					owl2mas.getFactory().getOWLObjectProperty(URI.create(JASDLParams.JASDL_OWL_NS + "trust_rating_agent")));
			
			float trustRatingValue = Float.parseFloat(owl2mas.getPellet().getRelatedValue(
					trustRatingI, 
					owl2mas.getFactory().getOWLDataProperty(URI.create(JASDLParams.JASDL_OWL_NS + "trust_rating_value"))).getLiteral());
			
			optionMap.put("jasdl_"+trustRatingAgent+"_trustRating", "\""+trustRatingValue+"\"");
			
		}
	
	}


}
