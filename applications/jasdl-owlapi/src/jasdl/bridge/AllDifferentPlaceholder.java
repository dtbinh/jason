package jasdl.bridge;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAnnotationAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityVisitor;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNamedObjectVisitor;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;

/**
 * Since the all_different has no concrete entity associated with it, we create a "placeholder" so that it remains consistent with
 * JASDL's mapping mechanisms. Entities of this type will be intercepted and dealt with differently.
 * @author tom
 *
 */
public class AllDifferentPlaceholder implements OWLEntity {
	public static AllDifferentPlaceholder INSTANCE = new AllDifferentPlaceholder();

	public void accept(OWLEntityVisitor visitor) {		
	}

	public Set<OWLAnnotationAxiom> getAnnotationAxioms(OWLOntology ontology) {
		return null;
	}

	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology) {
		return null;
	}

	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology, URI annotationURI) {
		return null;
	}

	public void accept(OWLObjectVisitor visitor) {
		
	}

	public void accept(OWLNamedObjectVisitor visitor) throws OWLException {		
	}

	public URI getURI() {
		return null;
	}

}
