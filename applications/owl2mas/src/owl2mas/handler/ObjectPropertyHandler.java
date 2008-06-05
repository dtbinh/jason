package owl2mas.handler;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owl.model.OWLIndividual;

import owl2mas.OWL2MAS;
import owl2mas.exception.OWL2MASInvalidMASOntologyException;

public interface ObjectPropertyHandler {
	
	public abstract void handle(OWLIndividual agent, Set<OWLIndividual> values, OWL2MAS owl2mas, HashMap<String, String> optionMap) throws OWL2MASInvalidMASOntologyException;
}
