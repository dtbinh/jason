package jasdl.bb;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.seliteral.SELiteralAllDifferentAssertion;
import jasdl.util.NotEnrichedException;
import jason.asSyntax.Literal;
import jason.bb.DefaultBeliefBase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owl.model.AddAxiom;
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
			getLogger().fine("... "+sl);
			OWLOntology ontology = sl.getOntology();
			OWLIndividualAxiom axiom = sl.createAxiom();
			getLogger().fine("..... as axiom: "+axiom);
			AddAxiom add = new AddAxiom(ontology, axiom);
			agent.getOntologyManager().applyChange(add);
			agent.getReasoner().refresh();
			
			if(!agent.getReasoner().isConsistent()){
				RemoveAxiom rem = new RemoveAxiom(ontology, axiom);
				agent.getOntologyManager().applyChange(rem);
				agent.getReasoner().refresh();
				return false;
			}
			
			return true;
		}catch(NotEnrichedException e){	
			getLogger().fine("... semantically-naive");
			return super.add(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught adding SELiteral "+l+" to belief base: ");
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	@Override
	public boolean remove(Literal l) {
		getLogger().fine("Removing "+l);
		try{
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
			agent.getSELiteralFactory().create(l); // just to check for enrichement
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



	public void setAgent(JasdlAgent agent) {
		this.agent = agent;
	}
	
	private Logger getLogger(){
		return agent.getLogger();
	}
	

}
