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
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.JasdlException;
import jasdl.util.NotEnrichedException;
import jasdl.util.UnknownMappingException;
import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;

public class JasdlPlanLibrary extends PlanLibrary{
	
	private JasdlAgent agent;
	
	public JasdlPlanLibrary(JasdlAgent agent){
		this.agent = agent;
	}

	@Override
	public void add(Plan p) throws JasonException {
		try{
			agent.getSELiteralFactory().create(p.getTrigger().getLiteral());
			super.add(new SEPlan(agent, p));
		}catch(NotEnrichedException e){
			super.add(p);
		}catch(Exception e){
			throw new JasdlException("Exception caught while initialising JasdlPlanLibrary: "+e);
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
			} catch(NotEnrichedException e){
				// do nothing, we have a SN literal
			} catch(UnknownMappingException e){
				// do nothing, might be a reserved keyword
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
		SELiteral sl = agent.getSELiteralFactory().create(l);
		
		if(l.negated()){
			throw new JasdlException("JASDL cannot generalise strongly negated triggers");
		}
		
		Vector<Trigger> moreGeneral = new Vector<Trigger>();
		Vector<String> functors = new Vector<String>();

		List<OWLObject> os = generalise( agent,  sl.toOWLObject() );		
		for(OWLObject o : os){
			functors.add(agent.getAliasManager().getLeft(o).getFunctor().toString());
		}
			
		Collections.reverse(functors);
			
		for(String functor : functors){
			Literal imaginaryLiteral = mutateLiteral(l, functor);
			Trigger imaginaryTrigger = new Trigger(getTEOp(te), te.getType(), imaginaryLiteral);
			moreGeneral.add(imaginaryTrigger);				
		}
		return moreGeneral;
	}	
	
	
	/**
	 * Convenience wrapper method to retrieve all OWLObject ancestors of a (TBox) resource referred
	 * to by an alias.
	 * @param alias		alias of the resource to generalise
	 * @return			a list of resources more general than the TBox resource referred to by alias
	 * @throws JasdlException
	 */
	public static List<OWLObject> generalise(JasdlAgent agent, OWLObject o) throws JasdlException{
		List<OWLObject> os = new Vector<OWLObject>();
		if(o instanceof OWLClass){
			os.addAll(OWLReasonerAdapter.flattenSetOfSets(agent.getReasoner().getAncestorClasses((OWLClass)o)));
		}else if(o instanceof OWLObjectProperty){
			os.addAll(OWLReasonerAdapter.flattenSetOfSets(agent.getReasoner().getAncestorProperties((OWLObjectProperty)o)));
		}else if(o instanceof OWLDataProperty){
			os.addAll(OWLReasonerAdapter.flattenSetOfSets(agent.getReasoner().getAncestorProperties((OWLDataProperty)o)));
		}	
		return os;		
	}	
	
	/**
	 * Creates a new literal identical except functor is replaced by new functor
	 * @param original		the original literal
	 * @param newFunctor	functor to replace the original functor with
	 * @return
	 */
	private static Literal mutateLiteral(Literal original, String newFunctor){
		Literal mutated = new Literal(newFunctor); // negation dealt with by ~ prefix
		mutated.addTerms(original.getTerms());
		mutated.addAnnots(original.getAnnots());
		return mutated;
	}	

}
