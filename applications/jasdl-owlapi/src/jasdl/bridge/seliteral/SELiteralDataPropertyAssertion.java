package jasdl.bridge.seliteral;

import static jasdl.util.Common.RANGE;
import static jasdl.util.Common.strip;
import static jasdl.util.Common.surroundedBy;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.xsd.XSDDataType;
import jasdl.bridge.xsd.XSDDataTypeUtils;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLTypedConstant;

public class SELiteralDataPropertyAssertion extends SELiteralPropertyAssertion{

	public SELiteralDataPropertyAssertion(Literal l, JasdlAgent agent) throws JasdlException {
		super(l, agent);
	}

	public SELiteralDataPropertyAssertion(SELiteral l) throws JasdlException {
		super(l);
	}
	
	public OWLDataProperty getPredicate() throws JasdlException{
		return (OWLDataProperty)toEntity();
	}
	
	public OWLTypedConstant getObject() throws JasdlException{		
		OWLOntology ontology = agent.getLabelManager().get(ontologyLabel);		
		OWLDataType typ = (OWLDataType)getPredicate().getRanges(ontology).toArray()[0];// will this always return exactly 1 range? If not, how should I deal with it
		XSDDataType wrapper = XSDDataTypeUtils.get(typ.toString());
		Term o = getTerm(RANGE);
		if(XSDDataTypeUtils.isStringType(wrapper)){
			if(!surroundedBy(o.toString(), "\"")){
				throw new InvalidSELiteralException("Data type mismatch on "+this);
			}
		}
		return agent.getOntologyManager().getOWLDataFactory().getOWLTypedConstant(strip(o.toString(), "\""), typ); // quotes stripped
	}
	

}
