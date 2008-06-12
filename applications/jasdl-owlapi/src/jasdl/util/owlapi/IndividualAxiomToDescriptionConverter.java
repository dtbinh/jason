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
package jasdl.util.owlapi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLRuntimeException;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;

/**
 * Converts an axiom asserting something about an individual into a description that expresses the same information.
 * i.e. axiom entailed <-> description satisfiable.
 * Works only for class, object property, data property and all_different assertions (since these are the only types of assertions that can be made using SE-Literals).
 * Attempting to visit any other type of axiom will throw a runtime exception.
 * @author Tom Klapiscak
 *
 */
public class IndividualAxiomToDescriptionConverter implements OWLAxiomVisitor {

	private OWLDataFactory factory;

	private OWLDescription desc;

	public IndividualAxiomToDescriptionConverter(OWLDataFactory factory) {
		this.factory = factory;
	}

	public OWLDescription convert(OWLAxiom axiom) {
		axiom.accept(this);
		return getDescription();
	}

	public OWLDescription getDescription() {
		return desc;
	}

	public void visit(OWLClassAssertionAxiom axiom) {
		desc = factory.getOWLObjectIntersectionOf(new OWLDescription[] { axiom.getDescription(), factory.getOWLObjectOneOf(Collections.singleton(axiom.getIndividual())) });
	}

	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		desc = factory.getOWLObjectIntersectionOf(new OWLDescription[] { factory.getOWLObjectValueRestriction(axiom.getProperty(), axiom.getObject()), factory.getOWLObjectOneOf(Collections.singleton(axiom.getSubject())) });
	}

	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		desc = factory.getOWLObjectIntersectionOf(new OWLDescription[] { factory.getOWLDataValueRestriction(axiom.getProperty(), axiom.getObject()), factory.getOWLObjectOneOf(Collections.singleton(axiom.getSubject())) });
	}

	public void visit(OWLDifferentIndividualsAxiom axiom) {
		Set<OWLDescription> nominals = new HashSet<OWLDescription>();
		for (OWLIndividual individual : axiom.getIndividuals()) {
			nominals.add(factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(individual)));
		}
		desc = factory.getOWLObjectIntersectionOf(nominals);
	}

	public void visit(OWLSubClassAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDisjointClassesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDataPropertyDomainAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLImportsDeclaration axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLAxiomAnnotationAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLObjectSubPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDisjointUnionAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDeclarationAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLEntityAnnotationAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLOntologyAnnotationAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDataPropertyRangeAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLEquivalentClassesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLDataSubPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLSameIndividualsAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		throw new OWLRuntimeException("Unimplemented");
	}

	public void visit(SWRLRule rule) {
		throw new OWLRuntimeException("Unimplemented");
	}

}
