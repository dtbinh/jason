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
package jasdl.asSyntax;

import jasdl.asSemantics.JASDLAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.JASDLCommon;
import jasdl.util.exception.JASDLException;
import jasdl.util.exception.JASDLInvalidSELiteralException;
import jasdl.util.exception.JASDLNotEnrichedException;
import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLException;

/**
 * 
 * SE-Plans are ordered most specific -> most general, ensuring the most specific option is generalised to.
 * TODO: write tests
 * 
 * @author Tom Klapiscak
 *
 */
public class JASDLPlanLibrary extends PlanLibrary {

	private JASDLAgent agent;

	/**
	 * Keep track of SE Plans for generating candidate relevances for incoming triggers containing SE-Literals
	 */
	private LinkedList<SEPlan> sePlans;

	public JASDLPlanLibrary(JASDLAgent agent) {
		this.agent = agent;
		sePlans = new LinkedList<SEPlan>();
	}


	@Override
	public void add(Plan p) throws JasonException {
		// All plan who's trigger contains a SE-Literal is added as an SE-Plan so DL-unification can be performed when checking its relevance
		if (containsSELiteral(p.getTrigger().getLiteral())) {
			SEPlan sePlan = new SEPlan(agent, p);
			
			try {
				insertSEPlan(sePlan);
			} catch (OWLException e) {
				throw new JasonException("Error placing SE-Plan "+p, e);
			}
			
			super.add(sePlan);
		} else {
			super.add(p);
		}
	}
	
	/**
	 * Inserts an SE-Plan according to its specificity (most specific first).
	 * TODO: Is this not better placed within a JMCA option selection strategy?
	 */	
	private void insertSEPlan(SEPlan unplaced) throws JASDLException, OWLException{
		int i = 0;
		for(SEPlan placed : sePlans){			
			Literal x = unplaced.getTrigger().getLiteral();
			Literal y = placed.getTrigger().getLiteral();
			boolean xIsMoreSpecific = isMoreSpecific(x, y);
			getLogger().finest("Is "+x+" more specific than "+y+"? "+xIsMoreSpecific);
			if(xIsMoreSpecific) break;
			i++;
		}
		sePlans.add(i, unplaced);
	}
	
	
	public List<SEPlan> getSEPlans(){
		return sePlans;
	}
	
	/**
	 * Semantically naive literals are always considered more specific than semantically enriched.
	 * Do not need to consider incomparable literals (those with different arities or those with different semantic enrichment state)
	 * since one can never generalise to the other.
	 * 
	 * Algorithm functions as follows:
	 * 
	 * Terminal cases:
	 * if either x or y is not a structure, they are incomparable (different arities (at least one has no terms))
	 * If arity(x)!=arity(y) then they are considered incomparable and we (arbitrarily) return arity(x)<arity(y)
	 * If SN(x) and SE(y) then we return true
	 * if SE(x) and SN(y) then we return false
	 * if SE(x) and SE(y) then we return subsumes(y, x) (i.e. true if x is more specific)
	 * 
	 * Recursive case:
	 * score=0
	 * if SN(x) and SN(y) then
	 * 		for each term of x and y in parallel
	 * 			if isMoreSpecific(x, y) then score++ else score--
	 * 
	 * if score>=0 then we return true (i.e. x has a greater number of more specific terms than y) else false
	 *
	 */
	private boolean isMoreSpecific(Term _x, Term _y) throws JASDLException, OWLException{
		if(!_x.isStructure() || !_y.isStructure()) return false;		
		Structure x = (Structure)_x;
		Structure y = (Structure)_y;
		
		if(x.getArity() != y.getArity()) return false;
		SELiteral sx = null;
		SELiteral sy = null;
		
		try {
			if(x.isLiteral()) sx = agent.getSELiteralFactory().construct((Literal)x);
		} catch (JASDLInvalidSELiteralException e) {
		}
		
		try {
			if(y.isLiteral()) sy = agent.getSELiteralFactory().construct((Literal)y);
		} catch (JASDLInvalidSELiteralException e) {
		}
		
		if((sx == null && sy != null) || (sx != null && sy == null)) return false;
		
		if(sx != null && sy != null) return JASDLCommon.subsumes(agent.getJom(), sy.toOWLObject(), sx.toOWLObject());
		
		// both sx and sy are semantically-naive and have equivalent arities
		int score = 0;
		for(int i=0; i<x.getArity(); i++){
			Term xt = x.getTerm(i);
			Term yt = y.getTerm(i);
			
			if(isMoreSpecific(xt, yt)) score++; else score--;
			
		}
		
		if(score>=0) return true; else return false;
	}
	

	@Override
	public List<Plan> getCandidatePlans(Trigger te) {
		// All SE-plans treated as candidates. TODO: rule-out some based on arity?

		List<Plan> candidates = super.getCandidatePlans(te);
		if (!sePlans.isEmpty()) {
			if (candidates == null) {
				candidates = new Vector<Plan>();
			}
			candidates.addAll(sePlans);
			
			
		}
		return candidates;

	}

	private Logger getLogger() {
		return agent.getLogger();
	}

	/**
	 * Returns true if l or any of its nested terms are SE-Literals
	 * @param l
	 * @return
	 */
	private boolean containsSELiteral(Literal l) throws JASDLException {
		try {
			SELiteral sl = new SELiteral(l, agent.getJom());
			// Be tolerant of literals with "self" since this ontology has not yet been instantiated (and can't be till agent name has been AFTER init agent). 
			if (sl.getOntologyAnnotation().getTerm(0).equals(agent.getJom().getPersonalOntologyLabel())) {
				return true;
			}
			sl = agent.getSELiteralFactory().construct(l);
			return true;
		} catch (JASDLNotEnrichedException e) {
			if (l.getArity() > 0) {
				for (Term nested : l.getTerms()) {
					if (nested.isLiteral()) {
						if (containsSELiteral((Literal) nested)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
