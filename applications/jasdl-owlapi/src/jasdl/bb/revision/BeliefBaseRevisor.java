package jasdl.bb.revision;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.ReasonerFactory;
import com.clarkparsia.explanation.SatisfiabilityConverter;

public class BeliefBaseRevisor extends IsolatedOntologyOperation {
	
	private Logger logger;
	private OWLAxiom toAdd;
	
	public BeliefBaseRevisor(OWLAxiom toAdd, OWLOntologyManager originalManager, ReasonerFactory reasonerFactory, Logger logger) throws UnknownOWLOntologyException, OWLOntologyCreationException, OWLOntologyChangeException, OWLReasonerException{
		super(originalManager, reasonerFactory);	
		this.logger = logger;		
		// unfortunately, this needs to be done here (not in .revise)
		// since JASDL (specifically) needs to annotate this axiom
		this.toAdd = toAdd;
		isolatedOntologyManager.applyChange(new AddAxiom(isolatedOntology, toAdd));	
	}

	public List<OWLAxiom>[] revise(KernelsetFilter kernelsetFilter, IncisionFunction incisionFunction){
		List<OWLAxiom> addList = new Vector<OWLAxiom>();
		List<OWLAxiom> delList = new Vector<OWLAxiom>();		
		try {			
			isolatedReasoner.classify();
			
			if(isolatedReasoner.isConsistent(isolatedOntology)){
				addList.add(toAdd);
			}else{			
				Set<Set<OWLAxiom>> kernelset = applyKernelOperator(toAdd, isolatedOntologyManager, isolatedOntology, isolatedReasoner, reasonerFactory);
				kernelset = kernelsetFilter.apply(kernelset);
				log("Explanation of inconsistency: "+kernelset);
				BeliefBaseContractor contractor = new BeliefBaseContractor(isolatedOntologyManager, reasonerFactory, logger);
				List<OWLAxiom> contracted = contractor.contract(incisionFunction.apply(kernelset), kernelsetFilter, incisionFunction);
								
				
				if(!contracted.contains(toAdd)){
					addList.add(toAdd); // remember, toAdd is not in original ontology so need to remove if it has been contracted
				}
				delList.addAll(contracted);				
			}			
			
			
		} catch (Exception e) {
			logger.warning("Revision failed. Reason: ");
			e.printStackTrace();
		}
		
		return new List[] {addList, delList};
	}	

	
	private Set<Set<OWLAxiom>> applyKernelOperator(OWLAxiom a, OWLOntologyManager ontologyManager, OWLOntology ontology, OWLReasoner reasoner, ReasonerFactory reasonerFactory){
		SatisfiabilityConverter satcon = new SatisfiabilityConverter(ontologyManager.getOWLDataFactory());
		OWLDescription description = ontologyManager.getOWLDataFactory().getOWLObjectComplementOf(satcon.convert(a));
		BlackBoxExplanation bbgen = new BlackBoxExplanation(ontologyManager);
		HSTExplanationGenerator hstgen = new HSTExplanationGenerator(bbgen);
		hstgen.setOntology(ontology);
		hstgen.setReasoner(reasoner);
		hstgen.setReasonerFactory(reasonerFactory);
		return hstgen.getExplanations(description);
	}
	
	
	private void log(String msg){
		if(logger != null){
			logger.fine(msg);
		}
	}

	
	
}
