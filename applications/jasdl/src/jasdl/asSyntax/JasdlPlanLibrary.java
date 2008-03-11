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

import static jasdl.util.Common.getTEOp;
import static jasdl.util.Common.mutateLiteral;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.JasdlOntology;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLObject;

public class JasdlPlanLibrary extends PlanLibrary{
	
	private JasdlAgent agent;
	
	public JasdlPlanLibrary(JasdlAgent agent){
		this.agent = agent;
	}

	@Override
	public void add(Plan p) throws JasonException {
		if(agent.isSELiteral(p.getTrigger().getLiteral())){
			super.add(new SEPlan(agent, p));
		}else{
			super.add(p);
		}
	}

	/**
	 * Generates more general plans as candidate relevancies
	 * TODO: We are repeating work here: fetching more general triggers. Optimise by storing result for immedate use?
	 * Could store as part of plan, but wouldn't work correctly for run-time defined class
	 */
	public List<Plan> getAllRelevant(Trigger te){
		List<Plan> relevant = super.getAllRelevant(te);
		if(!te.getLiteral().negated()){			
			try {
				List<Plan> moreGeneral = getMoreGeneralPlans(te);
				if(!moreGeneral.isEmpty()){
					if(relevant == null){
						relevant = new Vector<Plan>();
					}
					relevant.addAll(moreGeneral);
				}		
			} catch(InvalidSELiteralException e){
				// do nothing, we have a SN literal
			} catch (JasdlException e) {
				agent.getLogger().warning("JASDL plan relevancy check failed. Reason: "+e);
			}
		}
		return relevant;
	}
	
	
	/**
	 * Return a list of plans that are relevant for this event in a more general sense (according to subsumption relationship)
	 * Note: hashMap is still used - no unification need be performed (yet)!
	 * Plans are ordered by increasing generality
	 * @param tes
	 * @return
	 */
	private List<Plan> getMoreGeneralPlans(Trigger te) throws JasdlException{
		List<Trigger> tes = getMoreGeneralTriggers(agent, te);
		List<Plan> moreGeneral = new Vector<Plan>();
		for(Trigger generalised : tes){
			List<Plan> relevant = super.getAllRelevant(generalised);
			if(relevant != null){
				moreGeneral.addAll(relevant);
			}
		}
		return moreGeneral;
	}	
	
	
	
	/**
	 * Triggers are ordered by increasing generality
	 * @param te
	 * @return
	 * @throws JasdlException
	 */
	public static List<Trigger> getMoreGeneralTriggers(JasdlAgent agent, Trigger te) throws JasdlException{
		Literal l = te.getLiteral();
		
		if(l.negated()){
			throw new JasdlException("JASDL cannot generalise strongly negated triggers");
		}
		
		Vector<Trigger> moreGeneral = new Vector<Trigger>();
		Vector<String> functors = new Vector<String>();
		List<JasdlOntology> onts = agent.getOntologies(l);
		
		for(JasdlOntology ont : onts){ // in case of ungrounded ontology annotation. TODO: Ground ontology annotation?
			List<OWLObject> os = ont.generalise( ont.toAlias(l) );		
			for(OWLObject o : os){
				functors.add(ont.toAlias(o).getName());
			}
		}
			
		Collections.reverse(functors);
			
		for(String functor : functors){
			Literal imaginaryLiteral = mutateLiteral(l, functor);
			Trigger imaginaryTrigger = new Trigger(getTEOp(te), te.getType(), imaginaryLiteral);
			moreGeneral.add(imaginaryTrigger);				
		}
		return moreGeneral;
	}	
	

}
