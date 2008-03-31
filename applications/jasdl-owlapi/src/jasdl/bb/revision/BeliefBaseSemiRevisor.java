package jasdl.bb.revision;

import jason.RevisionFailedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.ReasonerFactory;
import com.clarkparsia.explanation.io.ConciseExplanationRenderer;

public class BeliefBaseSemiRevisor extends IsolatedOntologyOperation {
	
	private Logger logger;
	private OWLAxiom toAdd;

	
	public BeliefBaseSemiRevisor(OWLAxiom toAdd, OWLOntologyManager originalManager, ReasonerFactory reasonerFactory, Logger logger) throws UnknownOWLOntologyException, OWLOntologyCreationException, OWLOntologyChangeException, OWLReasonerException{
		super(originalManager, reasonerFactory);	
		this.logger = logger;		
		this.toAdd = toAdd;		
	}

	public List<OWLAxiom> revise(KernelsetFilter kernelsetFilter, IncisionFunction incisionFunction)  throws RevisionFailedException, OWLReasonerException, OWLOntologyChangeException{
		isolatedOntologyManager.applyChange(new AddAxiom(isolatedOntology, toAdd));		
		isolatedReasoner.classify();		
		List<OWLAxiom> delList = new Vector<OWLAxiom>();
		if(!isolatedReasoner.isConsistent(isolatedOntology)){			
			Set<Set<OWLAxiom>> kernelset = applyKernelOperator();			
			kernelset = kernelsetFilter.apply(kernelset);
			logger.fine("Explanation of inconsistency due to "+toAdd+": "+kernelset);
			Set<OWLAxiom> toContract = incisionFunction.apply(kernelset);
			logger.fine("... chosen to contract: "+toContract);
			if(toContract.contains(toAdd)){
				throw new RevisionFailedException(toAdd+" rejected");
			}
			
			delList.addAll(toContract);		
		}		
		return delList;
	}
	
	private void render(Set<Set<OWLAxiom>> kernelset){
		ConciseExplanationRenderer renderer = new ConciseExplanationRenderer();
		renderer.startRendering(new PrintWriter(System.out));
		try {
			renderer.render(toAdd, kernelset);
		} catch (OWLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		renderer.endRendering();		
	}

	
	private Set<Set<OWLAxiom>> applyKernelOperator(){
		BlackBoxExplanation bbgen = new BlackBoxExplanation(isolatedOntologyManager);
		HSTExplanationGenerator hstgen = new HSTExplanationGenerator(bbgen);
		hstgen.setOntology(isolatedOntology);
		hstgen.setReasoner(isolatedReasoner);
		hstgen.setReasonerFactory(reasonerFactory);		
		IndividualAxiomToDescriptionConverter conv = new IndividualAxiomToDescriptionConverter(isolatedOntologyManager.getOWLDataFactory());
		toAdd.accept(conv);		
		Set<Set<OWLAxiom>> kernelset = hstgen.getExplanations(conv.getDescription());
		
		// since we are dealing with semi-revision, ensure the axiom toAdd is a possible justification in each a-kernel that can be retracted
		// doesn't seem to be adding when toAdd is a class assertion?? TODO: Is this a bug??
		for(Set<OWLAxiom> akernel : kernelset){
			akernel.add(toAdd);
		}
		
		return kernelset;
	}

	
	
}
