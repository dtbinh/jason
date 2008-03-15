package jasdl.bridge.seliteral;

import static jasdl.util.Common.DOMAIN;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.AliasFactory;
import jasdl.util.JasdlException;
import jasdl.util.UnknownMappingException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;

public class SELiteralClassAssertion extends SELiteral{

	public SELiteralClassAssertion(Literal l, JasdlAgent agent) throws JasdlException {
		super(l, agent);
	}

	public SELiteralClassAssertion(SELiteral l) throws JasdlException {
		super(l);
	}
	
	

	public OWLDescription getOWLDescription() throws JasdlException{
		return (OWLDescription)toOWLObject();
	}
	
	public OWLIndividual getOWLIndividual() throws JasdlException{
		return getOWLIndividual(DOMAIN);
	}
	

}
