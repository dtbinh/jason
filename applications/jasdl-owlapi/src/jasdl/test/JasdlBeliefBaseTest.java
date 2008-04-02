package jasdl.test;

import static jasdl.asSemantics.JasdlConfigurator.MAS2J_ONTOLOGIES;
import static jasdl.asSemantics.JasdlConfigurator.MAS2J_PREFIX;
import static jasdl.asSemantics.JasdlConfigurator.MAS2J_URI;
import static jasdl.asSemantics.JasdlConfigurator.MAS2J_USEBELIEFREVISION;
import jasdl.architecture.JasdlAgArch;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.seliteral.SELiteral;
import jasdl.util.exception.JasdlException;
import jasdl.util.owlapi.xsd.XSDDataType;
import jasdl.util.owlapi.xsd.XSDDataTypeUtils;
import jason.architecture.AgArch;
import jason.architecture.AgArchInfraTier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.bb.DefaultBeliefBase;
import jason.infra.centralised.CentralisedAgArch;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
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
 * TODO: Perhaps we shouldn't be testing literal transation functionality here (i.e. in add)?
 * 
 * @author Tom Klapiscak
 *
 */
public class JasdlBeliefBaseTest extends TestCase{	
	
	public static String JASDL_AG_ARCH_CLASS = "jasdl.architecture.JasdlAgArch";
	public static String JASDL_AGENT_CLASS = "jasdl.asSemantics.JasdlAgent";
	public static String JASDL_BELIEF_BASE_CLASS = "jasdl.bb.JasdlBeliefBase";
	
	public static Atom TEST_ONTOLOGY_LABEL = new Atom("test");
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
		stts.addOption(MAS2J_PREFIX + MAS2J_ONTOLOGIES, TEST_ONTOLOGY_LABEL.getFunctor());
		stts.addOption(MAS2J_PREFIX + "_" + TEST_ONTOLOGY_LABEL + MAS2J_URI, TEST_ONTOLOGY_URI);
		stts.addOption(MAS2J_PREFIX + MAS2J_USEBELIEFREVISION, "true");		// Must be enabled (we don't want legacy consistency assurance!) - remember brf is skipped by this
		
		// load arch
		Class cls = Class.forName(JASDL_AG_ARCH_CLASS);
		Constructor ct = cls.getConstructor(new Class[] {});
		AgArch arch = (AgArch)ct.newInstance(new Object[] {});		
		AgArchInfraTier archInfraTier = new CentralisedAgArch();
		arch.setArchInfraTier(archInfraTier);		
		arch.initAg(JASDL_AGENT_CLASS, new ClassParameters(JASDL_BELIEF_BASE_CLASS), null, stts);
		
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
	
	


	
	private void testAddIndividualAssertion(boolean sign, Atom functor, Term[] terms, Term[] annots, Atom label) throws JasdlException{
		try{
			testAddIndividualAssertion(agent.getSELiteralFactory().construct(sign, functor, terms, annots, label));			
		}catch(JasdlException e){}
	}
	
	private void testAddIndividualAssertion(SELiteral sl) throws JasdlException{
		Literal l = sl.getLiteral();
		System.out.print("Adding: "+l);
		//cloning necessary since Jason's default bb.add affects l passed to it
		boolean expected = testbb.add((Literal)l.clone());
		boolean actual = bb.add((Literal)l.clone());			
		assertEquals(expected, actual);
		System.out.println(" ... Result: "+actual);
	}	
	

	private void testRemoveIndividualAssertion(boolean sign, Atom functor, Term[] terms, Term[] annots, Atom label) throws JasdlException{
		try{
			testRemoveIndividualAssertion(agent.getSELiteralFactory().construct(sign, functor, terms, annots, label));			
		}catch(JasdlException e){}
	}
	
	private void testRemoveIndividualAssertion(SELiteral sl) throws JasdlException{
		Literal l = sl.getLiteral();
		System.out.print("Adding: "+l);
		//cloning necessary since Jason's default bb.add affects l passed to it
		boolean expected = testbb.remove((Literal)l.clone());
		boolean actual = bb.remove((Literal)l.clone());			
		assertEquals(expected, actual);
		System.out.println(" ... Result: "+actual);
	}	
			
	private static boolean[] signs = new boolean[] {true, false};
	

	
	
