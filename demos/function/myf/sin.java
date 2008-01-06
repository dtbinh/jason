// User defined function for project function.mas2j

package myf;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class sin extends ArithFunction {

	public sin() {		
		super(sin.class.getName(), // the name of the function as must be used in AS code, in this case is the name of the class
		      1);                  // max number of arguments
	}
		
	private sin(sin a) { // used by clone
		super(a);
	}
	
	@Override
	public double evaluate(Term[] args) throws JasonException {
		if (args[0].isNumeric()) {
			double n = ((NumberTerm)args[0]).solve(); // get the first argument
			return Math.sin(n);
		} else {
			throw new JasonException("The argument '"+args[0]+"' is not numeric!");
		}
	}

	@Override
	public boolean checkArity(int a) { // should return true if a is a valid number of arguments
		return a == 1;
	}
	
	@Override
	public Object clone() {
        if (isEvaluated()) {
            return getValue();
        } else {
        	return new sin(this);
        }
	}
}

