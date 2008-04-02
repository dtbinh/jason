package jasdl.test;

import static jasdl.asSemantics.JasdlConfigurator.MAS2J_ONTOLOGIES;
import static jasdl.asSemantics.JasdlConfigurator.MAS2J_PREFIX;
import static jasdl.asSemantics.JasdlConfigurator.MAS2J_URI;
import static jasdl.asSemantics.JasdlConfigurator.MAS2J_USEBELIEFREVISION;
import jasdl.architecture.JasdlAgArch;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.bridge.xsd.XSDDataType;
import jasdl.bridge.xsd.XSDDataTypeUtils;
import jason.architecture.AgArch;
import jason.architecture.AgArchInfraTier;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jason.infra.centralised.CentralisedAgArch;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;


/**
 * Tests that behaviour of JASDL's BB is identical to that of Jason's default BB.
 * 
 * Note that the ordering of JASDL's iterator function doesn't emulate Jason's exactly (Jason's is not strictly alphabetical and has a few odd quirks).
 * As a result, we compare the results of the iterator functions using Sets.
 * 
 * No thought it given to consistency of additions. This is dealt with by the brf before these methods are called.
 * A seperate test suite must be developed for belief bases employing the legacy rollback consistency assurance mechanism.
 * 
 * TODO: contruct special testing ontology to better cover all test cases
 * 
 * @author Tom Klapiscak
 *
 */
public class JasdlBeliefBaseTest extends TestCase{	
	
	public static String JASDL_AG_ARCH_CLASS = "jasdl.architecture.JasdlAgArch";
	public static String JASDL_AGENT_CLASS = "jasdl.asSemantics.JasdlAgent";
	public static String JASDL_BELIEF_BASE_CLASS = "jasdl.bb.JasdlBeliefBase";
	
	public static String TEST_ONTOLOGY_LABEL = "test";
	public static String TEST_ONTOLOGY_URI = "http://www.dur.ac.uk/t.g.klapiscak/onts/travel.owl";
	
	protected JasdlAgArch arch;
	protected JasdlAgent agent;	
	protected JasdlBeliefBase bb;
	protected DefaultBeliefBase testbb;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		// Use default manual mappings, mapping strategies and reasoner class. No known agents + trust ratings
		Settings stts = new Settings();
		stts.addOption(MAS2J_PREFIX + MAS2J_ONTOLOGIES, TEST_ONTOLOGY_LABEL);
		stts.addOption(MAS2J_PREFIX + "_" + TEST_ONTOLOGY_LABEL + MAS2J_URI, TEST_ONTOLOGY_URI);
		stts.addOption(MAS2J_PREFIX + MAS2J_USEBELIEFREVISION, "true");		// Must be enabled (we don't want legacy consistency assurance!) - remember brf is skipped by this
		String aslFile =  getClass().getResource("blank.asl").toExternalForm();		
		
		// load arch
		Class cls = Class.forName(JASDL_AG_ARCH_CLASS);
		Constructor ct = cls.getConstructor(new Class[] {});
		AgArch arch = (AgArch)ct.newInstance(new Object[] {});		
		AgArchInfraTier archInfraTier = new CentralisedAgArch();
		arch.setArchInfraTier(archInfraTier);		
		arch.initAg(JASDL_AGENT_CLASS, new ClassParameters(JASDL_BELIEF_BASE_CLASS), aslFile, stts);
		
		agent = (JasdlAgent)arch.getTS().getAg();
		bb = (JasdlBeliefBase)agent.getBB();
		
		testbb = new DefaultBeliefBase();
				
