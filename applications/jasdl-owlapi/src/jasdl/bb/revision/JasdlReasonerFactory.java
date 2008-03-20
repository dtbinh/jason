package jasdl.bb.revision;

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
		return new Reasoner(manager);
	}

	public String getReasonerName() {
		return "reasoner";
	}
	

}
