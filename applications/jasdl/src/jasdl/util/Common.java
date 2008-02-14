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

import jason.asSyntax.ListTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;

public class Common {
	public static String DELIM=",";
	
	public static String ONTOLOGY_ANNOTATION = "o";
	
	public static String EXPR_ANNOTATION = "expr";
	public static String DEFINED_BY_ANNOTATION = "defined_by";
	
	//public static String STANDARD_AUTOMAPS = "uncapitalise_individuals, uncapitalise_concepts"; // assumed auto-mapping sequence
	
	public static int DOMAIN = 0;
	public static int RANGE = 1;
	
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
	public static Structure getAnnot(Term term, String functor) throws JasdlException{
		List<Structure> terms = getAnnots(term, functor);
		if(terms.size() == 0){
			return null;
		}else if(terms.size() == 1){
			return terms.get(0);
		}else{
			throw new JasdlException("More than one "+functor+" present erroneously");
		}
	}
	
	public static boolean hasAnnot(Term term, String functor) throws JasdlException{
		return !getAnnots(term, functor).isEmpty();
	}
	
	public static String strip(String text){
		if(text == null){ return null; }
		return text = text.substring(1, text.length()-1);
	}
	
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
	
	public static String stripAll(String text, String remove){
		while(text.startsWith(remove) && text.length()>0){
			text = text.substring(1);
		}
		while(text.endsWith(remove) && text.length()>0){
			text = text.substring(0, text.length() - 1);
		}
		return text;
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
	
	public static XSDDatatype getDatatypePropertyXSDDatatype(DatatypeProperty prop){
		return (XSDDatatype)TypeMapper.getInstance().getTypeByName(prop.getRange().getURI());
	}
	
	public static String getDefinedBy(Pred p) throws JasdlException{
		Term _definedBy = getAnnot(p, DEFINED_BY_ANNOTATION);
		if(_definedBy == null){
			return null;
		}else{
			return ((Structure)_definedBy).getTerm(0).toString();
		}
	}		
	

}
