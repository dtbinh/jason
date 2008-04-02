package jasdl.bb.bbops.revision;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.exception.JasdlException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiom;

public class JasdlIncisionFunction implements IncisionFunction{	
	

	private JasdlAgent agent;
	private SELiteral toAddLiteral;

	/**
	 * The SELiteral to be added is required for a particularly inelegant hack that allows us
	 * to retrieve the annotations of the NOT YET ADDED axiom.
	 * 
	 * @param agent		required for getting trust ratings
	 * @param toAdd
	 * @param toAddAnnotations
	 */
	public JasdlIncisionFunction(JasdlAgent agent, SELiteral toAddLiteral){
		this.agent = agent;
		this.toAddLiteral = toAddLiteral;
	}
	
	/**
	 * Chooses from each akernel axiom with lowest trust associated with it.
	 * Currently calculated using ASSERTED source annotations only.
	 * No source annotation is taken as trust = 0;
	 */
	public Set<OWLAxiom> apply(Set<Set<OWLAxiom>> kernelset) {
		try{
			Set<OWLAxiom> chosen = new HashSet<OWLAxiom>();
			for(Set<OWLAxiom> akernel : kernelset){			
				if(!akernel.isEmpty()){
					OWLAxiom leastTrusted = null; // guaranteed to take a value 
					float minTrustRating = 1f;					
					for(OWLAxiom axiom : akernel){
						float trustRating = getTrustRating(axiom);
						agent.getLogger().finest("Trust rating of "+axiom+"="+trustRating);
						if(trustRating <= minTrustRating){
							minTrustRating = trustRating;
							leastTrusted = axiom;
						}
					}
					chosen.add(leastTrusted);
				}				
			}
			return chosen;
		}catch(Exception e){
			e.printStackTrace(); // TODO: introduce a seperate exception hierarchy once this package has been seperated from JASDL
			return null;
		}
	}
	
	/**
	 * Currently returns the trust rating of the most trusted source. Future work will look at better ways of calculating this.
	 * @param axiom
	 * @return
	 * @throws JasdlException
	 */
	private float getTrustRating(OWLAxiom axiom) throws JasdlException{
		ListTerm sources;
		if(axiom.equals(toAddLiteral.createAxiom())){ // hack to get the annotations of the NOT YET ADDED toAdd axiom
			sources = toAddLiteral.getLiteral().getSources();
		}else{
			SELiteral sl = agent.getAxiomToSELiteralConverter().convert((OWLIndividualAxiom)axiom); // kernel filter ensures axiom is an OWLIndividualAxiom
			sources = sl.getLiteral().getSources();
		}		
		agent.getLogger().finest("Sources of "+axiom+"="+sources);
		float maxTrustRating = 0f; // if no sources available, trust is 0
		for(Term source : sources){
			if(!source.isAtom()){
				throw new JasdlException("Invalid source annotation "+source);
			}
			Atom name = (Atom)source;
			float trustRating = agent.getTrustRating(name);
			if(trustRating > maxTrustRating){
				maxTrustRating = trustRating;
			}
		}		
		return maxTrustRating;
	}

	
}