	/**
	 * Number of entities (classes and object and datatype properties) to test BB add method against (per ontology)
	 * (Little point in testing it for ALL entities in an ontology)
	 */
	private static int NUM_ENTITIES_TEST_ADD = 3;
	@Test
	public void testAddLiteral() throws Exception{		
		Atom[][] annotSets = new Atom[][] {new Atom[]{}, new Atom[] {new Atom("x")}, new Atom[] {new Atom("x"), new Atom("y")} }; // TODO: No real reason to check annotation handling for ALL types of entity - exactly the same code is used
		for(OWLOntology ontology : agent.getOntologyManager().getOntologies()){	// test against all known ontologies
			for(int run=0; run<=1; run++){	// run twice to ensure duplicate additions are rejected
				for(boolean sign : signs){ // run for both positive and negative literals (l and ~l)
					for(Atom[] annots : annotSets){
						int i;
						i=0;
						for(OWLClass cls : ontology.getReferencedClasses()){
							if(i==NUM_ENTITIES_TEST_ADD) break;
							Alias alias = agent.getAliasManager().getLeft(cls);
							Atom label = alias.getLabel();
							Atom functor = alias.getFunctor();					
							testAddIndividualAssertion(sign, functor, new Atom[] {new Atom("a")}, annots, label);
							i++;
						}
						i=0;
						for(OWLObjectProperty oprop : ontology.getReferencedObjectProperties()){
							if(i==NUM_ENTITIES_TEST_ADD) break;
							Alias alias = agent.getAliasManager().getLeft(oprop);
							Atom label = alias.getLabel();
							Atom functor = alias.getFunctor();			
							Atom a = new Atom("a");
							Atom b = new Atom("b");
							testAddIndividualAssertion(sign, functor, new Atom[] {a, b}, annots, label);
							testAddIndividualAssertion(sign, functor, new Atom[] {b, a}, annots, label); // anti-symmetric properties shouldn't be rejected at this stage
							testAddIndividualAssertion(sign, functor, new Atom[] {a, a}, annots, label); // irreflexive properties shouldn't be rejected at this stage
							i++;
						}
						i=0;
						for(OWLDataProperty dprop : ontology.getReferencedDataProperties()){
							if(i==NUM_ENTITIES_TEST_ADD) break;
							Alias alias = agent.getAliasManager().getLeft(dprop);
							Atom label = alias.getLabel();
							Atom functor = alias.getFunctor();
							XSDDataType typ = XSDDataTypeUtils.get(((OWLDataType)dprop.getRanges(ontology).toArray()[0]).toString());
							if(typ == XSDDataType.XSD_STRING){
								testAddIndividualAssertion(sign, functor, new Term[] {new Atom("a"), new StringTermImpl("Winchester")}, annots, label);
							}
							/*
							// NOT NECESSARY, AS LONG AS SELITERAL WORKS FOR ALL TYPES OF DATATYPE PROPERTY, THIS WILL (SAME CODE IS USED)
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
							*/
							i++;

					}
						// All different assertion
						//TODO: below only uses a single individuals, ordering of all_different individuals important for literal equality
						// (see hack in BB.getCandidateBeliefs)
						ListTerm list = new ListTermImpl();
						list.add(new Atom("a"));
						testAddIndividualAssertion(sign, AliasFactory.OWL_ALL_DIFFERENT_FUNCTOR, new Term[] {list}, annots, agent.getPersonalOntologyLabel());
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
	
	
	private static int NUM_ENTITIES_TEST_REMOVE = 3;
	@Test
	public void testRemoveLiteral() throws Exception{
	
		
		for(boolean sign : signs){
			
			/** Testing annotation handling () */		
			Atom a = new Atom("a");
			Atom b = new Atom("b");
			Atom hotel = new Atom("hotel");
			SELiteral l = agent.getSELiteralFactory().construct(sign, hotel, a, new Atom[0], TEST_ONTOLOGY_LABEL);
			SELiteral l_x = agent.getSELiteralFactory().construct(sign, hotel, a, new Atom[] {new Atom("x")}, TEST_ONTOLOGY_LABEL);
			SELiteral l_xy = agent.getSELiteralFactory().construct(sign, hotel, a, new Atom[] {new Atom("x"), new Atom("y")}, TEST_ONTOLOGY_LABEL);
			SELiteral l_y = agent.getSELiteralFactory().construct(sign, hotel, a, new Atom[] {new Atom("y")}, TEST_ONTOLOGY_LABEL);
			
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
			
			testAddIndividualAssertion(l_xy);
			testRemoveIndividualAssertion(l_xy);
			
			testAddIndividualAssertion(l_xy);
			testRemoveIndividualAssertion(l);
			
			testAddIndividualAssertion(l_xy);
			testRemoveIndividualAssertion(l_x);
			testRemoveIndividualAssertion(l_xy);
			testRemoveIndividualAssertion(l_y);
			testRemoveIndividualAssertion(l_xy);
			testRemoveIndividualAssertion(l_y);
			testRemoveIndividualAssertion(l);	
			
		}
		/** Test removals work for other types of entities **/
		// (no need to check annotation handling, exactly the same code is used)
		// this is unnecessary, literal translation is tested elsewhere
		// and except for that, exactly the same code is used
		for(OWLOntology ontology : agent.getOntologyManager().getOntologies()){	// test against all known ontologies
			int i;
			i=0;
			for(OWLObjectProperty oprop : ontology.getReferencedObjectProperties()){
				if(i==NUM_ENTITIES_TEST_REMOVE) break;
				Alias alias = agent.getAliasManager().getLeft(oprop);
				Atom label = alias.getLabel();
				Atom functor = alias.getFunctor();
				SELiteral k = agent.getSELiteralFactory().construct(true, functor, new Atom("a"), new Atom("b"), new Atom[0], label);
				testAddIndividualAssertion(k);
				testRemoveIndividualAssertion(k);
			}
		}
		
		/** (Briefly) test it integrates correctly with contractor (contractor is tested more thoroughly elsewhere) **/
		
		
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
