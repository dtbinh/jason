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

import jasdl.asSemantics.JasdlAgent;
import jasdl.util.JasdlException;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Trigger;

import java.util.List;

/**
 * <p>A plan whose trigger is associated with a semantically-enriched literal.</p>
 * <p>Jason's definition of relevancy for this type of plan is overloaded to additionally include unification with subsuming triggers</p>
 * 
 * @author Tom Klapiscak
 *
 */
public class SEPlan extends Plan {
	private static final long serialVersionUID = 1L;
	private JasdlAgent agent;
	
	public SEPlan(JasdlAgent agent, Plan p){
		super(p.getLabel(), p.getTrigger(), p.getContext(), p.getBody());
		this.agent = agent;
	}
	
	
	
	@Override
	public Unifier isRelevant(Trigger te) {
		Unifier un = super.isRelevant(te);
		if(un != null){ // plan is specifically relevant to deal with the trigger
			return un;
		}					
		try {
			// add te of result_of(te) as an annotation?
			List<Trigger> moreGeneralTriggers = JasdlPlanLibrary.getMoreGeneralTriggers(agent, te);
			for(Trigger moreGeneralTrigger : moreGeneralTriggers){
				un = super.isRelevant(moreGeneralTrigger);
				if(un != null){ // plan is generally relevant to deal with trigger
					return un; // found a unification for this candidate, no need to continue
				}
			}
		} catch (JasdlException e) {
			agent.getLogger().warning("Relevancy check failed for plan: "+this+"\n against trigger: "+te);
		}
		return null;
	}
	
	@Override
	public Object clone(){
		return new SEPlan(agent, (Plan)super.clone());
	}
}
