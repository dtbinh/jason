package jasdl.bridge.factory;

import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.JasdlException;
import jason.asSyntax.Atom;

/**
 * A convenient singleton factory providing methods to create JASDL alias objects.
 * 
 * @author Tom Klapiscak
 *
 */
public class AliasFactory {
	public static Atom OWL_NAMESPACE_LABEL = new Atom("owl");
	
	/**
	 * A singleton alias mapped to the universal owl:thing concept
	 */
	public static Alias OWL_THING = new Alias(new Atom("thing"), OWL_NAMESPACE_LABEL);
	
	/**
	 * A singleton alias mapped to the universal owl:nothing concept
	 */
	public static Alias OWL_NOTHING = new Alias(new Atom("nothing"), OWL_NAMESPACE_LABEL);
	
	
	public static Atom OWL_NOTHING_FUNCTOR = new Atom("nothing");
	public static Atom OWL_ALL_DIFFERENT_FUNCTOR = new Atom("all_different");	
	
	
	/**
	 * Singleton instance we should use.
	 */
	public static AliasFactory INSTANCE = new AliasFactory();
	
	private AliasFactory(){
	}
	
	/**
	 * Create an alias to represent an SELiteral. Literal functor becomes alias functor.
	 * Literal ontology label becomes alias label;
	 * If l is negated (and a unary class assertion) then functor is prefixed with "~".
	 * @param sl	the SELiteral the alias will represent
	 * @return		an alias representing the supplied SELiteral
	 */
	public Alias create(SELiteral sl) throws JasdlException{
		return new Alias( (sl.getLiteral().negated()?"~":"") + sl.getLiteral().getFunctor(), sl.getOntologyLabel());
	}
	
	/**
	 * Create an alias from an atomic functor and label
	 * @param functor
	 * @param label
	 * @return
	 */
	public Alias create(Atom functor, Atom label){
		return new Alias(functor, label);
	}
	
	public Alias all_different(Atom label){
		return new Alias(OWL_ALL_DIFFERENT_FUNCTOR, label);
	}
}
