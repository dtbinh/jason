package jasdl.bridge;

import org.semanticweb.owl.inference.OWLReasoner;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.explanation.ReasonerFactory;

public class JasdlReasonerFactory implements ReasonerFactory{
	
	private Reasoner reasoner;
	
	public JasdlReasonerFactory(Reasoner reasoner) {
		super();
		this.reasoner = reasoner;
	}

	public boolean requiresExplicitClassification() {
		return false;
	}

	public OWLReasoner createReasoner(OWLOntologyManager manager) {
		return reasoner;
	}
	

}
