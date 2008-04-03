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

import jasdl.asSemantics.JasdlAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
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
public class set_belief_revision_enabled extends DefaultInternalAction {


    private Logger logger = Logger.getLogger("jasdl."+set_belief_revision_enabled.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	JasdlAgent agent = (JasdlAgent)ts.getAg();
        	if(args[0].equals(Literal.LTrue)){
        		agent.setBeliefRevisionEnabled(true);
        	}else if(args[0].equals(Literal.LFalse)){
        		agent.setBeliefRevisionEnabled(false);
        	}else{
        		throw new Exception("Argument must be boolean");
        	}
        	return true; 
        } catch (Exception e) {
        	logger.warning("Error in internal action 'jasdl.ia.set_belief_revision_enabled'! Reason:");
        	e.printStackTrace();            
            return false;
        }       
    }
    
    
    
}