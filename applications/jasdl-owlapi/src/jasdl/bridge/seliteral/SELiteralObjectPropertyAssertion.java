package jasdl.bridge.seliteral;

import static jasdl.util.Common.DOMAIN;
import static jasdl.util.Common.RANGE;
import jasdl.asSemantics.JasdlAgent;
import jasdl.util.JasdlException;
import jason.asSyntax.Literal;

import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;

public class SELiteralObjectPropertyAssertion extends SELiteralPropertyAssertion{

	public SELiteralObjectPropertyAssertion(Literal l, JasdlAgent agent) throws JasdlException {
		super(l, agent);
	}

	
	public OWLObjectProperty getPredicate() throws JasdlException{
		return (OWLObjectProperty)toOWLObject();
	}

	
	public OWLIndividual getObject() throws JasdlException{
		return getOWLIndividual(RANGE);
	}	
	
	
}
