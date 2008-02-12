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
package jasdl.bb;

import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.ontology.StatementTriple;
import jasdl.util.JasdlException;
import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.bb.DefaultBeliefBase;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class OwlBeliefBase extends DefaultBeliefBase{
	
	/**
	 * Required for performing ontological manipulation and reasoning
	 */
	private OntologyManager manager;
	
	public void init(Agent agent, String[] args){
		super.init(agent, args);
	}
	
	public void setOntologyManager(OntologyManager manager){
		this.manager = manager;
	}	
	/**
	 * Assumption: l is ground - valid?
	 * 
	 * TODO: What about annotations?
	 * TODO: resolve inconsistencies using a trust rating if available
	 */
	public boolean add(Literal l){	
		manager.getLogger().finest("Attempting to add "+l);
		try {
			JasdlOntology ont = manager.getJasdlOntology(l);	
			if(ont == null){ // standard literal. proceed using Jason's standard belief base add mechanism
				return super.add(l);
			}else{ // semantically enriched literal, add to ABox
				StatementTriple triple = ont.constructTripleFromPred(l);
				Statement stmt = triple.toStatement(ont.getModel());
				ont.getModel().add(stmt);				
				if(!ont.isConsistent()){
					manager.getLogger().info("** ABox inconsistency detected on literal "+l+": "+stmt);
					ont.getModel().remove(stmt);
					return false;
				}				
				return true;
			}
		} catch (JasdlException e) {
			manager.getLogger().info("Error adding literal "+l+" to belief base. Reason: "+e);
			return false;
		}	
	}

	/**
	 * TODO: can only remove asserted information at the moment. Reasoner directly supports this only.
	 * TODO: I don't think removals can result in inconsistencies - but I need to check this assumption 
	 * TODO: bb.remove can currently only cope with ground literals
	 */
	@SuppressWarnings("unchecked")
	public boolean remove(Literal l){
		try{
			JasdlOntology ont = manager.getJasdlOntology(l);
			if(ont == null){
				return super.remove(l);
			}else{
				if(contains(l) == null){ // should never happen since Jason checks existence - here for certainty (and unit tests for now)
					return false;
				}
				StatementTriple triple = ont.constructTripleFromPred(l);
				Statement stmt = triple.toStatement(ont.getModel());				
				// This ensures that notification of removal failure 
				// if we try to remove information that has not been explicitly asserted
				if(!ont.getModel().getRawModel().contains(stmt)){ // TODO: contains must used getRelevant (so variables are grounded and inferred statements are found)
				//	return false;
				}				
				//ont.getModel().remove(stmt);
				Individual i = ont.getModel().getIndividual(stmt.getSubject().toString());
				manager.getLogger().finest("i: "+i);
				i.remove();
				return true;
			}
		} catch (JasdlException e) {
			manager.getLogger().info("Error removing "+l+" from BeliefBase. Reason: "+e);
			return false;
		}		
	}
	
	/**
	 * Question: bb.contains() currently simply uses getRelevant(). Is this OK? Could it be made more efficient by using less expensive reasoning mechanisms? Do we need to unify l?
	 * Answer: using model.contains. Not suitable however if we do in fact need to unify an unground l
	 * 
	 * TODO BB.contains is not strictly correct - it should ground an unground literal - might be wise to go back to using getRelevant! (remember, the behaviour of getRelevant is different for JASDL)
	 */
	public Literal contains(Literal l){
		manager.getLogger().finest("contains: "+l);
		try{
			JasdlOntology ont = manager.getJasdlOntology(l);
			if(ont == null){
				return super.contains(l);
			}else{
				Iterator<Literal> relIt = getRelevant(l);
				if(relIt.hasNext()){
					return relIt.next();
				}else{
					return null;
				}
				/*
				StatementTriple triple = ont.constructTripleFromPred(l);
				Statement stmt = triple.toStatement(ont.getModel());
				if(ont.getModel().contains(stmt)){
					return l;
				}else{
					return null;
				}
				*/
						
			}
		} catch (JasdlException e) {
			manager.getLogger().info("Error determining if "+l+" is contained in BeliefBase. Reason: "+e);
			return null;
		}
	}
	
	/**
	 * Returns all literals relevant to one supplied. Can now cope with unground ontology annotations.
	 * 
	 * If supplied literal is NOT semantically enchanced, literal is dealt with in the usual way (super.getAllRelevant is called).
	 * Otherwise:
	 * (0) Get a reference to the appropriate ontology as indicated in annotations (can be alias or physical ns) ({@link OntologyManager#getJasdlOntology(Pred)})
	 * (1) A (subject, predicate, object) triple statement is constructed from the literal ({@link JasdlOntology#constructTripleFromPred(Pred)})
	 * (2) All statements matching this are inferred from the ontology
	 * (3) For each inferred statement, a new literal is constructed corresponding to the assertion made by the statement ({@link JasdlOntology#constructLiteralFromStatement(Statement, boolean, boolean)})
	 * (4) All "inferred" literals are returned as relevant (justified by DL reasoning)
	 * 
	 * literals are "artificially" grounded here
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Literal> getRelevant(Literal l){
		manager.getLogger().finest("getRelevant: "+l);
		
		Vector<Literal> relevant = new Vector<Literal>();
		try {			
			List<JasdlOntology> onts = manager.listJasdlOntologies(l); // (0)
			if(onts.isEmpty()){ // standard literal, proceed using Jason's standard relevancy mechanism
				return super.getRelevant(l);
			}else{// semantically-enriched literal, use ontological reasoning
				for(JasdlOntology ont : onts){
				
					StatementTriple triple = ont.constructTripleFromPred(l); // (1)		
					manager.getLogger().finest("Relevancy check: ("+triple+")");
					//StmtIterator stmtIt = ont.getModel().listStatements(null, triple.getPredicate(), triple.getObject());
					StmtIterator stmtIt = ont.getModel().listStatements(triple.getSubject(), triple.getPredicate(), triple.getObject());
					while(stmtIt.hasNext()){ // (2)
						Statement foundStmt = (Statement) stmtIt.nextStatement();
						
						// construct a new literal for this statement
						Literal rl = null;						
						if(foundStmt.getPredicate().equals(RDF.type)){ // concept assertion - unary literal	
							rl = ont.constructLiteralFromResource( (OntClass)foundStmt.getObject().as(OntClass.class), l.negated());
							Literal indLit = ont.constructLiteralFromResource ( (Individual)foundStmt.getSubject().as(Individual.class), false); // individuals are never negated
							rl.addTerm(indLit);
						}else{ // object/datatype property assertion - binary literal
							
							// left individual is always subject
							Term l_term = ont.constructLiteralFromResource(((Individual)foundStmt.getSubject().as(Individual.class)), false); // left individual is always subject
							Term r_term = null;						
							
							// o and p swap round when listing unground object property statements - bug in Jena?
							OntProperty predicate;
							RDFNode object;			
							if(foundStmt.getObject().canAs(OntProperty.class)){ //if object is erroneously a property 
								 object = foundStmt.getPredicate();
								 predicate = (OntProperty)foundStmt.getObject().as(OntProperty.class);
							}else{ // as normal
								 predicate = (OntProperty)foundStmt.getPredicate().as(OntProperty.class);
								 object = foundStmt.getObject();
							}
							
							rl = ont.constructLiteralFromResource( predicate, l.negated());						
							
							if(predicate.canAs(ObjectProperty.class)){							
								r_term = ont.constructLiteralFromResource(((Individual)object.as(Individual.class)), false);								
							}else{								
								r_term = transposeDatatypeLiteral((com.hp.hpl.jena.rdf.model.Literal)object.as(com.hp.hpl.jena.rdf.model.Literal.class));
							}
							rl.addTerm(l_term);
							rl.addTerm(r_term);		
							
						}				
						
						// add back all (outside) annotations
						rl.addAnnots(l.getAnnots());					

						// copy across term annotations TODO: annotation copying correct behaviour in general? Also, need to recurse down terms for generality.						
						for(int i=0; i<rl.getArity(); i++){
							if(l.getTerm(i).isPred()){
								((Pred)rl.getTerm(i)).addAnnots(((Pred)l.getTerm(i)).getAnnots());
							}
						}
												
						relevant.add(rl);				
					}
				}
			}			
		} catch (JasdlException e) {
			manager.getLogger().info("Error retrieving relevancies for "+l+". Reason: "+e);			
		}
		manager.getLogger().finest("Found: "+relevant);
		return relevant.iterator(); // (4)
	}
	
	/**
	 * Transposes a RDF Datatype Literal to a Jason Term
	 * 
	 * @param literal
	 * @return	either a number term or string term
	 */
	private Term transposeDatatypeLiteral(com.hp.hpl.jena.rdf.model.Literal literal){
		RDFDatatype datatype = literal.getDatatype();
		String value = literal.getValue().toString();				
		
		// surround with quotes if necessary for datatype representation in Jason
		if(datatype == XSDDatatype.XSDstring || datatype == XSDDatatype.XSDdate || datatype == XSDDatatype.XSDdateTime || datatype == XSDDatatype.XSDtime){
			//discard -T at start if time (not sure why Jena puts this in) //TODO: is this in fact the proper way of representing a time in owl?			
			if(datatype == XSDDatatype.XSDtime){
				value = value.replace("-T", "");
			}
			return new StringTermImpl(value);
		}
		
		if(datatype == XSDDatatype.XSDboolean){
			if(Boolean.parseBoolean(value)){
				return Literal.LTrue;
			}else{
				return Literal.LFalse;
			}
		}
				
		return new NumberTermImpl(value);
	}

}
