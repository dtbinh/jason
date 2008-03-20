package jasdl.bb.revision;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

import com.clarkparsia.explanation.ReasonerFactory;
// brf should clone ontology so other reasoning can carry on elsewhere
public class IsolatedOntologyOperation {
	
	protected OWLOntologyManager isolatedOntologyManager;
	protected OWLOntology isolatedOntology;
	protected ReasonerFactory reasonerFactory;
	protected OWLReasoner isolatedReasoner;
	
	
	public IsolatedOntologyOperation(OWLOntologyManager originalManager, ReasonerFactory reasonerFactory) throws OWLOntologyCreationException, OWLOntologyChangeException, UnknownOWLOntologyException, OWLReasonerException{
		this.isolatedOntologyManager = OWLManager.createOWLOntologyManager();
		this.isolatedOntology = createDebuggingOntology(originalManager, isolatedOntologyManager);			
		this.reasonerFactory = new JasdlReasonerFactory();
		this.isolatedReasoner = reasonerFactory.createReasoner(isolatedOntologyManager);			
		this.isolatedReasoner.loadOntologies(isolatedOntologyManager.getImportsClosure(isolatedOntology));		
	}
	
	private OWLOntology createDebuggingOntology(OWLOntologyManager from, OWLOntologyManager to) throws OWLOntologyCreationException, OWLOntologyChangeException{
		OWLOntology debuggingOntology = to.createOntology(URI.create("http://beliefbase/isolated.owl"));
		for(OWLOntology ontology : from.getOntologies()){
			List<OWLOntologyChange> changes = new Vector<OWLOntologyChange>();
			for(OWLAxiom axiom : ontology.getAxioms()){
				AddAxiom change = new AddAxiom(debuggingOntology, axiom);
				changes.add(change);
			}			
			to.applyChanges(changes);
		}
		return debuggingOntology;
	}

	public OWLOntology getIsolatedOntology() {
		return isolatedOntology;
	}

	public OWLOntologyManager getIsolatedOntologyManager() {
		return isolatedOntologyManager;
	}
	
	
}
