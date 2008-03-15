package jasdl.bridge.alias;

import jasdl.bridge.seliteral.SELiteral;
import jason.asSyntax.Atom;

public class AliasFactory {
	public static Atom OWL_THING_FUNCTOR = new Atom("thing");
	public static Atom OWL_NOTHING_FUNCTOR = new Atom("nothing");
	public static Atom OWL_ALL_DIFFERENT_FUNCTOR = new Atom("all_different");
	
	public static Atom OWL_NAMESPACE_LABEL = new Atom("owl");
	
	public static AliasFactory INSTANCE = new AliasFactory();
	
	public Alias create(SELiteral l){
		return new Alias(l.getFunctor(), l.getOntologyLabel());
	}
	
	public Alias create(Atom functor, Atom label){
		return new Alias(functor, label);
	}
	
	public Alias thing(){
		
		return new Alias(OWL_THING_FUNCTOR, OWL_NAMESPACE_LABEL);
	}
	
	public Alias nothing(){
		return new Alias(OWL_NOTHING_FUNCTOR, OWL_NAMESPACE_LABEL);
	}
	
	public Alias all_different(Atom label){
		return new Alias(OWL_ALL_DIFFERENT_FUNCTOR, label);
	}
}
