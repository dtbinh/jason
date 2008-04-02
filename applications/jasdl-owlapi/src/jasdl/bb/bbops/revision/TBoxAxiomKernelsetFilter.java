package jasdl.bb.bbops.revision;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiom;

public class TBoxAxiomKernelsetFilter extends KernelsetFilter {

	public boolean retain(OWLAxiom axiom) {
		return (axiom instanceof OWLIndividualAxiom);
	}

}
