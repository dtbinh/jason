package jasdl.bridge.seliteral;

import static jasdl.util.Common.DOMAIN;

import org.semanticweb.owl.model.OWLIndividual;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.factory.AliasFactory;
import jasdl.util.exception.JasdlException;
import jason.asSyntax.Literal;

public abstract class SELiteralPropertyAssertion extends SELiteral{

	public SELiteralPropertyAssertion(Literal l, JasdlAgent agent) throws JasdlException {
		super(l, agent);
	}

	
	
	public OWLIndividual getSubject() throws JasdlException{
		return getOWLIndividual(DOMAIN);
	}


}
