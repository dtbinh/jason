package jasdl.bb;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.NotEnrichedException;
import jason.asSyntax.Literal;
import jason.bb.DefaultBeliefBase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiom;

public class JasdlBeliefBase extends DefaultBeliefBase{
	
	private JasdlAgent agent;

	@Override
	public boolean add(Literal l) {
		getLogger().info("Adding "+l);
		try{
			SELiteral sl = agent.getSELiteralFactory().create(l);
			OWLIndividualAxiom axiom = sl.createAxiom();
			getLogger().info("... as axiom: "+axiom);
			AddAxiom change = new AddAxiom(sl.getOntology(), axiom);
			agent.getOntologyManager().applyChange(change);
			agent.getReasoner().refresh();
			return true;
		}catch(NotEnrichedException e){			
			return super.add(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught adding SELiteral "+l+" to belief base: ");
			e.printStackTrace();
			return false;
		}
	}
	
	

	@Override
	public Iterator<Literal> getRelevant(Literal l) {
		getLogger().info("Getting relevancies for "+l);
		try{
			Set<Literal> relevant = new HashSet<Literal>();
			SELiteral sl = agent.getSELiteralFactory().create(l);
			Set<OWLIndividualAxiom> axioms = sl.getAxioms();
			for(OWLIndividualAxiom axiom : axioms){
				SELiteral found = agent.getSELiteralConverter().convert(axiom);
				relevant.add(found);
			}
			getLogger().info("... found: "+relevant);
			return relevant.iterator();
		}catch(NotEnrichedException e){
			return super.getRelevant(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught getting relevancies for SELiteral "+l+" to belief base: ");
			e.printStackTrace();
			return null;
		}
	}



	public void setAgent(JasdlAgent agent) {
		this.agent = agent;
	}
	
	private Logger getLogger(){
		return agent.getLogger();
	}
	

}
