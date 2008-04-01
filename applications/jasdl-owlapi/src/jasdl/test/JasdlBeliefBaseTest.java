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
import java.util.Vector;

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
		
		/*
		// ensure initial state of testbb contained predefinitions from bb's ABox
		// Add in reverse order, so iterator ordering is identical
		List<Literal> state = new Vector<Literal>();
		Iterator<Literal> bbit = bb.iterator();
		while(bbit.hasNext()){
			state.add(bbit.next());
		}
		Collections.reverse(state);
		for(Literal l : state){
			testbb.add(l);
		}
		*/
		
		
		// we are not worrying about ordering of iterator anymore
		Iterator<Literal> bbit = bb.iterator();
		while(bbit.hasNext()){
			testbb.add(bbit.next());
		}		
		
		
		
		
			
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddLiteral() throws Exception{
		
		boolean[] signs = new boolean[] {true, false};
		
		for(OWLOntology ontology : agent.getOntologyManager().getOntologies()){	
			for(int run=0; run<=1; run++){	// run twice to ensure duplicate additions are rejected
				for(boolean sign : signs){
					for(OWLClass cls : ontology.getReferencedClasses()){
						Alias alias = agent.getAliasManager().getLeft(cls);
						String label = alias.getLabel().getFunctor();
						String functor = alias.getFunctor().getFunctor();					
						assertAddClass(sign, functor, "a", label);
						assertAddClass(sign, functor, "b", label);
					}
					for(OWLObjectProperty oprop : ontology.getReferencedObjectProperties()){
						Alias alias = agent.getAliasManager().getLeft(oprop);
						String label = alias.getLabel().getFunctor();
						String functor = alias.getFunctor().getFunctor();					
						assertAddObjectProperty(sign, functor, "a", "b", label);
						assertAddObjectProperty(sign, functor, "b", "a", label);
						assertAddObjectProperty(sign, functor, "a", "a", label); // irreflexive properties shouldn't be rejected at this stage
					}
					
					for(OWLDataProperty dprop : ontology.getReferencedDataProperties()){
						Alias alias = agent.getAliasManager().getLeft(dprop);
						String label = alias.getLabel().getFunctor();
						String functor = alias.getFunctor().getFunctor();
						XSDDataType typ = XSDDataTypeUtils.get(((OWLDataType)dprop.getRanges(ontology).toArray()[0]).toString());
						if(typ == XSDDataType.XSD_BOOLEAN){
							assertAddDataProperty(sign, functor, "a", "false", label);
							assertAddDataProperty(sign, functor, "a", "true", label);
						}else if(typ == XSDDataType.XSD_DATE){
							assertAddDataProperty(sign, functor, "a", "\"2007-12-20\"", label);
						}else if(typ == XSDDataType.XSD_DATETIME){
							assertAddDataProperty(sign, functor, "a", "\"2007-12-20T20:16:55\"", label);
						}else if(typ == XSDDataType.XSD_DOUBLE){
							assertAddDataProperty(sign, functor, "a", "22.0", label);							
						}else if(typ == XSDDataType.XSD_FLOAT){
							assertAddDataProperty(sign, functor, "a", "0.5", label);	
						}else if(typ == XSDDataType.XSD_INT){
							assertAddDataProperty(sign, functor, "a", "22", label);
						}else if(typ == XSDDataType.XSD_STRING){
							assertAddDataProperty(sign, functor, "a", "\"Winchester\"", label);
						}else if(typ == XSDDataType.XSD_TIME){
							assertAddDataProperty(sign, functor, "a", "\"20:16:57\"", label);
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
		
		Set<Literal> actuallyLessDifference = new HashSet<Literal>();
		actuallyLessDifference.addAll(expected);
		actuallyLessDifference.removeAll(actual);
		if(!actuallyLessDifference.isEmpty()){
			System.out.println("\n\nexpected > actual. Difference: "+actuallyLessDifference+"\n\n");
		}
		
		Set<Literal> actuallyMoreDifference = new HashSet<Literal>();
		actuallyMoreDifference.addAll(actual);
		actuallyMoreDifference.removeAll(expected);
		if(!actuallyMoreDifference.isEmpty()){
			System.out.println("\n\nexpected < actual. Difference: "+actuallyMoreDifference+"\n\n");
		}
		
		//System.out.println("Terminal state: "+actual);
		
		assertEquals(expected, actual);
		//System.out.println("Terminal state consistent? "+);
		
		

	}
	
	
	/**
	 * Constructs functor and annotations (including ontology annotation) of a SE-Literal.
	 * @param sign
	 * @param functor
	 * @param annots
	 * @return
	 */
	private Literal constructSELiteral(boolean sign, String functor, String label){
		Literal l = new Literal(sign, functor);	
		addO(l, label);
		return l;
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
		
	/**
	 * Constructs SE-Literal, adds terms
	 * recognises special cases (i.e. negated properties)
	 * otherwise, ensures behaviour is identical to Jason's BB.
	 * Additionally, tries with "x", then "y" annots added.
	 * @param sign
	 * @param functor
	 * @param annots
	 * @param is
	 * @param expected
	 */
	private void assertAdd(boolean sign, String functor, List<Term> is, String label){
		Term[][] annotSets = new Term[][] {new Term[]{}, new Term[] {new Atom("x")}, new Term[] {new Atom("x"), new Atom("y")} };
		for(Term[] annots : annotSets){		
			Literal l = constructSELiteral(sign, functor, label);
			l.addTerms(is);
			l.addAnnots(Arrays.asList(annots));
			// special case: reject ~thing and ~nothing
			if(!sign && (functor.equals("thing") || functor.equals("nothing"))){
				assertFalse(bb.add(l));return;
			}
			//special case: reject negated property assertions
			if(!sign && l.getArity() == 2){
				assertFalse(bb.add(l));return;
			}			
			//System.out.println("Adding: "+l);		
			assertEquals(testbb.add((Literal)l.clone()), bb.add((Literal)l.clone())); // cloning necessary since Jason's default bb.add affects l passed to it
		}
		
		// TODO: error in Jason's BB below? Rejects additions even though annots changed!
		// might be because deffault  bb.add affects literals?
		//l.addAnnot(new Atom("x"));
		//System.out.println("Adding: "+l);
		//assertEquals(testbb.add(l), bb.add(l));
		//l.addAnnot(new Atom("y"));
		//System.out.println("Adding: "+l);
		//assertEquals(testbb.add(l), bb.add(l));
	}
	
	
	/**
	 * Convenience method for testing class assertion additions (unary literals)
	 * 
	 * @param sign
	 * @param functor
	 * @param annots
	 * @param i
	 * @param expected
	 */
	private void assertAddClass(boolean sign, String functor, String i, String label){	
		assertAdd(sign, functor, Collections.singletonList((Term)new Atom(i)), label);
	}
	
	/**
	 * Convenience method for testing class assertion additions (unary literals)
	 * 
	 * @param sign
	 * @param functor
	 * @param annots
	 * @param i
	 * @param expected
	 */
	private void assertAddDataProperty(boolean sign, String functor, String s, String o, String label){
		assertAdd(sign, functor, Arrays.asList(new Term[] {new Atom(s), DefaultTerm.parse(o)}), label);
	}	
	
	/**
	 * Convenience method for testing class assertion additions (unary literals)
	 * 
	 * @param sign
	 * @param functor
	 * @param annots
	 * @param i
	 * @param expected
	 */
	private void assertAddObjectProperty(boolean sign, String functor, String s, String o, String label){
		assertAdd(sign, functor, Arrays.asList(new Term[] {new Atom(s), new Atom(o)}), label);
	}	


	@Test
	public void testRemoveLiteral() {
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
