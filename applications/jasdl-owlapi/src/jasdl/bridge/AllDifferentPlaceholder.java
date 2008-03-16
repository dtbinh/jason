package jasdl.bridge;

import jason.asSyntax.Atom;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAnnotationAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityVisitor;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObjectVisitor;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;

/**
 * Since the all_different has no concrete entity associated with it, we create a "placeholder" so that it remains consistent with
 * JASDL's mapping mechanisms. Entities of this type will be intercepted and dealt with differently.
 * Associated with an ontology label which determines hash-code - required since each ontology must have its own placeholder to reference
 * @author tom
 *
 */
public class AllDifferentPlaceholder implements OWLEntity {
	private Atom label;
	
	public AllDifferentPlaceholder(Atom label){
		this.label = label;
	}
	
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

	public void accept(OWLNamedObjectVisitor visitor) {		
	}

	public URI getURI() {
		return null;
	}
	
	public boolean equals(Object other){
		if(!(other instanceof AllDifferentPlaceholder)){
			return false;
		}
		return label.equals(((AllDifferentPlaceholder)other).label);
	}
	
	public int hashCode(){
		return label.hashCode();
	}

	public OWLClass asOWLClass() {
		return null;
	}

	public OWLDataProperty asOWLDataProperty() {
		return null;
	}

	public OWLDataType asOWLDataType() {
		return null;
	}

	public OWLIndividual asOWLIndividual() {
		return null;
	}

	public OWLObjectProperty asOWLObjectProperty() {
		return null;
	}

	public boolean isOWLClass() {
		return false;
	}

	public boolean isOWLDataProperty() {
		return false;
	}

	public boolean isOWLDataType() {
		return false;
	}

	public boolean isOWLIndividual() {
		return false;
	}

	public boolean isOWLObjectProperty() {
		return false;
	}

	public int compareTo(OWLObject arg0) {
		return 0;
	}

}
