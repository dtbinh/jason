package jasdl.bridge.seliteral;

import static jasdl.util.Common.DOMAIN;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.AllDifferentPlaceholder;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jason.asSyntax.Literal;

import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;

public class SELiteralFactory {

	private JasdlAgent agent;
	
	public SELiteralFactory(JasdlAgent agent){
		this.agent = agent;
	}
	
	/**
	 * Polymorphically creates a specific type of SELiteral based on the properties of the supplied literal
	 * @param l
	 * @return
	 * @throws JasdlException
	 */
	public SELiteral create(Literal l) throws JasdlException{
		SELiteral sl = new SELiteral(l, agent); // so we can use convenience methods on a processed seliteral
		OWLObject entity = sl.toOWLObject();
		if(sl.getLiteral().getArity() == 1){
			if(entity instanceof OWLDescription){
				return sl.asClassAssertion();
			}else if(entity instanceof AllDifferentPlaceholder){
				//if(!l.isGround()) throw new JasdlException("JASDL does not currently support unground all_different assertions such as "+l);
				// TODO: can ensure this here (due to unground TG all_different literals), where should I? axiom converter?
				if(l.negated()) throw new JasdlException("JASDL does not currently support negated all_different assertions such as "+l+", since OWL makes the UNA by default and JASDL doesn't allow this to be overridden");
				return sl.asAllDifferentAssertion();
			}else{
				throw new InvalidSELiteralException(sl+" does not refer to a known class or an all_different assertion");
			}
		}else if(sl.getLiteral().getArity() == 2){
			if(sl.getLiteral().negated()) throw new JasdlException("JASDL does not currently support negated property assertions such as "+sl);
			if(!sl.getLiteral().getTerm(DOMAIN).isGround()) throw new JasdlException("JASDL cannot handle left-unground property assertions such as "+sl);
			if(entity instanceof OWLObjectProperty){
				return sl.asObjectPropertyAssertion();
			}else if(entity instanceof OWLDataProperty){
				return sl.asDataPropertyAssertion();
			}else{
				throw new InvalidSELiteralException(sl+" does not refer to a known object or data property");
			}
		}else{
			throw new InvalidSELiteralException(sl+" must be either unary or binary");
		}
	}
}