		// we are not worrying about ordering of iterator anymore
		Iterator<Literal> bbit = bb.iterator();
		while(bbit.hasNext()){
			testbb.add(bbit.next());
		}	
		
			
	}

	@After
	public void tearDown() throws Exception {
	}
	
	

	private Literal constructSELiteral(boolean sign, String functor, Term[] terms, Term[] annots, String label){
		Literal l = new Literal(sign, functor);	
		addO(l, label);
		l.addTerms(Arrays.asList(terms));
		if(annots!=null){
			l.addAnnots(Arrays.asList(annots));
		}
		return l;
	}
	
	private Literal constructSELiteral(boolean sign, String functor, Term[] terms, String[] _annots, String label){
		Term[] annots = null;
		if(_annots != null){
			annots = new Term[_annots.length];
			for(int i=0; i<_annots.length; i++){
				annots[i] = DefaultTerm.parse(_annots[i]);
			}		
		}
		return constructSELiteral(sign, functor, terms, annots, label);
	}	
	
	private Literal constructClass(boolean sign, String functor, String i, String[] annots, String label){
		return constructSELiteral(sign, functor, new Term[] {new Atom(i)}, annots, label);
	}
	
	private Literal constructObjectProperty(boolean sign, String functor, String s, String o, String[] annots, String label){
		return constructSELiteral(sign, functor, new Term[] {new Atom(s), new Atom(o)}, annots, label);
	}
	
	private Literal constructDataProperty(boolean sign, String functor, String s, String o, String[] annots, String label){
		return constructSELiteral(sign, functor, new Term[] {new Atom(s), DefaultTerm.parse(o)}, annots, label);
	}
	
		
	
	
	private Literal constructSELiteral(boolean sign, String functor, Term[] terms, String label){
		return constructSELiteral(sign, functor, terms, (Term[])null, label);
	}	
	
	private Literal constructClass(boolean sign, String functor, String i, String label){
		return constructSELiteral(sign, functor, new Term[] {new Atom(i)}, label);
	}
	
	private Literal constructObjectProperty(boolean sign, String functor, String s, String o, String label){
		return constructSELiteral(sign, functor, new Term[] {new Atom(s), new Atom(o)}, label);
	}
	
	private Literal constructDataProperty(boolean sign, String functor, String s, String o, String label){
		return constructSELiteral(sign, functor, new Term[] {new Atom(s), DefaultTerm.parse(o)}, label);
	}		
	
	/**
	 * Adds the ontology annotation with the specified label to the supplied literal
	 * @param l
	 */
	private void addO(Literal l, String label){
		Structure o = new Structure(SELiteral.ONTOLOGY_ANNOTATION_FUNCTOR);
		o.addTerm(new Atom(label));
		l.addAnnot(o);
	}	
			
	private static boolean[] signs = new boolean[] {true, false};
	
	
	@Test
	public void testRemoveLiteral() throws Exception{
		// contractor is tested more thoroughly elsewhere
		
		/** Testing annotation handling */
		//for(boolean sign : signs){
			boolean sign = true;
			Literal l = constructClass(sign, "hotel", "a", TEST_ONTOLOGY_LABEL);
			Literal l_x = constructClass(sign, "hotel", "a", new String[] {"x"}, TEST_ONTOLOGY_LABEL);
			Literal l_xy = constructClass(sign, "hotel", "a", new String[] {"x", "y"}, TEST_ONTOLOGY_LABEL);
			
			testRemoveIndividualAssertion(l);	
			testRemoveIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l_xy);
			
			testAddIndividualAssertion(l);
			testRemoveIndividualAssertion(l);			
			
						
			testAddIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l);
			testRemoveIndividualAssertion(l_x);			
			
			testAddIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l);
			
			
			testAddIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l_xy);
			testRemoveIndividualAssertion(l);
			
			testAddIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l);
			testRemoveIndividualAssertion(l_xy);				
			
			
			
		//}
		
		
		//l = constructClass();
		
	}
	
	
	//@Test
	public void notestAddLiteral() throws Exception{		
		String[][] annotSets = new String[][] {new String[]{}, new String[] {"x"}, new String[] {"x", "y"} };
		for(OWLOntology ontology : agent.getOntologyManager().getOntologies()){	
			for(int run=0; run<=1; run++){	// run twice to ensure duplicate additions are rejected
				for(boolean sign : signs){
					for(String[] annots : annotSets){
						for(OWLClass cls : ontology.getReferencedClasses()){
							Alias alias = agent.getAliasManager().getLeft(cls);
							String label = alias.getLabel().getFunctor();
							String functor = alias.getFunctor().getFunctor();					
							testAddIndividualAssertion(constructClass(sign, functor, "a", annots, label));
							testAddIndividualAssertion(constructClass(sign, functor, "b", annots, label));
						}
						for(OWLObjectProperty oprop : ontology.getReferencedObjectProperties()){
							Alias alias = agent.getAliasManager().getLeft(oprop);
							String label = alias.getLabel().getFunctor();
							String functor = alias.getFunctor().getFunctor();					
							testAddIndividualAssertion(constructObjectProperty(sign, functor, "a", "b", annots, label));
							testAddIndividualAssertion(constructObjectProperty(sign, functor, "b", "a", annots, label));
							testAddIndividualAssertion(constructObjectProperty(sign, functor, "a", "a", annots, label)); // irreflexive properties shouldn't be rejected at this stage
						}
						
						for(OWLDataProperty dprop : ontology.getReferencedDataProperties()){
							Alias alias = agent.getAliasManager().getLeft(dprop);
							String label = alias.getLabel().getFunctor();
							String functor = alias.getFunctor().getFunctor();
							XSDDataType typ = XSDDataTypeUtils.get(((OWLDataType)dprop.getRanges(ontology).toArray()[0]).toString());
							if(typ == XSDDataType.XSD_BOOLEAN){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "false", annots, label));
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "true", annots, label));
							}else if(typ == XSDDataType.XSD_DATE){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "\"2007-12-20\"", annots, label));
							}else if(typ == XSDDataType.XSD_DATETIME){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "\"2007-12-20T20:16:55\"", annots, label));						
							}else if(typ == XSDDataType.XSD_FLOAT){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "0.5", annots, label));	
							}else if(typ == XSDDataType.XSD_INT){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "22", annots, label));
							}else if(typ == XSDDataType.XSD_STRING){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "\"Winchester\"", annots, label));
							}else if(typ == XSDDataType.XSD_TIME){
								testAddIndividualAssertion(constructDataProperty(sign, functor, "a", "\"20:16:57\"", annots, label));
							}
						}
					}
					
				}					
			}
		}
		
		Set<Literal> expected = new HashSet<Literal>();
		Iterator<Literal> testbbit = testbb.iterator();
		
		while(testbbit.hasNext()){
			expected.add(testbbit.next());
		}
		
		Set<Literal> actual = new HashSet<Literal>();
		Iterator<Literal> bbit = bb.iterator();
		while(bbit.hasNext()){
			actual.add(bbit.next());
		}	
		assertEquals(expected, actual);
	}
	


	private void testAddIndividualAssertion(Literal l){				
		// special case: reject ~thing and ~nothing
		if(l.negated() && (l.getFunctor().equals("thing") || l.getFunctor().equals("nothing"))){
			assertFalse(bb.add(l));return;
		}
		//special case: reject negated property assertions
		if(l.negated() && l.getArity() == 2){
			assertFalse(bb.add(l));return;
		}
		System.out.print("Adding: "+l);
		//cloning necessary since Jason's default bb.add affects l passed to it
		boolean expected = testbb.add((Literal)l.clone());
		boolean actual = bb.add((Literal)l.clone());			
		assertEquals(expected, actual);
		System.out.println(" ... Result: "+actual);
	}
	

	private void testRemoveIndividualAssertion(Literal l){				
		// special case: reject ~thing and ~nothing
		if(l.negated() && (l.getFunctor().equals("thing") || l.getFunctor().equals("nothing"))){
			assertFalse(bb.remove(l));return;
		}
		//special case: reject negated property assertions
		if(l.negated() && l.getArity() == 2){
			assertFalse(bb.remove(l));return;
		}
		System.out.print("Removing: "+l);
		boolean expected = testbb.remove((Literal)l.clone());
		boolean actual = bb.remove((Literal)l.clone());		
		assertEquals(expected, actual);
		System.out.println(" ... Result: "+actual);		
	}	
	


	@Test
	public void testContainsLiteral() {
	}

	@Test
	public void testGetCandidateBeliefsLiteralUnifier() {
	}

	@Test
	public void testIterator() {
	}

}
