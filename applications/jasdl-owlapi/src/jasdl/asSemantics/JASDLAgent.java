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

package jasdl.asSemantics;

import jasdl.JASDLParams;
import jasdl.asSyntax.JASDLPlanLibrary;
import jasdl.asSyntax.SEPlan;
import jasdl.bb.JASDLBeliefBase;
import jasdl.bb.bebops.JASDLIncisionFunction;
import jasdl.bb.bebops.JASDLKernelsetFilter;
import jasdl.bb.bebops.JASDLReasonerFactory;
import jasdl.bridge.JASDLOntologyManager;
import jasdl.bridge.factory.AxiomToSELiteralConverter;
import jasdl.bridge.factory.SELiteralFactory;
import jasdl.bridge.factory.SELiteralToAxiomConverter;
import jasdl.bridge.mapping.aliasing.AliasManager;
import jasdl.bridge.mapping.aliasing.MappingStrategy;
import jasdl.bridge.mapping.label.LabelManager;
import jasdl.bridge.mapping.label.OntologyURIManager;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.exception.JASDLException;
import jasdl.util.exception.JASDLNotEnrichedException;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import jmca.asSemantics.JmcaAgent;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

import bebops.semirevision.BeliefBaseSemiRevisor;
import bebops.util.exception.BebopsRevisionFailedException;

import com.clarkparsia.explanation.SatisfiabilityConverter;

/**
 * JASDL's extension to JMCA agent (and hence the default Jason agent).
 * Provides:
 * <ul>
 * <li>JASDL specific agent initialisation {@link JASDLAgentConfigurator}</li>
 * <li>Two implementations of DL-based belief-revision (experimental and optional see {@link JASDLParams#USE_BEBOPS_BELIEF_REVISION)} and {@link JASDLParams#DEFAULT_USEBELIEFREVISION}}</li>
 * <li>Access to the various utility classes used throughout JASDL</li>
 * <
 * 
 * TODO: Ensure "o" annotation is added to SE-Percepts removed by buf
 * @author Tom Klapiscak
 *
 */
public class JASDLAgent extends JmcaAgent {

	private JASDLOntologyManager jom;




	private JASDLAgentConfigurator config;

	public JASDLAgent() throws Exception {
		super();

		// instantiate utility classes
		jom = new JASDLOntologyManager(getLogger());
		config = new JASDLAgentConfigurator(this);


		// override plan library
		setPL(new JASDLPlanLibrary(this));
		
		

	}

	@Override
	public TransitionSystem initAg(AgArch arch, BeliefBase bb, String src, Settings stts) throws JasonException {
		if (!(bb instanceof JASDLBeliefBase)) {
			throw new JASDLException("JASDL must be used in combination with the jasdl.bb.OwlBeliefBase class");
		}
		((JASDLBeliefBase) bb).setAgent(this);

		// load .mas2j JASDL configuration and initialise agent
		config.preInitConfigure(stts);
		TransitionSystem ts = super.initAg(arch, bb, src, stts);
		config.postInitConfigure(stts);
		

		for(SEPlan se : ((JASDLPlanLibrary)getPL()).getSEPlans()){
			getLogger().finest(se.getTrigger().toString());
		}

		return ts;
	}
	
	@Override
	public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {
		// TODO: what annotations should revision contractions contain? all! (or none? - same effect)

		// No! the same. -a[x] only undermines assertions leading to a[x]!
		// if we are performing belief-revision all annotations will be gathered (shortcut - use none?) ensuring axiom will be obliterated
		// annotations never solely lead to conflicts.

		if (!config.isBeliefRevisionEnabled()) { // if experimental feature is disabled
			return super.brf(beliefToAdd, beliefToDel, i);
		}

		List<Literal> addList = new Vector<Literal>();
		List<Literal> removeList = new Vector<Literal>();

		// NO LONGER APPLICABLE: For efficiency reasons: no need to check beliefs to be contracted if established by BB revisor (since they are implicitly grounded)

		// just accept beliefToDel. Assumption: Deletions never lead to inconsistencies?!
		if (beliefToDel != null) {
			getLogger().finest("Revise: -" + beliefToDel);
			removeList.add(beliefToDel);
		} else if (beliefToAdd != null) {
			if (config.isBeliefRevisionEnabled()) {
				try {
					getLogger().fine("Revise: +" + beliefToAdd);
					SELiteral sl = getSELiteralFactory().construct(beliefToAdd);
					if (JASDLParams.USE_BEBOPS_BELIEF_REVISION) {
						applyBebopsBeliefRevision(sl);
					} else {
						applyPelletOnlyBeliefRevision(sl);
					}
					// *** this point is only reached if the addition is accepted ***
					addList.add(beliefToAdd);
				} catch (BebopsRevisionFailedException e) {
					throw new RevisionFailedException("", e);
				} catch (RevisionFailedException e) {
					getLogger().info("brf: rejected " + beliefToAdd);
					throw e; // propagate upwards
				} catch (JASDLNotEnrichedException e) {
					addList.add(beliefToAdd);// can't perform DL-based belief revision on SN-Literals
				} catch (Exception e) {
					throw new RuntimeException("Error encountered during belief revision ", e);
				}

			} else {
				addList.add(beliefToAdd); //brf disabled, just accept belief (legacy consistency assurance will be applied later)
			}
		} else {
			throw new RuntimeException("Unexpected behaviour, both beliefToAdd and beliefToDel are non-null...?");
		}
		List<Literal>[] toReturn = null;

		for (Literal removed : removeList) {
			// we need to ground unground removals
			Unifier u = null;
			try {
				u = i.peek().getUnif(); // get from current intention
			} catch (Exception e) {
				u = new Unifier();
			}
			if (believes(removed, u)) {
				removed.apply(u);
				if (getBB().remove(removed)) {
					if (toReturn == null) {
						toReturn = new List[2];
						toReturn[0] = new Vector<Literal>();
						toReturn[1] = new Vector<Literal>();
					}
					toReturn[1].add(removed);
				}
			}
		}

		// affect BB
		for (Literal added : addList) {
			if (getBB().add(added)) {
				if (toReturn == null) {
					toReturn = new List[2];
					toReturn[0] = new Vector<Literal>();
					toReturn[1] = new Vector<Literal>();
				}
				toReturn[0].add(added);
			}
		}

		return toReturn;
	}

