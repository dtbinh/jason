package jasdl.bb.revision;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

public abstract class KernelsetFilter {
	
	
	public abstract boolean retain(OWLAxiom axiom);
	
	public Set<Set<OWLAxiom>> apply(Set<Set<OWLAxiom>> set){
		// filter to get only assertions about individuals - we cannot affect predefined TBoxes
		// TODO: how do run-time defined classes come into this?
		Set<Set<OWLAxiom>> filteredKernelset = new HashSet<Set<OWLAxiom>>();
		for(Set<OWLAxiom> akernel : set){
			Set<OWLAxiom> filteredKernel = new HashSet<OWLAxiom>();
			for(OWLAxiom belief : akernel){
				if(retain(belief)){
					filteredKernel.add(belief);
				}
			}
			filteredKernelset.add(filteredKernel);
		}
		return filteredKernelset;
	}	
}
