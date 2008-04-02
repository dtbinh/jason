package jasdl.bridge.mapping.aliasing;

import jason.asSyntax.Atom;

public class Alias {
	private Atom functor;
	private Atom label;
	
	public Alias(String functor, Atom label){
		this( new Atom(functor), label);
	}
	
	public Alias(Atom functor, Atom label) {
		super();
		this.functor = functor;
		this.label = label;
	}

	@Override
	public boolean equals(Object _other) {
		if(!(_other instanceof Alias)){
			return false;
		}
		Alias other = (Alias)_other;
		return functor.equals(other.functor) && label.equals(other.label);
	}
	
	@Override
	public int hashCode(){
		return (functor.getFunctor()+label.getFunctor()).hashCode();
	}

	@Override
	public String toString() {
		return functor+"["+label+"]";
	}
	
	public boolean isAllDifferent(){
		return functor.equals(AliasFactory.OWL_ALL_DIFFERENT_FUNCTOR);
	}

	public Atom getFunctor() {
		return functor;
	}

	public Atom getLabel() {
		return label;
	}
	
	
	
}
