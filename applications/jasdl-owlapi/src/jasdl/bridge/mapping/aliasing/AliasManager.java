package jasdl.bridge.mapping.aliasing;

import jasdl.bridge.mapping.MappingManager;

import org.semanticweb.owl.model.OWLObject;

/**
 * Maintains a bi-directional link between JASDL aliases (functor+ontology label) and the corresponding ontology resource (OWLObject).
 * Each JasdlAgent has one AliasManager.
 * @author Tom Klapiscak
 *
 */
public class AliasManager extends MappingManager<Alias, OWLObject> {	
}