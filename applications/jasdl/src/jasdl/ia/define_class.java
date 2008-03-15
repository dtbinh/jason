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
import jasdl.bridge.JasdlOntology;
import jasdl.bridge.alias.Alias;
import jasdl.util.JasdlException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * @author Tom Klapiscak
 * 
 * Usage jasdl.ia.define_class(classname, classexpr, label), where:
 * 	- classname is an atomic name used to refer to this class in future. Must begin with a lowercase letter and not clash with any AgentSpeak keyword
 *  - classexpr is a expression defining this class
 *  - label is the label of the ontology this new class definition belongs in
 *  
 *  TODO: Allow (force??) use of aliases within expressions
 *  
 *  Changed class name to atom only - forces valid alias
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
        	for(int i = 1; i<args.length-1; i++){
        		if(args[i].isString()){
        			expr+=strip(args[i].toString(), "\"");
        		}else if(args[i].isAtom()){
        			expr+=args[i].toString();
        		}else{
        			throw new JasdlException("Invalid expression component: "+args[i]);
        		}
        	}

        	Term _label = args[args.length-1];
        	if(!_label.isAtom()){
        		throw new JasdlException("third argument must be an existing atomic ontology alias");
        	}
        	Atom label = (Atom)args[args.length-1];     
        	
        	
        	JasdlAgent agent = (JasdlAgent)ts.getAg();
        	
        	JasdlOntology ont = agent.getOntology(label);  
        	
        	logger.finest("Compiling expression: "+expr);
        	
        	expr = compileExpression(expr, ont);
        	
        	logger.finest("... result: "+expr);
        	
        	logger.finest("Compiled to: "+ont.defineClass(classname, expr, new Atom(agent.getAgentName())));   
        	
    
        	
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'jasdl.ia.define_class'! "+e);
            return false;
        }       
    }
    
    
    
	
    /** 
     * Replace all alias predicates with full uris
     * Prepares a statement for compilation with clexer
     * 
     * Would potentially be too expensive to iterate through ALL known ontological entities, so a search is performed
     * to see which to search + replace
     * 
     * Ideally, this will should be written as parser and lexer using antlr
     */
	
    private String compileExpression(String expr, JasdlOntology ont) throws JasdlException{
    	List<Literal> alreadyReplaced = new Vector<Literal>();
    	String newExpr = expr;
    	expr = expr.replace("{", " ");
    	expr = expr.replace("}", " ");
    	expr = expr.replace(",", " ");
    	String[] tokens = expr.split("[ ]");
    	for(String token : tokens){
    		
    		// strip leading and ending brackets
    		while(token.startsWith("(")){ token = token.substring(1); }    		
    		while(token.endsWith(")")){ token = token.substring(0, token.length() - 1);	}    		

    		if(token.length()>0){
    			if(!keywords.contains(token)){    				
    				Literal l = (Literal)Literal.parse(token);
    	    		Alias alias = ont.toAlias(l);    				
    				if(!alreadyReplaced.contains(l)){    				
    					if(ont.isMapped( alias )){ // an ontological entity of some kind
	    					alreadyReplaced.add(l);
			    			try {
								newExpr = newExpr.replace(token, ont.toExpr(alias) );
							} catch (RuntimeException e) {
								throw new JasdlException("Error precompiling expression "+newExpr+" on token "+token+". Reason: "+e);
							}
    					}
		    		}
    			}
    		}
    	}
    	return newExpr;
    }
    
    private static Collection<String> keywords = new HashSet<String>();
    static{
    	Collections.addAll(keywords, new String[] {
    			"and",
    			"some",
    			"value",
    			"not",
    			"or",
    			"all",
    			"min",
    			"max",
    			"exactly"
    	});
    }
    
    
}