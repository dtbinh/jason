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
package jasdl.bridge.seliteral;

import jasdl.JASDLParams;
import jasdl.bridge.JASDLOntologyManager;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.util.exception.JASDLDuplicateMappingException;
import jasdl.util.exception.JASDLException;
import jasdl.util.exception.JASDLInvalidSELiteralException;
import jasdl.util.exception.JASDLNotEnrichedException;
import jasdl.util.exception.JASDLUnknownMappingException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;

import uk.ac.manchester.cs.owl.OWLLabelAnnotationImpl;

/**
 * Wraps around a Jason literal to provide ontology-related functionality. Follows the "Decorator" design pattern.
 * There is a specialisation of this class for each type of SELiteral JASDL supports, each adding functionality appropriate to its type.
 * 
 * TODO: This class could be made much more efficient by storing values to avoid recalculation
 * @author Tom Klapiscak
 *
 */
public class SELiteral {

	protected Structure ontologyAnnotation = null;

	protected OWLOntology ontology = null;

	protected OWLIndividualAxiom axiom = null;

	protected Set<OWLIndividualAxiom> axioms = null;

	protected JASDLOntologyManager jom;

	protected Literal literal;

	/**
	 * Note: does not check validity of supplied literal. 
	 * @param literal
	 * @param jasdlOntologyManager
	 */
	public SELiteral(Literal literal, JASDLOntologyManager jasdlOntologyManager) {
		this.literal = (Literal) literal.clone(); // TODO: for some reason, without cloning the literal we have trouble with annotations on incoming propsotional content?!
		this.jom = jasdlOntologyManager;
	}

	public Structure getOntologyAnnotation() throws JASDLInvalidSELiteralException {
		if (ontologyAnnotation == null || !JASDLParams.USE_SELITERAL_CACHING) {
			ListTerm os = literal.getAnnots(JASDLParams.ONTOLOGY_ANNOTATION_FUNCTOR);
			if (os.size() == 0) {
				throw new JASDLNotEnrichedException("Not semantically-enriched");
			}
			if (os.size() > 1) {
				throw new JASDLInvalidSELiteralException("Multiple ontology annotations present");
			}
			Term t = os.get(0);
			if (!(t instanceof Structure)) {
				throw new JASDLInvalidSELiteralException("Invalid ontology annotation term");
			}
			ontologyAnnotation = (Structure) t;

			if (ontologyAnnotation.getArity() != 1) {
				throw new JASDLInvalidSELiteralException("Invalid ontology annotation arity");
			}
		}
		return ontologyAnnotation;
	}

	public OWLOntology getOntology() throws JASDLInvalidSELiteralException, JASDLUnknownMappingException {
		if (ontology == null || !JASDLParams.USE_SELITERAL_CACHING) {
			Structure o = getOntologyAnnotation();
			if (o.getTerm(0).isStructure()) { // Checking for atomicity directly does not seem to work
				ontology = jom.getLabelManager().getRight((Atom) o.getTerm(0));
			} else {
				throw new JASDLInvalidSELiteralException("Invalid ontology annotation format on " + o);
			}
		}
		return ontology;
	}

	public Atom getOntologyLabel() throws JASDLInvalidSELiteralException, JASDLUnknownMappingException {
		return jom.getLabelManager().getLeft(getOntology());
	}

	/**
	 * Convenience method, calls AliasFactory
	 * @return	the alias associated with this SELiteral
	 */
	public Alias toAlias() throws JASDLInvalidSELiteralException, JASDLUnknownMappingException {
		return AliasFactory.INSTANCE.create(this);
	}

	/**
	 * Convenience method, calls AliasManager to retrieve the ontological object referred to by the alias representing
	 * this SELiteral. Special case is made for unmapped strongly-negated class assertions. Such SELiterals are identified by 
	 * the "~" prefix to their functor. In this case the "unegated" ontological object is obtained, complemented and mapped
	 * to the negated alias for future use.
	 * @return	 the ontological object referred to by the alias representing this SELiteral
	 * @throws JASDLUnknownMappingException
	 */
	public OWLObject toOWLObject() throws JASDLInvalidSELiteralException, JASDLUnknownMappingException, JASDLDuplicateMappingException {
		try {
			return jom.getAliasManager().getRight(this.toAlias());
		} catch (JASDLUnknownMappingException e) {
			Alias alias = toAlias();
			if (alias.getFunctor().toString().startsWith("~")) {
				if (literal.getArity() == 2) {
					throw new JASDLInvalidSELiteralException("JASDL does not currently support negated property assertions");
				}
				Atom unnegatedFunctor = new Atom(alias.getFunctor().toString().substring(1));
				Alias unnegatedAlias = AliasFactory.INSTANCE.create(unnegatedFunctor, alias.getLabel());
				if (unnegatedAlias.equals(JASDLParams.OWL_THING) || unnegatedAlias.equals(JASDLParams.OWL_NOTHING)) {
					throw new JASDLInvalidSELiteralException("owl:thing and owl:nothing should not be negated");
				}
				OWLClass unnegated = (OWLClass) jom.getAliasManager().getRight(unnegatedAlias);
				OWLDescription negated = jom.getOWLDataFactory().getOWLObjectComplementOf(unnegated);
				jom.getAliasManager().put(alias, negated);

				return negated;
			} else {
				throw e;
			}
		}
	}

