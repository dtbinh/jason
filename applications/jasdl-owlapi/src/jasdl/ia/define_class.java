/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package jasdl.ia;

import static jasdl.util.Common.strip;
import jasdl.asSemantics.JasdlAgent;
import jasdl.util.JasdlException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.logging.Logger;

/**
 * @author Tom Klapiscak
 * 
 * Usage jasdl.ia.define_class(classname, classexpr, label), where:
 * 	- classname is an atomic name used to refer to this class in future. Must begin with a lowercase letter and not clash with any AgentSpeak keyword
 *  - classexpr is a expression defining this class
 *  
 *  Changed class name to atom only - forces valid alias syntax
 *
 */
public class define_class extends DefaultInternalAction {


    private Logger logger = Logger.getLogger("jasdl."+define_class.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try { 	
        	// class name ATOM
        	Term _classname = args[0];
        	if(!_classname.isAtom()){
        		throw new JasdlException("first argument must be a Atom containing a valid class name");
        	}        		
        	Atom classname = (Atom)_classname;
        	
        	// concat expression strings / atoms, with limited validity checks
        	String expr = "";
        	for(int i = 1; i<args.length; i++){
        		if(args[i].isString()){
        			expr+=strip(args[i].toString(), "\"");
        		}else if(args[i].isAtom()){
        			expr+=args[i].toString();
        		}else{
        			throw new JasdlException("Invalid expression component: "+args[i]);
        		}
        	}        	
        	
        	JasdlAgent agent = (JasdlAgent)ts.getAg();
        	
        	agent.defineClass(classname, expr);
        	
 
            return true;
        } catch (Exception e) {
        	logger.warning("Error in internal action 'jasdl.ia.define_class'! Reason:");        	e.printStackTrace();
            
            return false;
        }       
    }
    
    
    
}