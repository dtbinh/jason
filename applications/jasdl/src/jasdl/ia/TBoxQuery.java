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

import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.List;

import com.hp.hpl.jena.ontology.OntResource;

/**
 * Simply combines jasdl.ia.parents and jasdl.ia.children into one, since most code is the same
 * @author Tom Klapiscak
 *
 */
class TBoxQuery {
	public static boolean PARENTS  = true;
	public static boolean CHILDREN = false;
	
	@SuppressWarnings("unchecked")
	public static void perform(TransitionSystem ts, Unifier un, Term[] args, boolean type) throws JasdlException{
    	if(args.length < 3){
    		throw new JasdlException("Must supply at least three arguments");
    	}
    	Term resName = args[1];
    	Term alias = args[2];        	
    	boolean direct = false;
    	if(args.length == 4){
    		if(args[3].toString().equals("true")){
    			direct = true;
    		}
    	}
    	        	        	
    	if(!alias.isAtom()){
    		throw new JasdlException("second argument must be an atom representing an ontology alias");
    	}
    	
    	JasdlOntology ont = OntologyManager.getOntologyManager(ts.getAg()).getJasdlOntology(alias.toString());
    	if(ont==null){
    		throw new JasdlException("Unknown ontology alias: "+alias);
    	}
    	
    	if(!resName.isLiteral()){
    		throw new JasdlException("first argument must a predicate representing either a class or property in the ontology referred to by the second argument");
    	}
    	
    	//OntResource res = ont.getOntResourceFromPred((Pred)resName);
    	OntResource res = ont.getModel().getOntResource(ont.getReal((Literal)resName).toString());
    	List result = null;
    	if(type == PARENTS){
    		result = ont.listOntResourceParents(res, direct);
    	}else{
    		result = ont.listOntResourceChildren(res, direct);
    	}
    	
    	ListTerm list = new ListTermImpl();
    	for(Object _relative : result){
    		OntResource relative = (OntResource)_relative;
    		list.add( new Atom(ont.toAlias(URI.create(relative.getURI())).toString()) ); 
    	}
    	
    	un.unifies(args[0], list);		
	}
}
