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
import jasdl.util.JasdlException;
import jasdl.util.NotABoxAssertionException;
import jason.asSemantics.Agent;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.bb.DefaultBeliefBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import aterm.ATermAppl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
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
				Statement stmt = ont.toStatement(l);
				ont.getModel().add(stmt);
				
				manager.getLogger().finest("Adding "+stmt);
				if(!ont.isConsistent()){
					
					ont.getModel().remove(stmt); // remove for now, we might add this back if existing justification is less authoritative
					
					
					manager.getLogger().info("** ABox inconsistency detected on literal "+l+": "+stmt);					
					Set<ATermAppl> exp = ont.getPellet().getKB().getExplanationSet();
					
					
					for(ATermAppl at : exp){
						try{
							Literal expL = ont.fromATerm(at);
							manager.getLogger().info("Justifying assertion: "+expL);
						}catch(NotABoxAssertionException e){}// not a abox assertion
					}					
					return false; // only return false if we end up rejecting l
				}
				
				// store annotations
				ont.storeAnnotations(l, l.getAnnots());
				
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
	 * TODO: bb.remove can currently only cope with ground literals - NO? contains should be used to ground them
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
				Statement s = ont.toStatement(l);				
				// This ensures that notification of removal failure 
				// if we try to remove information that has not been explicitly asserted
				if(!ont.getModel().getRawModel().contains(s)){
					return false;
				}				
				//ont.getModel().remove(stmt);
				Individual i = ont.getModel().getIndividual(s.getSubject().toString());
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
	 * Ignores annotations
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
		Vector<Literal> relevant = new Vector<Literal>();
		try {			
			List<JasdlOntology> onts = manager.listJasdlOntologies(l); // (0)
			if(onts.isEmpty()){ // standard literal, proceed using Jason's standard relevancy mechanism
				return super.getRelevant(l);
			}else{// semantically-enriched literal, use ontological reasoning
				for(JasdlOntology ont : onts){
					Selector sel = ont.toSelector(l);
					// below allows left-unground support, but beware of bug in Jena: p & o swap round in resulting statements.
					// this suggests we shouldn't be making this type of query, so is disabled for now
					//StmtIterator it = ont.getModel().listStatements(sel.getSubject(), sel.getPredicate(), sel.getObject());
					StmtIterator it = ont.getModel().listStatements(sel);					
					
					while(it.hasNext()){
						Statement s = it.nextStatement();						
						Literal rl = ont.fromStatement(s);					
						relevant.add(rl);
					}
				}
			}			
		} catch (JasdlException e) {
			manager.getLogger().info("Error retrieving relevancies for "+l+". Reason: "+e);			
		}
		return relevant.iterator(); // (4)
	}

	
	/**
	 * Currently outputs all inferred information, not just asserted
	 */
	public Iterator<Literal> iterator(){
		List<Literal> ls = new Vector<Literal>();
		
		// get all semantically-enriched literals
		// this includes all ABox inferences from all loaded ontologies
		try {
			for(JasdlOntology ont : manager.getLoadedOntologies()){
				
				ExtendedIterator cIt = ont.getModel().listNamedClasses();
				while(cIt.hasNext()){
					OntClass c = (OntClass)cIt.next();
					StmtIterator it = ont.getModel().listStatements(new SimpleSelector(null, RDF.type, c));					
					while(it.hasNext()){
						Statement s = it.nextStatement();
						if(ont.getModel().isInBaseModel(s) || manager.getAgent().getShowInferred()){
							ls.add(ont.fromStatement(s));
						}
					}					
				}
				
				ExtendedIterator pIt = ont.getModel().listOntProperties();
				while(pIt.hasNext()){
					OntProperty p = (OntProperty)pIt.next();
					StmtIterator it = ont.getModel().listStatements(new SimpleSelector(null, p, (RDFNode)null));					
					while(it.hasNext()){
						Statement s = it.nextStatement();
						if(ont.getModel().isInBaseModel(s) || manager.getAgent().getShowInferred()){
							ls.add(ont.fromStatement(s));
						}									
					}					
				}
				
			}
		} catch (JasdlException e) {
			manager.getLogger().warning("Error retrieving ABox state. Reason: "+e);
		}
		
		// get all semantically-naive literals
		Iterator<Literal> it = super.iterator();
		while(it.hasNext()){
			ls.add(it.next());
		}
		
		return ls.iterator();		
	}

}
