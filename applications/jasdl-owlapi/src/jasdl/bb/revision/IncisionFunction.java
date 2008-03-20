package jasdl.bb.revision;

import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

public interface IncisionFunction {
	
	
	
	/**
	 * Given a kernel set, establish a set of beliefs that undermine all kernels
	 * Abstract since application dependent (see parsia:brf)
	 * @param kernelset
	 * @return
	 */
	public Set<OWLAxiom> apply(Set<Set<OWLAxiom>> input);
}
