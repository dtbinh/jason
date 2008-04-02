package jasdl.bridge.mapping.label;

import jasdl.bridge.mapping.MappingManager;
import jasdl.util.exception.DuplicateMappingException;
import jason.asSyntax.Atom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;

/**
 * Maintains a bi-directional link between atomic ontology label and the corresponding ontology object.
 * Each JasdlAgent has one LabelManager.
 * @author Tom Klapiscak
 *
 */
public class LabelManager extends MappingManager<Atom, OWLOntology>{
	
	/**
	 * Members of this set are defined as "personal" ontology instances used for storing axioms
	 * about run-time defined classes and individuals. This information is needed, for example, when preparing
	 * outgoing message content (named or anon?).
	 */
	private Set<OWLOntology> personals;
	
	
	
	
	public LabelManager() {
		super();
		personals = new HashSet<OWLOntology>();
	}
	
	public boolean isPersonal(OWLOntology y){
		return personals.contains(y);
	}


	public void put(Atom x, OWLOntology y, boolean isPersonal) throws DuplicateMappingException {
		if(isPersonal) personals.add(y);
		super.put(x, y);
	}
	
	
}
