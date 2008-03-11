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
package jasdl.util;

import jasdl.bridge.JasdlOntology;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;

public class Common {
	public static String ONTOLOGY_ANNOTATION = "o";	
	public static String EXPR_ANNOTATION = "expr";	
	public static String ORIGIN_ANNOTATION = "origin";
	public static String URI_ANNOTATION = "uri";
	
	
	public static String DELIM=",";
	public static int DOMAIN = 0;
	public static int RANGE = 1;
	
	
	/**
	 * Prefix to use when creating unique ontology labels
	 */
	public static String ANON_ONTOLOGY_LABEL_PREFIX = "_ontology_";
	
	public static boolean surroundedBy(String text, String match){
		return text.startsWith(match) && text.endsWith(match);
	}
	
	public static String strip(String text, String remove){
		if(text == null){ return null; }
		if(surroundedBy(text, remove)){
			return text.substring(remove.length(), text.length() - remove.length());
		}else{
			return text;
		}
	}
	
	
	public static String localName(URI uri){
		String s = uri.toString();		
		return s.substring(s.lastIndexOf("#")+1, s.length());
	}
	
	/**
	 * Returns all annotation terms with the given functor
	 * @param functor
	 * @return
	 */
	public static List<Structure> getAnnots(Term _term, String functor){
		Vector<Structure> terms = new Vector<Structure>();
		if(_term.isPred()){
			Pred term = (Pred)_term;			
			ListTerm annots = term.getAnnots();
			if(annots != null){
				for(Term _annot : annots){
					if(_annot.isStructure()){
						Structure annot = (Structure)_annot;
						if(annot.getFunctor().equals(functor)){
							terms.add(annot);
						}
					}
				}
			}
		}
		return terms;
	}
	
	/**
	 * Use when there should only be a single annotation present
	 * 
	 * @param predicate
	 * @param functor
	 * @return
	 */
	public static Structure getAnnot(Term term, String functor){
		List<Structure> terms = getAnnots(term, functor);
		if(terms.size() == 0){
			return null;
		}else{
			return terms.get(0);
		}
	}
	
	
	public static Structure getOntologyAnnotation(Literal l) throws JasdlException{
		Structure o = getAnnot(l, ONTOLOGY_ANNOTATION);
		if(o == null){
			throw new InvalidSELiteralException("No ontology annotation present on "+l);
		}
		return o;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Returns true iff l is satisfies sufficient conditions to be a class assertion
	 * @param l
	 * @param ont
	 * @return
	 */
	public static boolean isClassAssertion(Literal l, JasdlOntology ont){
		if(l.getArity() != 1) return false;	
		return true;
	}
	
	/**
	 * Returns true if l is satisfies sufficient conditions to be a property assertion
	 * @param l
	 * @param ont
	 * @return
	 */
	public static boolean isPropertyAssertion(Literal l, JasdlOntology ont){	
		if(l.getArity()!=2) return false;
		return true;
	}
	
	public static boolean isObjectPropertyAssertion(Literal l, JasdlOntology ont) throws JasdlException{
		if(!isPropertyAssertion(l, ont)) return false;
		if(!( ont.toObject(ont.toAlias(l)) instanceof OWLObjectProperty )) return false;
		return true;
	}
	
	public static boolean isDataPropertyAssertion(Literal l, JasdlOntology ont) throws JasdlException{
		if(!isPropertyAssertion(l, ont)) return false;
		if(!( ont.toObject(ont.toAlias(l)) instanceof OWLDataProperty )) return false;
		return true;
	}
	
	
	/**
	 * Probably should be a part of jason's Trigger class?
	 * @param trigger
	 * @return
	 */
	public static Trigger.TEOperator getTEOp(Trigger trigger){
		if(trigger.isAddition()){
			return Trigger.TEOperator.add;
		}else{
			return Trigger.TEOperator.del;
		}
	}
	
	/**
	 * Creates a new literal identical except functor is replaced by new functor
	 * @param original		the original literal
	 * @param newFunctor	functor to replace the original functor with
	 * @return
	 */
	public static Literal mutateLiteral(Literal original, String newFunctor){
		Literal mutated = new Literal(newFunctor); // negation dealt with by ~ prefix
		mutated.addTerms(original.getTerms());
		mutated.addAnnots(original.getAnnots());
		return mutated;
	}
}
