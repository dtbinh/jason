package jasdl.bb.bbops.revision;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.explanation.ReasonerFactory;

public class JasdlReasonerFactory implements ReasonerFactory{
	
	public JasdlReasonerFactory() {
		super();
	}

	public boolean requiresExplicitClassification() {
		return false;
	}

	public OWLReasoner createReasoner(OWLOntologyManager manager) {
		Reasoner pellet = new Reasoner(manager);
		return pellet;
	}

	public String getReasonerName() {
		return "reasoner";
	}
	

}
