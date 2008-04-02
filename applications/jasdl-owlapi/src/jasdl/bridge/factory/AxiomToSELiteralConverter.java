package jasdl.bridge.factory;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.mapping.aliasing.AllDifferentPlaceholder;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.exception.InvalidSELiteralException;
import jasdl.util.exception.JasdlException;
import jasdl.util.exception.UnknownMappingException;
import jasdl.util.owlapi.xsd.XSDDataType;
import jasdl.util.owlapi.xsd.XSDDataTypeUtils;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
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
public class AxiomToSELiteralConverter {
	
	/**
	 * The agent this converter is working on behalf of. Required for access to managers.
	 */
	private JasdlAgent agent;
	
	public AxiomToSELiteralConverter(JasdlAgent agent){
		this.agent = agent;
	}
	
	/**
	 * Fetches and deseralises and returns all ASSERTED annotations of the supplied axiom.
	 * TODO: Shift to a factory?
	 * @param sl
	 */
	public static List<Term> getAssertedAnnotations(OWLAxiom axiom, OWLOntology ontology){
		List<Term> result = new Vector<Term>();
		// get annotations
		Set<OWLAxiomAnnotationAxiom> annotAxioms = axiom.getAnnotationAxioms(ontology);
		for(OWLAxiomAnnotationAxiom annotAxiom : annotAxioms){ // remember, possibly semantically-naive payload!
			Term annot = Literal.parse(annotAxiom.getAnnotation().getAnnotationValueAsConstant().getLiteral());
			result.add(annot);
		}
		return result;
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
		Atom individual = agent.getAliasManager().getLeft(axiom.getIndividual()).getFunctor(); // TODO: what if individual is previously undefined? possible?		
		return agent.getSELiteralFactory().construct(alias, individual, getAnnots(alias, axiom));
	}
	
	/**
	 * Convert an axiom asserting that two individuals are related by an object property to a binary SELiteral
	 * @param axiom		the axiom to convert
	 * @return			an binary SELiteral encoding of axiom
	 * @throws JasdlException
	 */	
	public SELiteral convert(OWLObjectPropertyAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().getLeft(axiom.getProperty().asOWLObjectProperty());
		Atom subject = agent.getAliasManager().getLeft(axiom.getSubject()).getFunctor();
		Atom object = agent.getAliasManager().getLeft(axiom.getObject()).getFunctor();
		return agent.getSELiteralFactory().construct(alias, subject, object, getAnnots(alias, axiom));
	}

	/**
	 * Convert an axiom asserting that two individuals are related by a data property to a binary SELiteral
	 * @param axiom		the axiom to convert
	 * @return			a binary SELiteral encoding of axiom
	 * @throws JasdlException
	 */	
	public SELiteral convert(OWLDataPropertyAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().getLeft(axiom.getProperty().asOWLDataProperty());
		Atom subject = agent.getAliasManager().getLeft(axiom.getSubject()).getFunctor();		
		Term object;
		OWLConstant constant = axiom.getObject();
		if(constant.isTyped()){
			OWLTypedConstant ot = constant.asOWLTypedConstant();
			XSDDataType xsd = XSDDataTypeUtils.get(ot.getDataType().toString());
			// surround with quotes if necessary for representation in AgentSpeak syntax
			if(XSDDataTypeUtils.isStringType(xsd)){
				object = DefaultTerm.parse("\""+constant.getLiteral().toString()+"\"");
			}else if(XSDDataTypeUtils.isBooleanType(xsd)){
				if(Boolean.parseBoolean(ot.getLiteral().toString())){
					object = Literal.LTrue;
				}else{
					object = Literal.LFalse;
				}				
			}else{
				object = DefaultTerm.parse(constant.getLiteral().toString());
			}
		}else{
			throw new JasdlException("JASDL does not support untyped data ranges such as: "+axiom);
		}
		return agent.getSELiteralFactory().construct(alias, subject, object, getAnnots(alias, axiom));		
	}
	
	/**
	 * Convert an axiom asserting that a set of individuals are distinct to a unary SELiteral (whose term is a list and functor is "all_different")
	 * @param axiom		the axiom to convert
	 * @return			a unary SELiteral (whose term is a list and functor is "all_different") encoding of axiom
	 * @throws JasdlException
	 */	
	public SELiteral convert(OWLDifferentIndividualsAxiom axiom) throws JasdlException{	
		Set<OWLIndividual> _is = axiom.getIndividuals();
		if(_is.size() == 0){
			throw new InvalidSELiteralException("All different assertion must contain some individuals! "+axiom);
		}
		Atom[] is = new Atom[_is.size()];
		Atom label = null;
		int j = 0;
		for(OWLIndividual i : _is){
			Alias iAlias = agent.getAliasManager().getLeft(i);
			if(label == null){
				// hack, get a reference back to ontology by examining one of the individuals
				label = iAlias.getLabel();
			}
			is[j] = iAlias.getFunctor();
			j++;
		}		
		Alias alias = agent.getAliasManager().getLeft(new AllDifferentPlaceholder(label));
		return agent.getSELiteralFactory().construct(alias, is, getAnnots(alias, axiom));
	}
	
	/**
	 * Utility method to get the annotations of an axiom as an array of terms within the ontology referenced by alias
	 * @param alias
	 * @param axiom
	 * @return
	 * @throws UnknownMappingException
	 */
	private Term[] getAnnots(Alias alias, OWLAxiom axiom) throws UnknownMappingException{
		OWLOntology ontology = agent.getLabelManager().getRight(alias.getLabel());	
		List<Term> collection = getAssertedAnnotations(axiom, ontology);
		return (Term[])collection.toArray(new Term[collection.size()]);
	}	
	

}
