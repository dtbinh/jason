package jasdl.bridge;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.xsd.XSDDataType;
import jasdl.bridge.xsd.XSDDataTypeUtils;
import jasdl.util.InvalidSELiteralException;
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

public class SELiteralConverter {
	private JasdlAgent agent;
	
	public SELiteralConverter(JasdlAgent agent){
		this.agent = agent;
	}
	
	
	/**
	 * Polymorphically applies appropriate factory method depending on specialisation of axiom
	 * @param axiom
	 * @return
	 * @throws JasdlException	if specialisation of axiom is not of an appropriate type for conversion to a SE-Literal
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
	
	public SELiteral convert(OWLClassAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().get(axiom.getDescription().asOWLClass());
		Literal l = construct(alias);
		Atom i = agent.getAliasManager().get(axiom.getIndividual()).getFunctor(); // TODO: what if individual is previously undefined? possible?
		l.addTerm(i);		
		return agent.getSELiteralFactory().create(l);		
	}
	
	public SELiteral convert(OWLObjectPropertyAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().get(axiom.getProperty().asOWLObjectProperty());
		Literal l = construct(alias);
		Atom s = agent.getAliasManager().get(axiom.getSubject()).getFunctor();
		l.addTerm(s);	
		Atom o = agent.getAliasManager().get(axiom.getObject()).getFunctor();
		l.addTerm(o);
		return agent.getSELiteralFactory().create(l);		
	}
	
	public SELiteral convert(OWLDataPropertyAssertionAxiom axiom) throws JasdlException{		
		Alias alias = agent.getAliasManager().get(axiom.getProperty().asOWLDataProperty());
		Literal l = construct(alias);
		Atom s = agent.getAliasManager().get(axiom.getSubject()).getFunctor();
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
	
	public SELiteral convert(OWLDifferentIndividualsAxiom axiom) throws JasdlException{
		Alias alias = agent.getAliasManager().get(AllDifferentPlaceholder.INSTANCE);
		Literal l = construct(alias);
		ListTerm list = new ListTermImpl(); // TODO: override this object's unify method to perform set, not list, unification?
		Set<OWLIndividual> is = axiom.getIndividuals();		
		for(OWLIndividual i : is){
			list.add(agent.getAliasManager().get(i).getFunctor());
		}
		l.addTerm(list);
		return agent.getSELiteralFactory().create(l);
	}
	
	
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