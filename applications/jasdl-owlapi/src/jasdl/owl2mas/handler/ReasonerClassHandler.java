package jasdl.owl2mas.handler;

import jasdl.JASDLParams;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owl.model.OWLIndividual;

import owl2mas.OWL2MAS;
import owl2mas.exception.OWL2MASInvalidMASOntologyException;
import owl2mas.handler.ObjectPropertyHandler;

public class ReasonerClassHandler implements ObjectPropertyHandler{
	public void handle(OWLIndividual agent, Set<OWLIndividual> values, OWL2MAS owl2mas, HashMap<String, String> optionMap) throws OWL2MASInvalidMASOntologyException {
		OWLIndividual reasonerClassI = owl2mas.getPellet().getRelatedIndividual(
				agent, 
				owl2mas.getFactory().getOWLObjectProperty(URI.create(JASDLParams.JASDL_OWL_NS + "reasoner_class")));
		
		String reasonerClassName = owl2mas.getPellet().getRelatedValue(
				reasonerClassI, 
				owl2mas.getFactory().getOWLDataProperty(URI.create(owl2mas.getMAS_NS() + "hasClassName"))).getLiteral();
		
		optionMap.put("jasdl_reasonerClass", "\""+reasonerClassName+"\"");
	
	}


}
