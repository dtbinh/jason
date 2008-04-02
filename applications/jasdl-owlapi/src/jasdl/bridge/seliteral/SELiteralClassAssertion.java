package jasdl.bridge.seliteral;

import static jasdl.util.Common.DOMAIN;
import jasdl.asSemantics.JasdlAgent;
import jasdl.util.exception.JasdlException;
import jason.asSyntax.Literal;

import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;

public class SELiteralClassAssertion extends SELiteral{

	public SELiteralClassAssertion(Literal l, JasdlAgent agent) throws JasdlException {
		super(l, agent);
	}	

	public OWLDescription getOWLDescription() throws JasdlException{
		return (OWLDescription)toOWLObject();
	}
	
	public OWLIndividual getOWLIndividual() throws JasdlException{
		return getOWLIndividual(DOMAIN);
	}
	

}
