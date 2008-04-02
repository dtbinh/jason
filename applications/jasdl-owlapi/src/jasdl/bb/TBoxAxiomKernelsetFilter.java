package jasdl.bb;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiom;

import bebops.common.KernelsetFilter;

public class TBoxAxiomKernelsetFilter extends KernelsetFilter {

	public boolean retain(OWLAxiom axiom) {
		return (axiom instanceof OWLIndividualAxiom);
	}

}
