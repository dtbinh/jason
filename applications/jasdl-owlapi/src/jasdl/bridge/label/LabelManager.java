package jasdl.bridge.label;

import jasdl.bridge.MappingManager;
import jason.asSyntax.Atom;

import org.semanticweb.owl.model.OWLOntology;

/**
 * Maintains a bi-directional link between atomic ontology label and the corresponding ontology object.
 * Each JasdlAgent has one LabelManager.
 * @author Tom Klapiscak
 *
 */
public class LabelManager extends MappingManager<Atom, OWLOntology>{
}
