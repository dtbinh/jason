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
import static jasdl.util.Common.stripAll;
import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * @author Tom Klapiscak
 * 
 * Usage jasdl.ia.define_class(classname, classexpr, alias), where:
 * 	- classname is a name used to refer to this class in future. Must begin with a lowercase letter and not clash with any AgentSpeak keyword
 *  - classexpr is a expression defining this class
 *  - alias is the alias of the ontology this new class definition belongs in
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
        	String classname = _classname.toString();
        	
        	// concat expression strings / atoms, with limited validty checks
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

        	Term _alias = args[args.length-1];
        	if(!_alias.isAtom()){
        		throw new JasdlException("third argument must be an existing atomic ontology alias");
        	}
        	String alias = args[args.length-1].toString();     
        	
        	OntologyManager manager = OntologyManager.getOntologyManager(ts.getAg());       	
        	
        	JasdlOntology ont = manager.getJasdlOntology(alias);    
        	
        	expr = precompileExpression(expr, ont);
        	logger.info("Defining class: "+expr);
        	ont.defineClass(classname, expr, manager.getAgentName());       	
        	
            return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'jasdl.ia.define_class'! "+e);
            return false;
        }       
    }
    
    
    
	
    /** 
     * Replace all alias predicates with full uris
     * Prepares a statement for compilation with joce
     * 
     * Would potentially be too expensive to iterate through ALL known ontological entities, so a search is performed
     * to see which to search + replace
     * 
     * Ideally, this will should be written as parser and lexer using antlr
     */
	
    private String precompileExpression(String expr, JasdlOntology ont) throws JasdlException{
    	List<String> alreadyReplaced = new Vector<String>();
    	String newExpr = expr;
    	expr = expr.replace("{", " ");
    	expr = expr.replace("}", " ");
    	expr = expr.replace(",", " ");
    	String[] tokens = expr.split("[ ]");
    	for(String token : tokens){
    		// trim brackets from token - can be done since all brackets encased with square brackets!
    		token = stripAll(token, "(");
    		token = stripAll(token, ")");
    		token = stripAll(token, " ");
    		if(token.length()>0){
    			if(!alreadyReplaced.contains(token)){
		    		if(!keywords.contains(token)){ // a ontological entity of some kind
		    			alreadyReplaced.add(token);
		    			try {
		    				Literal p = null;
							p = Literal.parseLiteral(token);
							URI real = ont.getReal(p);
							newExpr = newExpr.replace(p.toString(), "|"+real.toString()+"|");
						} catch (RuntimeException e) {
							throw new JasdlException("Error precompiling expression "+newExpr+" on token "+token+". Reason: "+e);
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