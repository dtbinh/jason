package jasdl.bridge;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.xsd.XSDDataType;
import jasdl.bridge.xsd.XSDDataTypeUtils;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.util.Set;

import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;


/**
 * Accepts an arbitrary OWLIndividualAxiom and, depending on its type, creates a SE-Literal encoding of it.
 * Will always result in a ground SELiteral (since OWLIndividualAxioms must always be ground)<p>
 * For example:
 * <ul>
 * 	<li>ClassAssertion(Hotel hilton) -> hotel(hilton)[o(travel)]</li>
 *  <li>ObjectPropertyAssertion(hasRating hilton ThreeStarRating) -> hasRating(hilton,threeStarRating)[o(travel)]</li>
 *  <li>DifferentIndividuals( fourSeasons hilton ) -> all_different([hilton,fourSeasons])[o(travel)]</li>  
 * </ul>
 * @author Tom Klapiscak
 *
 */
public class ToSELiteralConverter {
	
	/**
	 * The agent this converter is working on behalf of. Required for access to managers.
	 */
	private JasdlAgent agent;
	
	public ToSELiteralConverter(JasdlAgent agent){
		this.agent = agent;
	}
	
	
	/**
	 * Polymorphically applies appropriate factory method depending on specialisation of axiom
	 * @param axiom
	 * @return
	 * @throws JasdlException	if specialisation of axiom is not of an appropriate type for conversion to a SELiteral
	 */
	public SELiteral convert(OWLIndividualAxiom axiom) throws JasdlException{
		if(axiom instanceof OWLClassAssertionAxiom){
			return convert((OWLClassAssertionAxiom)axiom);			
		}else if(axiom instanceof OWLObjectPropertyAssertionAxiom){
			return convert((OWLObjectPropertyAssertionAxiom)axiom);			
		}else if(axiom instanceof OWLDataPropertyAssertionAxiom){
			return convert((OWLDataPropertyAssertionAxiom)axiom);	
		}else if(axiom instanceof OWLDifferentIndividualsAxiom){
			return convert((OWLDifferentIndividualsAxiom) axiom);
		}else{
			throw new JasdlException(axiom+" is not of an appropriate type for conversion to a SELiteral");
		}
	}	
	
	/**
	 * Convert an axiom asserting class membership to a unary SELiteral
	 * @param axiom		the axiom to convert
	 * @return			a unary SELiteral encoding of axiom
	 * @throws JasdlException
	 */
	public SELiteral convert(OWLClassAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().getLeft(axiom.getDescription());
		Literal l = construct(alias);
		Atom i = agent.getAliasManager().getLeft(axiom.getIndividual()).getFunctor(); // TODO: what if individual is previously undefined? possible?
		l.addTerm(i);		
		return agent.getSELiteralFactory().create(l);		
	}
	
	/**
	 * Convert an axiom asserting that two individuals are related by an object property to a binary SELiteral
	 * @param axiom		the axiom to convert
	 * @return			an binary SELiteral encoding of axiom
	 * @throws JasdlException
	 */	
	public SELiteral convert(OWLObjectPropertyAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().getLeft(axiom.getProperty().asOWLObjectProperty());
		Literal l = construct(alias);
		Atom s = agent.getAliasManager().getLeft(axiom.getSubject()).getFunctor();
		l.addTerm(s);	
		Atom o = agent.getAliasManager().getLeft(axiom.getObject()).getFunctor();
		l.addTerm(o);
		return agent.getSELiteralFactory().create(l);		
	}

	/**
	 * Convert an axiom asserting that two individuals are related by a data property to a binary SELiteral
	 * @param axiom		the axiom to convert
	 * @return			a binary SELiteral encoding of axiom
	 * @throws JasdlException
	 */	
	public SELiteral convert(OWLDataPropertyAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().getLeft(axiom.getProperty().asOWLDataProperty());
		Literal l = construct(alias);
		Atom s = agent.getAliasManager().getLeft(axiom.getSubject()).getFunctor();
		l.addTerm(s);
		
		Term o;
		OWLConstant constant = axiom.getObject();
		if(constant.isTyped()){
			OWLTypedConstant ot = constant.asOWLTypedConstant();
			XSDDataType xsd = XSDDataTypeUtils.get(ot.getDataType().toString());
			// surround with quotes if necessary for representation in AgentSpeak syntax
			if(XSDDataTypeUtils.isStringType(xsd)){
				o = DefaultTerm.parse("\""+constant.getLiteral().toString()+"\"");
			}else if(XSDDataTypeUtils.isBooleanType(xsd)){
				if(Boolean.parseBoolean(ot.getLiteral().toString())){
					o = Literal.LTrue;
				}else{
					o = Literal.LFalse;
				}				
			}else{
				o = DefaultTerm.parse(constant.getLiteral().toString());
			}
		}else{
			throw new JasdlException("JASDL does not support untyped data ranges such as: "+axiom);
		}
		
		l.addTerm(o);
		return agent.getSELiteralFactory().create(l);		
	}
	
	/**
	 * Convert an axiom asserting that a set of individuals are distinct to a unary SELiteral (whose term is a list and functor is "all_different")
	 * @param axiom		the axiom to convert
	 * @return			a unary SELiteral (whose term is a list and functor is "all_different") encoding of axiom
	 * @throws JasdlException
	 */	
	public SELiteral convert(OWLDifferentIndividualsAxiom axiom) throws JasdlException{
		ListTerm list = new ListTermImpl(); // TODO: override this object's unify method to perform set, not list, unification?		
		Set<OWLIndividual> is = axiom.getIndividuals();
		if(is.size() == 0){
			throw new JasdlException("All different assertion must contain some individuals! "+axiom);
		}
		Alias iAlias = null;
		for(OWLIndividual i : is){
			iAlias = agent.getAliasManager().getLeft(i);
			list.add(iAlias.getFunctor());
		}
		// hack, get a reference back to ontology by examining one of the individuals		
		Alias alias = agent.getAliasManager().getLeft(new AllDifferentPlaceholder(iAlias.getLabel()));
		Literal l = construct(alias);		
		
		l.addTerm(list);
		return agent.getSELiteralFactory().create(l);
	}
	

	/**
	 * Common SELiteral construction code: sets functor, negation (based on presence of "~" prefix) and ontology annotation.
	 * Results in a SELiteral with no arguments.
	 * @param alias		the alias from which to construct this SELiteral
	 * @return			an SELiteral corresponding to alias with no arguments
	 */
	private Literal construct(Alias alias){
		// construct a new literal (with no terms) based on alias
		boolean sign = true;
		String functor = alias.getFunctor().toString();		
		 //~ might be present
		if(functor.startsWith("~")){
			functor = functor.substring(1);
			sign = false;
		}			
		Literal l = new Literal(sign, functor);
		
		// add ontology annotation
		Structure o = new Structure(SELiteral.ONTOLOGY_ANNOTATION_FUNCTOR);
		o.addTerm(alias.getLabel());		
		l.addAnnot(o);
		
		return l;
	}
}
