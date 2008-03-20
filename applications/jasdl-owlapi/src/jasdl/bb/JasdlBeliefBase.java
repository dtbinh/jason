package jasdl.bb;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bb.revision.BeliefBaseContractor;
import jasdl.bb.revision.JasdlReasonerFactory;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.seliteral.SELiteralAllDifferentAssertion;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jason.asSyntax.Literal;
import jason.bb.DefaultBeliefBase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
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
			SELiteral sl = agent.getSELiteralFactory().create(l); // all non-JASDL annotations added.. TODO: This needs sorting
			OWLIndividualAxiom axiom = sl.createAxiom();
			OWLOntology ontology = sl.getOntology();
			// add axiom annotations to axiom containing serialised list terms.
			// Could be (clumsily) made to sit in toAxiomConverter? - this is probably the only place we add axioms to an ontology anyway
			OWLAnnotation annot = agent.getOntologyManager().getOWLDataFactory().getOWLLabelAnnotation(sl.getSemanticallyNaiveAnnotations().toString()); //TODO: more efficient way of serialising list terms?
			OWLAxiomAnnotationAxiom annotAxiom = agent.getOntologyManager().getOWLDataFactory().getOWLAxiomAnnotationAxiom(axiom, annot);
			
			// need to merge existing annotations into a single one containing a whole list.
			
			
			boolean containsAxiom = ontology.containsAxiom(axiom);
			boolean containsAnnot = ontology.containsAxiom(annotAxiom);
			agent.getLogger().fine("Contains Axiom "+axiom+"?: " +containsAxiom);
			agent.getLogger().fine("Contains Annotation "+annotAxiom+"?: " +containsAnnot);
						
			if(!containsAxiom || !containsAnnot){
				if(!containsAxiom){
					agent.getOntologyManager().applyChange(new AddAxiom(ontology, axiom));	
				}			
				if(!containsAnnot){
					agent.getOntologyManager().applyChange(new AddAxiom(ontology, annotAxiom));
				}
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
		getLogger().fine("Removing "+l);
		try{
			// TODO: use contraction
			//BeliefBaseContractor contractor = new BeliefBaseContractor(agent.getOntologyManager(), new JasdlReasonerFactory(), agent.getLogger());			
			//contractor.contract(axiom, kernelsetFilter, incisionFunction)
			
			SELiteral sl = agent.getSELiteralFactory().create(l);
			OWLOntology ontology = sl.getOntology();
			OWLIndividualAxiom axiom = sl.createAxiom();
			getLogger().fine("... as axiom: "+axiom);
			RemoveAxiom rem = new RemoveAxiom(ontology, axiom);
			agent.getOntologyManager().applyChange(rem);
			agent.getReasoner().refresh();
			return true;
		}catch(NotEnrichedException e){			
			return super.add(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught removing SELiteral "+l+" from belief base: ");
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public Literal contains(Literal l) {		
		try {
			// TODO: use OWLOntology#contains
			SELiteral sl = agent.getSELiteralFactory().create(l); // <- currently just to establish if semantically-enriched
			//OWLIndividualAxiom axiom = sl.createAxiom();
			//OWLAnnotation annot = agent.getOntologyManager().getOWLDataFactory().getOWLLabelAnnotation(sl.getSemanticallyNaiveAnnotations().toString()); //TODO: more efficient way of serialising list terms?
			//OWLAxiomAnnotationAxiom annotAxiom = agent.getOntologyManager().getOWLDataFactory().getOWLAxiomAnnotationAxiom(axiom, annot);
			
			
			Iterator<Literal> it = getRelevant(l);
			if(it.hasNext()){
				return it.next();
			}else{
				return null;
			}
		}catch(NotEnrichedException e){
			return super.contains(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught while checking if bb contains SELiteral "+l+". Reason: "+e);
			return null;
		}
	}	
	
	

	@Override
	public Iterator<Literal> getRelevant(Literal l) {		
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
		}catch(NotEnrichedException e){
			return super.getRelevant(l); // semantically-naive, use standard Jason mechanisms
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
