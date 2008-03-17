package jasdl.bridge.seliteral;

import jasdl.asSemantics.JasdlAgent;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLIndividual;

public class SELiteralAllDifferentAssertion extends SELiteral{

	public SELiteralAllDifferentAssertion(Literal l, JasdlAgent agent) throws JasdlException {
		super(l, agent);
	}
	
	/**
	 * Validates since terms are mutable
	 * @return
	 */
	public Set<OWLIndividual> getOWLIndividuals() throws JasdlException{
		if(!literal.getTerm(0).isList()) throw new InvalidSELiteralException("The first term of an all_different assertion must be a list");
		ListTerm list = (ListTerm)literal.getTerm(0);
		Set<OWLIndividual> is = new HashSet<OWLIndividual>();
		for(Term i : list){
			is.add(getOWLIndividual(i));
		}
		return is;
	}
	

}
