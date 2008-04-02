package jasdl.bb.bbops.revision;

import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.ReasonerFactory;
import com.clarkparsia.explanation.SatisfiabilityConverter;

public class BeliefBaseContractor extends IsolatedOntologyOperation{
	
	private Logger logger;

	public BeliefBaseContractor(OWLOntologyManager originalManager, ReasonerFactory reasonerFactory, Logger logger) throws OWLOntologyCreationException, OWLOntologyChangeException, UnknownOWLOntologyException, OWLReasonerException {
		super(originalManager, reasonerFactory);
		this.logger = logger;
	}

	public List<OWLAxiom> contract(Set<OWLAxiom> contractionSet, KernelsetFilter kernelsetFilter, IncisionFunction incisionFunction) throws OWLOntologyChangeException, OWLReasonerException{
		List<OWLAxiom> toRemove = new Vector<OWLAxiom>(); // to be removed OUTSIDE isolated ontology elsewhere
		for(OWLAxiom toContract : contractionSet){
			toRemove.addAll(contract(toContract, kernelsetFilter, incisionFunction));
		}
		return toRemove;
	}
	
	public List<OWLAxiom> contract(OWLAxiom axiom, KernelsetFilter kernelsetFilter, IncisionFunction incisionFunction) throws OWLOntologyChangeException, OWLReasonerException{	
		List<OWLAxiom> toRemove = new Vector<OWLAxiom>();
		
		logger.fine("Contracting "+axiom+"...");
		toRemove.add(axiom);
		// note: either axiom is a which is contradictory by definition, or another which is entailed by definition		
		isolatedOntologyManager.applyChange(new RemoveAxiom(isolatedOntology, axiom));

		// gather justifications
		// apply incision function (same one???)
		// recursively contract	
		
		
		SatisfiabilityConverter satcon = new SatisfiabilityConverter(isolatedOntologyManager.getOWLDataFactory());
		OWLDescription description = satcon.convert(axiom);
		BlackBoxExplanation bbgen = new BlackBoxExplanation(isolatedOntologyManager);
		HSTExplanationGenerator hstgen = new HSTExplanationGenerator(bbgen);
		hstgen.setOntology(isolatedOntology);
		hstgen.setReasoner(isolatedReasoner);
		hstgen.setReasonerFactory(reasonerFactory);
		Set<Set<OWLAxiom>> justifications = kernelsetFilter.apply(hstgen.getExplanations(description));		
		
		//Set<Set<OWLAxiom>> justifications = hstgen.getExplanations(description);	
		if(!justifications.isEmpty()){
			logger.fine("Description of "+axiom+": "+description);
			logger.fine("Justifications for "+axiom+": "+justifications);
			
			
			Set<OWLAxiom> toContract = incisionFunction.apply(justifications);
			toRemove.addAll(toContract);
			List<OWLOntologyChange> isolatedRemovals = new Vector<OWLOntologyChange>();
			for(OWLAxiom remove : toContract){
				isolatedRemovals.add(new RemoveAxiom(isolatedOntology, remove));
			}
			logger.fine("Removing: "+toContract);
			isolatedOntologyManager.applyChanges(isolatedRemovals);
			
			toRemove.addAll(contract(toContract, kernelsetFilter, incisionFunction));
		}

			
		return toRemove;
		
		
	}
}
