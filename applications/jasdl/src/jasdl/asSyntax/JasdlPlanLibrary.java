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
import jasdl.ontology.Alias;
import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.JasonException;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntResource;

public class JasdlPlanLibrary extends PlanLibrary{
	
	@SuppressWarnings("unused")
	private OntologyManager manager;
	
	public JasdlPlanLibrary(OntologyManager manager){
		super();
		this.manager = manager;
	}
	
	/**
	 * Intercepts plan addition
	 * If plan trigger is associated with a SE-Literal, the plan's isRelevant method is overridden
	 * to check relevancy against all generalisation of the supplied trigger
	 */
	public void add(Plan p) throws JasonException {		
		JasdlOntology ont = manager.getJasdlOntology(p.getTrigger().getLiteral());
		Plan toAdd = p;
		if(ont != null){ // does the plan have a SE-Literal trigger?
			toAdd = new SEPlan(manager, p);		
		}
		super.add(toAdd);
	}
	
	/**
	 * Generates more general plans as candidate relevancies
	 * TODO: We are repeating work here: fetching more general triggers. Optimise by storing result for immedate use?
	 * Could store as part of plan, but wouldn't work correctly for run-time defined class
	 */
	public List<Plan> getAllRelevant(Trigger te){
		manager.getLogger().fine("Get relevant to: "+te);		
		List<Plan> relevant = super.getAllRelevant(te);		
		try {
			List<Plan> moreGeneral = getMoreGeneralPlans(te);
			if(!moreGeneral.isEmpty()){
				if(relevant == null){
					relevant = new Vector<Plan>();
				}
				relevant.addAll(moreGeneral);
			}			
		} catch (JasdlException e) {
			manager.getLogger().warning("JASDL plan relevancy check failed. Reason: "+e);
		}	
		
		return relevant;
	}
	
	/**
	 * Return a list of plans that are relevant for this event in a more general sense (according to subsumption relationship)
	 * Note: hashMap is still used - no unification need be performed!
	 * Plans are ordered by increasing generality
	 * @param tes
	 * @return
	 */
	private List<Plan> getMoreGeneralPlans(Trigger te) throws JasdlException{
		List<Trigger> tes = getMoreGeneralTriggers(manager, te);
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
	public static List<Trigger> getMoreGeneralTriggers(OntologyManager manager, Trigger te) throws JasdlException{
//		 generate a list of subsuming aliases
		Literal l = te.getLiteral();
		Vector<Trigger> moreGeneral = new Vector<Trigger>();
		Vector<String> functors = new Vector<String>();
		JasdlOntology ont = manager.getJasdlOntology(l);
		if(ont != null){ // if we have a SE-literal
			OntResource res = ont.getModel().getOntResource(ont.getReal(l).toString());
			List parents = ont.listOntResourceParents(res, false);
			for(Object _parent : parents){
				OntResource parent = (OntResource)_parent;
				String parentURI = parent.getURI();
				if(parentURI != null){ // in case of resources with no URI associated with it (anonymous?)
					Alias alias = ont.toAlias(URI.create(parentURI));
					functors.add(alias.getName());
				}
			}
			
			Collections.reverse(functors);
			
			for(String functor : functors){
				Literal imaginaryLiteral = Literal.parseLiteral(l.toString().replaceFirst(l.getFunctor(), functor));
				Trigger imaginaryTrigger = new Trigger(getTEOp(te), te.getType(), imaginaryLiteral);
				moreGeneral.add(imaginaryTrigger);				
			}
		}
		return moreGeneral;
	}
	/**
	 * Filters at least some non-relevancies due to non-matching annotations - only checks annotation functor and arities though
	 */
	
	public boolean isRelevant(Trigger te){
		// perform usual Jason relevancy check (functor/arity only)
		 List<Plan> plans = getAllRelevant(te);		 	 
		 if(plans==null || plans.isEmpty()){
			 return false;
		 }
		 // plans found, check that there is at least one plan whose annotations are a subset of trigger's
		 // custom subset check implemented to only check that functor and arity of annotation literals match (types of inner terms are ignored)
		 // TODO: What about nested terms with annotations? Ignore?
		 ListTerm tAnnots = te.getLiteral().getAnnots();
		 for(Plan plan : plans){
			 ListTerm pAnnots = plan.getTrigger().getLiteral().getAnnots();
			 if (pAnnots == null) return true;
			 if (tAnnots != null){		 
				 // check trigger has all annotations the plan does
				 // only check structures
				 boolean pRelevant = true;
				 for (Term _pAnnot : pAnnots){
					 if(_pAnnot.isStructure()){
						 Structure pAnnot = (Structure)_pAnnot;
						 boolean hasAnnot = false;
						 for(Term _tAnnot : tAnnots){
							 if(_tAnnot.isStructure()){
								 Structure tAnnot = (Structure)_tAnnot;
								 if(tAnnot.getFunctor().equals(pAnnot.getFunctor()) && tAnnot.getArity() == pAnnot.getArity()){
									 hasAnnot = true;
									 break;
								 }
							 }
						 }
						 if(!hasAnnot){
							 pRelevant = false;
							 break;
						 }
					 }
				 }
				 if(pRelevant){
					 return true;
				 }
			 }
		 }
		// no plan with subset of trigger annotations found, not relevant
		return false;
	}
	
	
	
}
