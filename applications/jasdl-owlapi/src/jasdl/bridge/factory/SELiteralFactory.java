package jasdl.bridge.factory;

import static jasdl.util.Common.DOMAIN;

import java.util.Arrays;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.mapping.aliasing.AllDifferentPlaceholder;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.exception.InvalidSELiteralException;
import jasdl.util.exception.JasdlException;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

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
	public SELiteral construct(Literal l) throws JasdlException{
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
	
	

	
	public SELiteral construct(boolean sign, Atom functor, Term[] terms, Term[] annots, Atom label) throws JasdlException{
		Literal l = new Literal(sign, functor);	
		
		// add "o"
		Structure o = new Structure(SELiteral.ONTOLOGY_ANNOTATION_FUNCTOR);
		o.addTerm(label);
		l.addAnnot(o);
		
		l.addTerms(Arrays.asList(terms));
		if(annots!=null){
			l.addAnnots(Arrays.asList(annots));
		}
		if(l.negated()){
			if(
			 (l.getFunctor().equals("thing") || l.getFunctor().equals("nothing")) ||		// special case: reject ~thing and ~nothing
			 (l.getArity() == 2) ||															//special case: reject negated property assertions
			 (l.getFunctor().equals(AliasFactory.OWL_ALL_DIFFERENT_FUNCTOR.getFunctor()))	// special case: reject ~all_different assertions
			 ){
				throw new InvalidSELiteralException(l+" is invalid");
			}
		}
		return construct(l);
	}
	
	public SELiteral construct(boolean sign, Atom functor, Atom individual, Term[] annots, Atom label) throws JasdlException{
		return construct(sign, functor, new Term[] {individual}, annots, label);
	}
	
	public SELiteral construct(boolean sign, Atom functor, Atom subject, Term object, Term[] annots, Atom label) throws JasdlException{
		return construct(sign, functor, new Term[] {subject, object}, annots, label);
	}	
	
	public SELiteral construct(boolean sign, Atom[] _is, Term[] annots, Atom label) throws JasdlException{
		ListTerm is = new ListTermImpl();// TODO: override this object's unify method to perform set, not list, unification + equality?	
		for(Atom i : _is){
			is.add(i);
		}
		return construct(sign, AliasFactory.OWL_ALL_DIFFERENT_FUNCTOR, new Term[] {is}, annots, label);
	}
	
	
	/**
	 * Common SELiteral construction code: sets functor, negation (based on presence of "~" prefix) and ontology annotation.
	 * Results in a SELiteral with no arguments.
	 * @param alias		the alias from which to construct this SELiteral
	 * @return			an SELiteral corresponding to alias with no arguments
	 */	
	public SELiteral construct(Alias alias, Term[] terms, Term[] annots) throws JasdlException{
		// construct a new literal (with no terms) based on alias
		boolean sign = true;
		String functor = alias.getFunctor().toString();		
		 //~ might be present
		if(functor.startsWith("~")){
			functor = functor.substring(1);
			sign = false;
		}		
		return construct(sign, new Atom(functor), terms, annots, alias.getLabel());
	}		
	
	
	public SELiteral construct(Alias alias, Atom individual, Term[] annots) throws JasdlException{	
		return construct(alias, new Term[] {individual}, annots);
	}
	
	public SELiteral construct(Alias alias, Atom subject, Term object, Term[] annots) throws JasdlException{	
		return construct(alias, new Term[] {subject, object}, annots);
	}
	
	public SELiteral construct(Alias alias, Atom[] _is, Term[] annots) throws JasdlException{
		ListTerm is = new ListTermImpl();// TODO: override this object's unify method to perform set, not list, unification + equality?	
		for(Atom i : _is){
			is.add(i);
		}
		return construct(alias, new Term[] {is}, annots);
	}		
}
