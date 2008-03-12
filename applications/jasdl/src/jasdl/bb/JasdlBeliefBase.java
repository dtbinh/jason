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

import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.JasdlOntology;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.bb.DefaultBeliefBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLIndividualAxiom;

public class JasdlBeliefBase extends DefaultBeliefBase{
	
	private JasdlAgent agent;
	
	
	/**
	 * Agent reference can't be simply caught in an overrideen bb.init since it
	 * seems initial beliefs are added _before_ it is called! (Bug in Jason?)
	 * @param agent
	 */
	public void setAgent(JasdlAgent agent){
		this.agent = agent;
	}

	@Override
	public boolean add(Literal l) {
		try{			
			if(!l.isGround()){
				throw new JasdlException("Cannot add unground literal "+l);			
			}
			boolean result = true;
			List<JasdlOntology> onts = agent.getOntologies(l);
			for(JasdlOntology ont : onts){
				OWLIndividualAxiom axiom = ont.getAxiomFactory().create(l);
				getLogger().finest("Adding axiom: "+axiom);
				boolean thisResult = ont.addAxiom(axiom);
				if(thisResult){
					ont.storeAnnotations(l);
				}
				result &= thisResult;
			}			
			return result;
		}catch(InvalidSELiteralException e){
			//agent.getLogger().finest("Adding SN-literal "+l+". Reason: "+e);
			return super.add(l); // not a SELiteral, just add using Jason's standard mechanism
		}catch(JasdlException e){
			getLogger().warning("Exception caught while adding SELiteral "+l+". Reason: "+e);
			//e.printStackTrace();
			return false;
		}
	}

	@Override
	public Literal contains(Literal l) {		
		try {
			if(agent.isSELiteral(l)){
				Iterator<Literal> it = getRelevant(l);
				if(it.hasNext()){
					return it.next();
				}else{
					return null;
				}
			}else{
				return super.contains(l);
			}
		} catch(Exception e){
			getLogger().warning("Exception caught while checking if bb contains SELiteral "+l+". Reason: "+e);
			return null;
		}
	}

	@Override
	public Iterator<Literal> getRelevant(Literal l) {
		getLogger().finest("get relevant to "+l);
		List<Literal> relevant = new Vector<Literal>();
		try{						
			List<JasdlOntology> onts = agent.getOntologies(l);
			for(JasdlOntology ont : onts){
				List<OWLIndividualAxiom> axioms = ont.getAxiomFactory().get(l);			
				for(OWLIndividualAxiom axiom : axioms){
					Literal found = ont.getLiteralFactory().toLiteral(axiom);
					found.addAnnots(ont.retrieveAnnotations(found));
					relevant.add(found);
				}
			}
			getLogger().finest("...found: "+relevant);
		}catch(InvalidSELiteralException e){
			getLogger().finest("Removing SN-literal "+l+". Reason: "+e);
			return super.getRelevant(l); // not a SELiteral, just add using Jason's standard mechanism				
			
		}catch(Exception e){
			getLogger().warning("Exception caught while checking relevancies SELiteral "+l+". Reason: "+e);
			e.printStackTrace();
		}
		return relevant.iterator();
	}

	/**
	 * TODO: Currently only returns asserted ABox axioms. Include option to also show inferences?
	 */
	@Override
	public Iterator<Literal> iterator() {
		List<Literal> bels = new Vector<Literal>();
		
		// add all SN-Literals
		Iterator it = super.iterator();
		while(it.hasNext()){
			bels.add((Literal)it.next());
		}
	
		// add all SE-Literals (asserted)
		try{
			bels.addAll(agent.getABoxState());
		}catch(JasdlException e){
			getLogger().warning("Exception caught while retrieving ABox state: "+e);
		}		
		
		return bels.iterator();
	}

	@Override
	public boolean remove(Literal l) {
		try{			
			boolean result = true;
			List<JasdlOntology> onts = agent.getOntologies(l);
			for(JasdlOntology ont : onts){
				if(!l.isGround()){
					throw new JasdlException("Cannot remove unground literal "+l);
				}
				OWLIndividualAxiom axiom = ont.getAxiomFactory().create(l);
				result &=  ont.removeAxiom(axiom);
			}
			return result; // for ungrounded ontology annotations, must succeed for all operations
		}catch(InvalidSELiteralException e){
			getLogger().finest("Removing SN-literal "+l+". Reason: "+e);
			return super.add(l); // not a SELiteral, just add using Jason's standard mechanism
		}catch(Exception e){
			getLogger().warning("Exception caught while removing SELiteral "+l+". Reason: "+e);
			//e.printStackTrace();
			return false;
		}
	}
	
	public JasdlAgent getAgent(){
		return agent;
	}
	
	private Logger getLogger(){
		return agent.getLogger();
	}
	

}
