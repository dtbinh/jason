package jasdl.asSemantics;

import jasdl.asSyntax.JasdlPlanLibrary;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.ToAxiomConverter;
import jasdl.bridge.ToSELiteralConverter;
import jasdl.bridge.alias.AliasManager;
import jasdl.bridge.label.LabelManager;
import jasdl.bridge.label.PhysicalURIManager;
import jasdl.bridge.seliteral.SELiteralFactory;
import jasdl.util.JasdlException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Atom;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.net.URI;
import java.util.Set;

import jmca.asSemantics.JmcaAgent;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owl.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class JasdlAgent extends JmcaAgent {	
	private OWLOntologyManager ontologyManager;
	private Reasoner reasoner;
	private AliasManager aliasManager;
	private LabelManager labelManager;
	private SELiteralFactory seLiteralFactory;
	private ToSELiteralConverter toSELiteralConverter;	
	private ToAxiomConverter toAxiomConverter;
	private PhysicalURIManager physicalURIManager;
	private ManchesterOWLSyntaxOWLObjectRendererImpl manchesterObjectRenderer;
	
	public JasdlAgent(){
		super();
		// instantiate managers
		aliasManager = new AliasManager();
		labelManager = new LabelManager();
		ontologyManager = OWLManager.createOWLOntologyManager();
		physicalURIManager = new PhysicalURIManager();
		seLiteralFactory = new SELiteralFactory(this);
		toAxiomConverter = new ToAxiomConverter(this);
		toSELiteralConverter = new ToSELiteralConverter(this);
		
		manchesterObjectRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		
		
		// instantiate (Pellet) reasoner
		PelletOptions.USE_TRACING = true;
		reasoner = new Reasoner(ontologyManager);
		reasoner.getKB().setDoExplanation( true );
		
		// override plan library
		setPL( new JasdlPlanLibrary(this) );
	}
	
	
	
	@Override
	public TransitionSystem initAg(AgArch arch, BeliefBase bb, String src, Settings stts) throws JasonException {
		if(!(bb instanceof JasdlBeliefBase)){
			throw new JasdlException("JASDL must be used in combination with the jasdl.bb.OwlBeliefBase class");
		}
		
		((JasdlBeliefBase)bb).setAgent(this);	
		
		
		// load .mas2j JASDL configuration
		JasdlConfigurator config = new JasdlConfigurator(this);
		config.configure(stts);
		return super.initAg(arch, bb, src, stts);
	}



	/**
	 * Convenience method to instantiate an ontology, load it into reasoner and map it to a label
	 * @param uri
	 * @throws JasdlException
	 */
	public void loadOntology(Atom label, URI uri) throws JasdlException{
		try{
			OWLOntology ontology = ontologyManager.loadOntologyFromPhysicalURI(uri);
			Set<OWLOntology> imports = ontologyManager.getImportsClosure(ontology);
			reasoner.loadOntologies(imports);
			reasoner.classify();
			labelManager.put(label, ontology);
			physicalURIManager.put(ontology, uri);
		}catch(OWLOntologyCreationException e){
			throw new JasdlException("Error loading ontology "+uri+". Reason: "+e);
		}		
	}



	public AliasManager getAliasManager() {
		return aliasManager;
	}



	public LabelManager getLabelManager() {
		return labelManager;
	}



	public OWLOntologyManager getOntologyManager() {
		return ontologyManager;
	}
	
	



	public PhysicalURIManager getPhysicalURIManager() {
		return physicalURIManager;
	}



	public Reasoner getReasoner() {
		return reasoner;
	}
	
	
	
	
	
	
	public SELiteralFactory getSELiteralFactory() {
		return seLiteralFactory;
	}
	
	



	public ToAxiomConverter getToAxiomConverter() {
		return toAxiomConverter;
	}
	
	



	public ToSELiteralConverter getToSELiteralConverter() {
		return toSELiteralConverter;
	}



	public ManchesterOWLSyntaxOWLObjectRendererImpl getManchesterObjectRenderer() {
		return manchesterObjectRenderer;
	}
	
	
	




}
