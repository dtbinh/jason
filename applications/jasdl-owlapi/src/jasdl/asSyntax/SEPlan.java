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

import jasdl.JASDLParams;
import jasdl.asSemantics.JASDLAgent;
import jasdl.bridge.DLUnifier;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

/**
 * <p>A plan whose trigger is associated with a semantically-enriched literal.</p>
 * <p>Jason's definition of relevancy for this type of plan is overloaded to additionally include unification with subsuming triggers</p>
 * 
 * @author Tom Klapiscak
 *
 */
public class SEPlan extends Plan{
	private static final long serialVersionUID = 1L;

	private JASDLAgent agent;

	public SEPlan(JASDLAgent agent, Plan p) {
		super(p.getLabel(), p.getTrigger(), p.getContext(), p.getBody());
		this.agent = agent;
	}

	@Override
	public Unifier isRelevant(Trigger te) {
		Unifier un = super.isRelevant(te);
		if (un != null) { // plan is specifically relevant to deal with the trigger
			return un;
		}
		DLUnifier dlun = new DLUnifier(agent);
		if (dlun.unifiesNoUndo(getTrigger(), te)) { // <- plan's trigger subsumes incoming

			Literal causeWithAnnots = (Literal)te.getLiteral().clone();		
			dlun.unifiesNoUndo(new VarTerm(JASDLParams.JASDL_TG_CAUSE_RETAIN_ANNOTS), causeWithAnnots);
			
			Literal causeNoAnnots = (Literal)te.getLiteral().clone();
			Term o = causeNoAnnots.getAnnots(JASDLParams.ONTOLOGY_ANNOTATION_FUNCTOR).get(0);			
			causeNoAnnots.clearAnnots();
			causeNoAnnots.addAnnot(o);			
			dlun.unifiesNoUndo(new VarTerm(JASDLParams.JASDL_TG_CAUSE), causeNoAnnots);
			
			
			return dlun;
		} else {
			return null;
		}
	}

	@Override
	public Object clone() {
		return new SEPlan(agent, (Plan) super.clone());
	}

}