	private void applyPelletOnlyBeliefRevision(SELiteral sl) throws Exception {
		OWLAxiom axiomToAdd = getSELiteralToAxiomConverter().create(sl);
		Reasoner pellet = (org.mindswap.pellet.owlapi.Reasoner) getReasoner();
		getOntologyManager().applyChange(new AddAxiom(sl.getOntology(), axiomToAdd));
		getJom().refreshReasoner();

		while (!pellet.isConsistent()) {
			getJom().refreshReasoner();
			SatisfiabilityConverter con = new SatisfiabilityConverter(getOWLDataFactory());
			OWLDescription desc = con.convert(axiomToAdd);
			pellet.getKB().setDoExplanation(true);
			pellet.isSatisfiable(desc);
			Set<OWLAxiom> explanation = pellet.getExplanation();

			explanation = new JASDLKernelsetFilter().filterSingleKernel(explanation);
			getLogger().fine("Incremental explanation: " + explanation);

			explanation = new JASDLIncisionFunction(this, sl).applyToOne(explanation);
			getLogger().fine("Remove: " + explanation);

			if (explanation.equals(Collections.singleton(axiomToAdd))) {
				// Axiom has been rejected,
				getOntologyManager().applyChange(new RemoveAxiom(sl.getOntology(), axiomToAdd));
				getJom().refreshReasoner();
				throw new RevisionFailedException("" + sl);
			} else {
				for (OWLAxiom revision : explanation) {
					for (OWLOntology ontology : getOntologyManager().getOntologies()) { // across all known ontologies
						getOntologyManager().applyChange(new RemoveAxiom(ontology, revision));
					}
				}
				getJom().refreshReasoner();
			}
		}
		/** Will only be reached if addition has been accepted */
		// remove the axiom added only for the purpose of explanation generation
		getOntologyManager().applyChange(new RemoveAxiom(sl.getOntology(), axiomToAdd));
		getJom().refreshReasoner();
	}

	private void applyBebopsBeliefRevision(SELiteral sl) throws Exception {
		OWLAxiom axiomToAdd = getSELiteralToAxiomConverter().create(sl);
		BeliefBaseSemiRevisor bbrev = new BeliefBaseSemiRevisor(getOntologyManager(), new JASDLReasonerFactory(), getLogger());
		Set<OWLAxiom> revisions = bbrev.revise(axiomToAdd, new JASDLKernelsetFilter(), new JASDLIncisionFunction(this, sl));
		// *** this point is only reached if the addition is accepted ***
		for (OWLAxiom revision : revisions) {
			for (OWLOntology ontology : getOntologyManager().getOntologies()) { // across all known ontologies
				getOntologyManager().applyChange(new RemoveAxiom(ontology, revision));
			}
		}
		getJom().refreshReasoner();
	}
	

	/**
	 * *Must* be unique within society!
	 * @return
	 */
	public String getAgentName() {
		return getTS().getUserAgArch().getAgName();
	}

	public SELiteralFactory getSELiteralFactory(){
		return getJom().getSELiteralFactory();
	}
	
	public List<MappingStrategy> getDefaultMappingStrategies() {
		return config.getDefaultMappingStrategies();
	}

	public JASDLOntologyManager getJom() {
		return jom;
	}

	public AliasManager getAliasManager() {
		return getJom().getAliasManager();
	}

	public LabelManager getLabelManager() {
		return getJom().getLabelManager();
	}

	public OWLOntologyManager getOntologyManager() {
		return getJom().getOntologyManager();
	}

	public OntologyURIManager getPhysicalURIManager() {
		return getJom().getPhysicalURIManager();
	}

	public OntologyURIManager getLogicalURIManager() {
		return getJom().getLogicalURIManager();
	}

	public OWLReasoner getReasoner() throws JASDLException {
		return getJom().getReasoner();
	}

	public SELiteralToAxiomConverter getSELiteralToAxiomConverter() {
		return getJom().getSELiteralToAxiomConverter();
	}

	public AxiomToSELiteralConverter getAxiomToSELiteralConverter() {
		return getJom().getAxiomToSELiteralConverter();
	}

	public OWLDataFactory getOWLDataFactory() {
		return getOntologyManager().getOWLDataFactory();
	}

	public void setReasoner(OWLReasoner reasoner) {
		getJom().setReasoner(reasoner);
	}

	public JASDLAgentConfigurator getConfig() {
		return config;
	}

}