	/**
	 * Placed here for convenient (varying) usage by subclasses
	 * Validates since terms are mutable
	 * @return
	 * @throws JASDLUnknownMappingException
	 */
	public OWLIndividual getOWLIndividual(int i) throws JASDLException {
		return getOWLIndividual(literal.getTerm(i));
	}

	/**
	 * Attempts to fetch individual from "parent" ontologies (i.e. that referred to by ontology label of enclosing SELiteral).
	 * If not present, attempts to fetch from personal ontology.
	 * If not present, instantiates in personal ontology and adds a meaningless all_different({i}) assertion to ensure the individual is referenced (for parsing - see toEntity method).
	 * Placed here for convenient (varying) usage by subclasses.
	 * Validates and doesn't cache since terms are mutable.
	 * @return
	 * @throws JASDLUnknownMappingException
	 */
	public OWLIndividual getOWLIndividual(Term term) throws JASDLInvalidSELiteralException, JASDLDuplicateMappingException, JASDLUnknownMappingException {
		Atom atom = new Atom(term.toString());
		OWLIndividual i;
		try {
			i = (OWLIndividual) jom.getAliasManager().getRight(AliasFactory.INSTANCE.create(atom, jom.getLabelManager().getLeft(getOntology())));
		} catch (JASDLUnknownMappingException e1) {
			Alias alias = AliasFactory.INSTANCE.create(atom, jom.getPersonalOntologyLabel()); // <- look in personal ontology
			try {
				i = (OWLIndividual) jom.getAliasManager().getRight(alias);
			} catch (JASDLUnknownMappingException e2) {
				// Instantiate and map the individual if not known
				OWLOntology ontology = getOntology();
				// Clashes (with different types of resource) don't matter thanks to OWL1.1's punning features
				//TODO: what about clashes with individuals (different alias, same uri)
				URI uri = URI.create(ontology.getURI() + "#" + atom);
				i = jom.getOntologyManager().getOWLDataFactory().getOWLIndividual(uri);
				jom.getAliasManager().put(alias, i);
				
				//OWLAxiom axiom = jom.getOWLDataFactory().getOWLDifferentIndividualsAxiom(Collections.singleton(i));
				
				OWLAnnotation a = new OWLLabelAnnotationImpl(jom.getOWLDataFactory(), jom.getOWLDataFactory().getOWLTypedConstant(""));
				OWLAxiom axiom = jom.getOWLDataFactory().getOWLEntityAnnotationAxiom(i, a);
				try {
					jom.getOntologyManager().applyChange(new AddAxiom(ontology, axiom)); // <- add this "redundant" axiom to ensure we have a reference to this individuals in the ontology (for toEntity method and description parsing)
				} catch (OWLOntologyChangeException e) {
					throw new JASDLInvalidSELiteralException("Error instantiating owl individual from " + term, e);
				}
				

			}
		} catch (ClassCastException e) {
			throw new JASDLInvalidSELiteralException(atom + " does not refer to an individual");
		}
		return i;
	}

	public Literal getLiteral() {
		return literal;
	}

	public SELiteralClassAssertion asClassAssertion() {
		return new SELiteralClassAssertion(literal, jom);
	}

	public SELiteralObjectPropertyAssertion asObjectPropertyAssertion() {
		return new SELiteralObjectPropertyAssertion(literal, jom);
	}

	public SELiteralDataPropertyAssertion asDataPropertyAssertion() {
		return new SELiteralDataPropertyAssertion(literal, jom);
	}

	public SELiteralAllDifferentAssertion asAllDifferentAssertion() {
		return new SELiteralAllDifferentAssertion(literal, jom);
	}

	/**
	 * Returns all non-JASDL annotations to this literal
	 * @return
	 * @throws JASDLException
	 */
	public ListTerm getSemanticallyNaiveAnnotations() throws JASDLException {
		ListTerm annotsClone = (ListTerm) literal.getAnnots().clone(); // clone so as not to affect original literal
		annotsClone.remove(getOntologyAnnotation());
		return annotsClone;
		//TODO: drop anon and named? uneccessary I think since they are isolated to architecture level.
	}

	// *** Mutators ***

	/**
	 * Sets the ontology annotation of this SELiteral to be the fully-qualified physical namespace of the associated ontology
	 */
	public void qualifyOntologyAnnotation() throws JASDLException {
		getOntologyAnnotation().setTerm(0, new StringTermImpl(jom.getPhysicalURIManager().getRight((getOntology())).toString()));
	}

	/**
	 * Clones the literal associated with this SELiteral, replacing its functor with the suppleid
	 * @param newFunctor	functor to replace the original functor with
	 * @return
	 */
	public void mutateFunctor(String newFunctor) throws JASDLException {
		Literal mutated = new Literal(!literal.negated(), newFunctor); // negation dealt with by ~ prefix
		mutated.addTerms(literal.getTerms());
		mutated.addAnnots(literal.getAnnots());
		literal = mutated;
	}

	//	*************	

	public String toString() {
		return literal.toString();
	}

}
