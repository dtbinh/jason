package jasdl.bb;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bb.revision.BeliefBaseContractor;
import jasdl.bb.revision.JasdlIncisionFunction;
import jasdl.bb.revision.JasdlReasonerFactory;
import jasdl.bb.revision.TBoxAxiomKernelsetFilter;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.seliteral.SELiteralAllDifferentAssertion;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.bb.DefaultBeliefBase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.RemoveAxiom;

public class JasdlBeliefBase extends DefaultBeliefBase{
	
	private JasdlAgent agent;

	@Override
	public boolean add(Literal l) {		
		getLogger().fine("Adding "+l);
		try{

			SELiteral sl = agent.getSELiteralFactory().create(l); 
			OWLIndividualAxiom axiom = sl.createAxiom();
			OWLOntology ontology = sl.getOntology();
						
			boolean containsAxiom = ontology.containsAxiom(axiom);
			if(!containsAxiom){
				agent.getOntologyManager().applyChange(new AddAxiom(ontology, axiom));	
			}
			
			boolean containsAllAnnots = true;
			for(Term _annot : sl.getSemanticallyNaiveAnnotations()){
				OWLAnnotation annot = agent.getOntologyManager().getOWLDataFactory().getOWLLabelAnnotation(_annot.toString());
				OWLAxiomAnnotationAxiom annotAxiom = agent.getOntologyManager().getOWLDataFactory().getOWLAxiomAnnotationAxiom(axiom, annot);
				if(!ontology.containsAxiom(annotAxiom)){
					containsAllAnnots = false;
					agent.getOntologyManager().applyChange(new AddAxiom(ontology, annotAxiom));
				}
			}			
						
			if(!containsAxiom || !containsAllAnnots){
				agent.getReasoner().refresh();				
				if(!agent.isBeliefRevisionEnabled()){ // if brf disabled, resort to legacy consistency maintenance mechanism 
					if(!agent.getReasoner().isConsistent()){
						RemoveAxiom rem = new RemoveAxiom(ontology, axiom);
						agent.getOntologyManager().applyChange(rem);
						agent.getReasoner().refresh();
						return false;
					}
				}
				
				return true;
			}else{
				return false;
			}			
		}catch(NotEnrichedException e){	
			getLogger().fine("... semantically-naive");
			return super.add(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught adding SELiteral "+l+" to belief base: ");
			e.printStackTrace();
			return false;
		}
	}

	
	/**
	 * Removal is equivalent to contraction!
	 */
	@Override
	public boolean remove(Literal l) {
		getLogger().fine("Contracting "+l);
		try{			
			SELiteral sl = agent.getSELiteralFactory().create(l);
			OWLOntology ontology = sl.getOntology();
			OWLIndividualAxiom axiom = sl.createAxiom();			
			
			boolean result = false; // -> at least *something* must be removed for this to be true!
			
			BeliefBaseContractor contractor = new BeliefBaseContractor(agent.getOntologyManager(), new JasdlReasonerFactory(), agent.getLogger());			
			List<OWLAxiom> contractList = contractor.contract(axiom, new TBoxAxiomKernelsetFilter(), new JasdlIncisionFunction(agent, sl));
			
			// NOTE: this technique only really makes sense with annotation gathering!
			for(OWLAxiom contract : contractList){ // removals corresponds to l and all l's whose removal will undermine it
				
				// for each annotation to this axiom, remove it if l has it
				List<OWLAxiomAnnotationAxiom> annotationsToRemove = new Vector<OWLAxiomAnnotationAxiom>(); // to avoid concurrency issues
				for(OWLAxiomAnnotationAxiom annotAxiom : contract.getAnnotationAxioms(ontology)){
					Term annot = Literal.parse(annotAxiom.getAnnotation().getAnnotationValueAsConstant().getLiteral());
					if(l.hasAnnot(annot)){
						annotationsToRemove.add(annotAxiom);
						result = true;
					}
				}
				
				for(OWLAxiom annotationToRemove : annotationsToRemove){
					agent.getOntologyManager().applyChange(new RemoveAxiom(ontology, annotationToRemove));
				}				
				
				// remove source(self) and source(percept). TODO: right thing to do?				
				// remove assertion if no annotation axioms left
				Set<OWLAxiomAnnotationAxiom> remaining = contract.getAnnotationAxioms(ontology);
				if(remaining.isEmpty()){
					agent.getOntologyManager().applyChange(new RemoveAxiom(ontology, contract));
					result = true;
				}
			}
			agent.getReasoner().refresh();
			return result;
		}catch(NotEnrichedException e){			
			return super.remove(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught removing SELiteral "+l+" from belief base: ");
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public Literal contains(Literal l) {	
		agent.getLogger().fine("Contains: "+l);
		try {
			SELiteral sl = agent.getSELiteralFactory().create(l);
			OWLIndividualAxiom axiom = sl.createAxiom();
			if(sl.getOntology().containsAxiom(axiom)){
				Iterator<Literal> it = getCandidateBeliefs(l, null);
				if(it.hasNext()){
					return it.next();
				}
			}
			return null;
		}catch(NotEnrichedException e){			
			return super.contains(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught while checking if bb contains SELiteral "+l+". Reason: "+e);
			return null;
		}
	}	
	
	

	@Override
	public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier un) {		
		l = (Literal)l.clone();
		l.apply(un);
		
		getLogger().fine("Getting relevancies for "+l);
		
		Set<Literal> relevant = new HashSet<Literal>();
		try{			
			SELiteral sl = agent.getSELiteralFactory().create(l);
			Set<OWLIndividualAxiom> axioms = sl.getAxioms();
			for(OWLIndividualAxiom axiom : axioms){
				SELiteral found = agent.getToSELiteralConverter().convert(axiom);
				
				// hack, gets around non-consistent ordering of OWLDifferentIndividualAxiom individuals
				// -- just check list membership is equivalent (i.e. treat as sets)
				if(found instanceof SELiteralAllDifferentAssertion){
					SELiteralAllDifferentAssertion diff = (SELiteralAllDifferentAssertion)found;
					Set<OWLIndividual> is = diff.getOWLIndividuals();
					Set<OWLIndividual> js = ((OWLDifferentIndividualsAxiom)axiom).getIndividuals();
					if(is.containsAll(js)){
						found.getLiteral().setTerm(0, l.getTerm(0)); // Sets are equivalent, change to original ordering
					}
				}
				
				relevant.add(found.getLiteral());
				
				
				
			}
			getLogger().fine("... found: "+relevant);
			if(relevant.isEmpty()){
				return null;
			}
			
			
		}catch(NotEnrichedException e){
			return super.getCandidateBeliefs(l, un); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught getting relevancies for SELiteral "+l+" to belief base: ");
			e.printStackTrace();			
		}
		return relevant.iterator();
	}
	
	
	/**
	 * TODO: Currently only returns asserted ABox axioms. Include option to also show inferences?
	 */
	@Override
	public Iterator<Literal> iterator() {
		List<Literal> bels = new Vector<Literal>();
		
		// add all SN-Literals
		Iterator it = super.iterator();
		while(it.hasNext()){
			bels.add((Literal)it.next());
		}
	
		// add all SE-Literals (asserted)
		try{
			bels.addAll(agent.getABoxState());
		}catch(JasdlException e){
			getLogger().warning("Exception caught while retrieving ABox state: "+e);
		}		
		
		return bels.iterator();
	}	



	public void setAgent(JasdlAgent agent) {
		this.agent = agent;
	}
	
	private Logger getLogger(){
		return agent.getLogger();
	}
	

}
