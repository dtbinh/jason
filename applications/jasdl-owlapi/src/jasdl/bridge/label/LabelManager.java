package jasdl.bridge.label;

import jasdl.util.DuplicateMappingException;
import jasdl.util.UnknownMappingException;
import jason.asSyntax.Atom;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;

/**
 * Basically a two-way hash map
 * @author tom
 *
 */
public class LabelManager {
	/**
	 * Maps labels to ontologies
	 */
	private HashMap<Atom, OWLOntology> labelToOntologyMap;
	
	/**
	 * Maps ontologies to labels
	 */
	private HashMap<OWLOntology, Atom> ontologyToLabelMap;
	
	public LabelManager(){
		labelToOntologyMap = new HashMap<Atom, OWLOntology>();
		ontologyToLabelMap = new HashMap<OWLOntology, Atom>();
	}
	
	/**
	 * Maps a label to an ontology and visa-versa
	 * 1 <-> 1 relationships enforced to prevent ambiguous labelling.
	 * @param label
	 * @param ontology
	 * @throws DuplicateMappingException	if either alias or entity is already mapped (thus breaking 1 <-> 1 constraint)
	 */
	public void put(Atom label, OWLOntology ontology) throws DuplicateMappingException{
		
		if(labelToOntologyMap.containsKey(label)){
			throw new DuplicateMappingException("Duplicate mapping on label "+label);
		}
		if(ontologyToLabelMap.containsKey(ontology)){
			throw new DuplicateMappingException("Duplicate mapping on ontology "+ontology);
		}
		
		labelToOntologyMap.put(label, ontology);
		ontologyToLabelMap.put(ontology, label);
	}
	
	/**
	 * Gets the alias associated with an entity
	 * @param ontology
	 * @return
	 * @throws UnknownMappingException	if entity is unknown (not mapped)
	 */
	public Atom get(OWLOntology ontology) throws UnknownMappingException{
		Atom label = ontologyToLabelMap.get(ontology);
		if(label == null){
			throw new UnknownMappingException("Unknown ontology "+ontology);
		}
		return label;
	}
	
	/**
	 * Gets the entity associated with an alias
	 * @param label
	 * @return
	 * @throws UnknownMappingException	if alias is unknown (not mapped)
	 */
	public OWLOntology get(Atom label) throws UnknownMappingException{
		OWLOntology ontology = labelToOntologyMap.get(label);
		if(ontology == null){
			throw new UnknownMappingException("Unknown label "+label);
		}
		return ontology;
	}
	
	
	public Set<Atom> getLabels(){
		return labelToOntologyMap.keySet();
	}
	
	public Set<OWLOntology> getOntologies(){
		return ontologyToLabelMap.keySet();
	}	
}
