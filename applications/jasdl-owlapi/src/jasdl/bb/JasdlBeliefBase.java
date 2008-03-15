package jasdl.bb;

import jasdl.asSemantics.JasdlAgent;
import jasdl.asSyntax.SELiteral;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jason.asSyntax.Literal;
import jason.bb.DefaultBeliefBase;

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
			return true;
		}catch(NotEnrichedException e){			
			return super.add(l); // semantically-naive, use standard Jason mechanisms
		}catch(Exception e){
			getLogger().warning("Exception caught adding SELiteral "+l+" to belief base: ");
			e.printStackTrace();
			return false;
		}
	}

	public void setAgent(JasdlAgent agent) {
		this.agent = agent;
	}
	
	private Logger getLogger(){
		return agent.getLogger();
	}
	

}
