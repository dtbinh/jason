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
package jasdl.bridge;

import jasdl.JASDLParams;
import jasdl.asSemantics.JASDLAgent;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.JASDLCommon;
import jasdl.util.exception.JASDLNotEnrichedException;
import jason.asSemantics.Unifier;
import jason.asSemantics.VarsCluster;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import org.semanticweb.owl.model.OWLObject;

/**
 * Extends Jason's standard unifier to generalise unification of SE-Literals to include ontological rules. Specifically,
 * functors need not directly match, but be related by subsumption, the first argument can either directly match the second
 * or subsume it (this includes classes and properties).
 * 
 * TODO: ontology labels always considered unifiable, this might causes problems when taken out of context (i.e. outside an "o" annotation)
 * TODO: DL-Unifier is not commutative, does this violate a necessary condition of Jason's unification mechanism?
 * @author Tom Klapiscak
 *
 */
public class DLUnifier extends Unifier {

	private JASDLAgent agent;

	public DLUnifier(JASDLAgent agent) {
		super();
		this.agent = agent;
	}

	@Override
	protected boolean unifyTerms(Term t1g, Term t2g) {
		agent.getLogger().finest("Apply DL-unification to "+t1g+" and "+t2g);
		
		// if args are expressions, apply them and use their values
		if (t1g.isArithExpr()) {
			t1g = (Term) t1g.clone();
			t1g.apply(this);
		}
		if (t2g.isArithExpr()) {
			t2g = (Term) t2g.clone();
			t2g.apply(this);
		}

		final boolean t1gisvar = t1g.isVar();
		final boolean t2gisvar = t2g.isVar();

		// both are vars
		if (t1gisvar && t2gisvar) {
			VarTerm t1gv = (VarTerm) t1g;
			VarTerm t2gv = (VarTerm) t2g;

			// get their values
			Term t1vl = function.get(t1gv);
			Term t2vl = function.get(t2gv);

			// if the variable value is a var cluster, it means it has no value
			if (t1vl instanceof VarsCluster)
				t1vl = null;
			if (t2vl instanceof VarsCluster)
				t2vl = null;

			// both has value, their values should unify
			if (t1vl != null && t2vl != null) {
				return unifiesNoUndo(t1vl, t2vl);
			}
			// only t1 has value, t1's value should unify with var t2
			if (t1vl != null) {
				return unifiesNoUndo(t2gv, t1vl);
			}
			// only t2 has value, t2's value should unify with var t1
			if (t2vl != null) {
				return unifiesNoUndo(t1gv, t2vl);
			}

			// both are var (not unnamedvar) with no value, like X=Y
			// we must ensure that these vars will form a cluster
			if (!t1gv.isUnnamedVar() && !t2gv.isUnnamedVar()) {						
				VarTerm t1c = (VarTerm) t1gv.clone();
				VarTerm t2c = (VarTerm) t2gv.clone();
				VarsCluster cluster = new VarsCluster(t1c, t2c, this);
				if (cluster.hasValue()) {
					// all vars of the cluster should have the same value
					for (VarTerm vtc : cluster) {
						function.put(vtc, cluster);
					}
				}
			}
			return true;
		}

		// t1 is var that doesn't occur in t2
		if (t1gisvar) {
			VarTerm t1gv = (VarTerm) t1g;
			// if t1g is not free, must unify values
			Term t1vl = function.get(t1gv);
			if (t1vl != null && !(t1vl instanceof VarsCluster))
				return unifiesNoUndo(t1vl, t2g);
			else if (!t2g.hasVar(t1gv))
				return setVarValue(t1gv, t2g);
			else
				return false;
		}

		// t2 is var that doesn't occur in t1
		if (t2gisvar) {
			VarTerm t2gv = (VarTerm) t2g;
			// if t1g is not free, must unify values
			Term t2vl = function.get(t2gv);
			if (t2vl != null && !(t2vl instanceof VarsCluster))
				return unifiesNoUndo(t2vl, t1g);
			else if (!t1g.hasVar(t2gv))
				return setVarValue(t2gv, t1g);
			else
				return false;
		}

		// both terms are not vars

		// if any of the terms is not a structure (is a number or a
		// string), they must be equal
		if (!t1g.isStructure() || !t2g.isStructure())
			return t1g.equals(t2g);

		// both terms are structures

		Structure t1s = (Structure) t1g;
		Structure t2s = (Structure) t2g;

		// different arities
		final int ts = t1s.getArity();
		if (ts != t2s.getArity())
			return false;

		final boolean t1islit = t1g.isLiteral();
		final boolean t2islit = t2g.isLiteral();
		final boolean t1isneg = t1islit && ((Literal) t1g).negated();
		final boolean t2isneg = t2islit && ((Literal) t2g).negated();

		// if both are literal, they must have the same negated
		if (t1islit && t2islit && t1isneg != t2isneg)
			return false;

		// if one term is literal and the other not, the literal should not be negated
		if (t1islit && !t2islit && t1isneg)
			return false;
		if (t2islit && !t1islit && t2isneg)
			return false;

		// if the first term is a predicate and the second not, the first should not have annots 
		if (t1g.isPred() && !t2g.isPred() && ((Pred) t1g).hasAnnot())
			return false;

		// different functor

		/** DL-Unification considers all ontology labels unifiable **/
		if (t1s.isAtom() && t2s.isAtom()) {
			try {
				// TODO: Might cause problems when taking out of context (i.e. owl(3) and self(3) will wrongfully unify)
				if (agent.getLabelManager().isKnownLeft((Atom) t1s) && agent.getLabelManager().isKnownLeft((Atom) t2s)) {
					return true;
				}
			} catch (ClassCastException e) {
				//TODO: possible bug, why is this happening?
			}
		}

		/** DL-Unification considers subsumed functors of SE-Literals unifiable */
		try {
			
			SELiteral sl1 = null;
			SELiteral sl2 = null;

			if (t1islit) {
				/**
				 * TODO: Fix this NASTY HACK
				 * Jason adds all annotations of selected event to plan clone trigger of newly instantiated means.
				 * This is not normally a problem in Jason. However, since valid SE-Literals may only have a single "o"
				 * annotation, this causes a rejection here. This fix simply deletes the last (i.e. added "later") "o"
				 * annotation on the left-most literal (since this is where the trigger of the intended means plan clone will
				 * be).
				 */
				ListTerm os = ((Literal)t1s).getAnnots(JASDLParams.ONTOLOGY_ANNOTATION_FUNCTOR);
				if(os.size() == 2){
					((Literal)t1s).delAnnot(os.get(1));
				}
				
				try {
					sl1 = agent.getSELiteralFactory().construct((Literal) t1s);
					//agent.getAliasManager().getRight(x);
				} catch (JASDLNotEnrichedException e) {
				}
			}

			if (t2islit) {
				try {
					sl2 = agent.getSELiteralFactory().construct((Literal) t2s);
				} catch (JASDLNotEnrichedException e) {
				}
			}

			agent.getLogger().finest("Attempting to unify " + t1s + " and " + t2s);

			if (sl1 == null && sl2 == null) {
				// Both are not SE-Literals, perform normal functor unification check
				if (t1s.getFunctor() != null && !t1s.getFunctor().equals(t2s.getFunctor())) {
					agent.getLogger().finest("... failed because functors do not directly match");
					return false;
				}
			} else if ((sl1 != null && sl2 == null) || (sl1 == null && sl2 != null)) {
				// One is SE-Literal and one is not, can't unify
				agent.getLogger().finest("... failed because one is a SE-literal and the other isn't");
				return false;
			} else {
				// Both are SE-Literals, apply generalised unification		    
				agent.getLogger().finest("Both SE-Literals: " + sl1 + " and " + sl2);
				OWLObject o1 = sl1.toOWLObject();
				OWLObject o2 = sl2.toOWLObject();
				if (!JASDLCommon.subsumes(agent.getJom(), o1, o2)) {
					agent.getLogger().finest("... failed because first doesn't subsume second");
					return false;
				}
			}
			agent.getLogger().finest("... success!");
		} catch (Exception e) {
			agent.getLogger().warning("Error encountered while attempting to unify " + t1g + " and " + t2g + ". Reason: ");
			e.printStackTrace();
			return false;
		}

		// unify inner terms
		// do not use iterator! (see ListTermImpl class)
		for (int i = 0; i < ts; i++)
			if (!unifiesNoUndo(t1s.getTerm(i), t2s.getTerm(i)))
				return false;

		// if both are predicates, the first's annots must be subset of the second's annots
		if (t1g.isPred() && t2g.isPred())
			if (!((Pred) t1g).hasSubsetAnnot((Pred) t2g, this))
				return false;

		return true;
	}



}