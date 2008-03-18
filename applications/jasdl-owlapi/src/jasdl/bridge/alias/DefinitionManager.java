package jasdl.bridge.alias;

import jasdl.bridge.MappingManager;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;

/**
 * Maintains mappings between run-time defined classes and their anonymous descriptions.
 * Required for recursive rendering of run-time defined classes in terms of predefined.
 * @author Tom Klapiscak
 *
 */
public class DefinitionManager extends MappingManager<OWLClass, OWLDescription> {	
}
