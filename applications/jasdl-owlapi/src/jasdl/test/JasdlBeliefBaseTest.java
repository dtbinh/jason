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
package jasdl.test;

import jasdl.asSemantics.JasdlAgent;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.seliteral.SELiteral;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.CentralisedEnvironment;
import jason.infra.centralised.CentralisedExecutionControl;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLOntology;

/**
 * TODO: This is very out of date: needs tidying up and improving.
 * Include tests for:
 *  - annotation gathering
 *  - all_different assertions
 *  - multiple ontologies
 * @author Tom Klapiscak
 *
 */
public class JasdlBeliefBaseTest extends TestCase{
	
	private JasdlAgent agent;
	private JasdlBeliefBase bb;
	
	private String ontology_uri =  getClass().getResource("travel.owl").toExternalForm();
	private String asl_file =  getClass().getResource("asl/exclude/blank.asl").toExternalForm();
	private Atom label;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Settings stts = new Settings();
		
		label = new Atom("travel");
		
		stts.addOption("jasdl_ontologies", "\""+label+"\"");
		stts.addOption("jasdl_travel_uri", "\""+ontology_uri+"\"");
		stts.addOption("jasdl_travel_mapping_strategies", "\"jasdl.bridge.alias.DecapitaliseMappingStrategy\"");
		
		RunCentralisedMAS runner = new RunCentralisedMAS();		
		CentralisedEnvironment env = new CentralisedEnvironment(new ClassParameters("jasdl.test.TestEnv"), runner);	
		CentralisedAgArch arch = new CentralisedAgArch();
		
		arch.initAg(
				"jason.architecture.AgArch",
				"jasdl.asSemantics.JasdlAgent",
				new ClassParameters("jasdl.bb.JasdlBeliefBase"),
				asl_file,
				stts,
				runner);	
		
		runner.addAg(arch);
		arch.setEnvInfraTier(env);
		arch.setControlInfraTier(new CentralisedExecutionControl(new ClassParameters("jason.control.ExecutionControl"), runner));
		
		// because setting these options in user parameters has no effect under these circumstances
		arch.getUserAgArch().getTS().getSettings().setEvents(Settings.ORetrieve);
		arch.getUserAgArch().getTS().getSettings().setIntBels(Settings.ONewFocus);
		
		agent = (JasdlAgent)arch.getUserAgArch().getTS().getAg();
		bb = (JasdlBeliefBase)agent.getBB();
		
		
	}
	


	/**
	 * Test method for {@link jasdl.bb.JasdlBeliefBase#add(jason.asSyntax.Literal)}.
	 */
	@Test
	public void testAddLiteral() {

		// CLASS ASSERTIONS (UNARY LITERALS)
		// positive
		testAgainstBB("luxuryHotel", "hintonHouse");
		addToBB("luxuryHotel", "hintonHouse", true);
		testAgainstBB("luxuryHotel", "hintonHouse", "hintonHouse");
		
		// negative
		addToBB("destination", "durham", true);
		addToBB("~ruralArea", "durham", true);
		testAgainstBB("~ruralArea", "durham", "durham");
		addToBB("farmland", "durham", false); // inconsistent assertion since durham can't be simultaneously non-rural and farmland
		checkConsistency();
		testAgainstBB("farmland", "durham");
		
		// conflict rollback
		addToBB("~luxuryHotel", "hintonHouse", false);
		checkConsistency();
		
		// unknown class
		addToBB("nonexistent","i", false);
		testAgainstBB("nonexistent", "X");
		
		// unknown individual - will be created
		addToBB("sightseeing", "new0", true);
		testAgainstBB("sightseeing", "new0", "new0");		
		
		// PROPERTY ASSERTIONS (BINARY LITERALS)
		// positive
		addToBB("adventure","goape",true);
		addToBB("campground","camp",true);
		addToBB("destination", "lakeDistrict", true);
		addToBB("hasActivity", "lakeDistrict,goape",true);
		addToBB("hasAccommodation","lakeDistrict,camp",true);
		testAgainstBB("backpackersDestination", "lakeDistrict", "lakeDistrict");// we can now infer that lakeDistrict is a backpackers destination since it has an adventure activity (goape) and budget accommodation (camp)
		
		// unknown predicate
		addToBB("nonexistent", "adventure,goape", false);
		testAgainstBB("nonexistent", "X,Y");
		
		// unknown individuals - will be created
		addToBB("hasActivity", "new1,new2", true);
		testAgainstBB("hasActivity", "new1,new2", "new1,new2");
		
		// inconsistency rollback mechanism
		addToBB("onlyCampground","desert",true);
		addToBB("hasAccommodation","desert,fourSeasons", false); // inconsistent, onlyCampground can't have accommodation luxuryHotel!
		addToBB("hasAccommodation", "desert,camp", true);
		checkConsistency();
		testAgainstBB("hasAccommodation","desert,X", new String[] {"desert,camp"});
		
		// datatype
		
		addToBB("hasDateTime", "jim,\"2008-01-08T16:01:55\"", true);
		addToBB("hasFloat", "jim,12", true);
		addToBB("hasDate", "jim,\"2008-01-08\"", true);
		addToBB("hasCity", "jim,\"Durham\"", true);
		addToBB("hasTime", "jim,\"16:01:55\"", true);
		addToBB("hasEMail", "jim,\"something@something.com\"", true);
		addToBB("hasBoolean", "jim,true", true);
		addToBB("hasInteger", "jim,99", true);
		addToBB("hasStreet", "jim,\"Hallgarth Street\"", true);
		addToBB("hasZipCode", "jim,\"dh13ay\"", true);
		
		// bad data format handling
		addToBB("hasDateTime", "jim,\"2008-14-08T16:01:55\"", false);// invalid date
		addToBB("hasDateTime", "jim,\"2008-12-08T25:01:55\"", false);// invalid time
		addToBB("hasDateTime", "jim,22", false);// wrong type
		addToBB("hasFloat", "jim,\"hello\"", false);// wrong type
		addToBB("hasCity", "jim,22", false);// wrong type
		
		// inconsistency rollback mechanism
		testAgainstBB("hasEMail","tom,X", "tom,\"t.g.klapiscak@durham.ac.uk\"");
		addToBB("hasEMail", "tom,\"tom.klapiscak@googlemail.com\"", false);// inconsistent since hasEMail is functional and tom already has an email listed
		checkConsistency();
		testAgainstBB("hasEMail","tom,X", "tom,\"t.g.klapiscak@durham.ac.uk\"");
		
		// unknown predicate
		addToBB("nonexistent", "tom,\"something\"", false);
		
	}
	
	
	/**
	 * Test method for {@link jasdl.bb.JasdlBeliefBase#getRelevant(jason.asSyntax.Literal)}.
	 */
	@Test
	public void testGetRelevantLiteral() {
		
		// OWL CLASSES
		
		// positive, ground, direct
		testAgainstBB("hotel", "fourSeasons", new String[] {"fourSeasons"});		
		testAgainstBB("nationalPark", "capeYork"); //too specific
		testAgainstBB("nothing", "woomera"); //too specific
		
		// positive, ground, inferred
		testAgainstBB("city", "blueMountains");// disjoint
		testAgainstBB("accommodation", "bnb", new String[] {"bnb"});
		
		// positive, unground, inferred
		testAgainstBB("accommodation", "A", new String[] {"fourSeasons", "bnb"});
		testAgainstBB("hotel", "A", new String[] {"fourSeasons"});				
		
		// negative, ground, direct
		testAgainstBB("~ruralArea", "sydney", new String[] {"sydney"});
		testAgainstBB("~ruralArea", "blueMountains");

		// negative, ground, inferred
		testAgainstBB("~city", "blueMountains", new String[] {"blueMountains"});// since blueMountains is rural area
		testAgainstBB("~city", "canberra"); // since canberra is a capital	
		
		// negative, unground
		testAgainstBB("~ruralArea", "A", new String[] {"cairns", "canberra", "coonabarabran", "sydney"});
		testAgainstBB("~city", "A", new String[] {"blueMountains", "capeYork", "warrumbungles", "woomera"});	
		
		
		
		
		// OWL OBJECT PROPERTIES
		
		// unground, inferred
		//testAgainstBB("hasRating", "X,Y", new String[] {"fourSeasons,threeStarRating"});
		
		// left-ground, inferred
		testAgainstBB("hasRating", "fourSeasons,Y", new String[] {"fourSeasons,threeStarRating"});
		testAgainstBB("hasRating", "bnb,Y");
		
		// left-ground, asserted
		testAgainstBB("hasAccommodation", "bondiBeach,X", new String[] {"bondiBeach,fourSeasons"});
		
		/*
		// right-ground, inferred
		testAgainstBB("hasRating", "X,threeStarRating", new String[] {"fourSeasons,threeStarRating"});
		testAgainstBB("hasRating", "X,twoStarRating");
		*/
		
		// fully-ground, inferred
		testAgainstBB("hasRating", "fourSeasons,threeStarRating", new String[] {"fourSeasons,threeStarRating"});
		testAgainstBB("hasRating", "fourSeasons,twoStarRating");
		testAgainstBB("hasRating", "bnb,threeStarRating");
		
		
		
		
		// OWL DATATYPE PROPERTIES
		
		// unground
		/*
		testAgainstBB("hasDateTime", "X,Y", new String[] {"tom, \"2007-12-20T20:16:55\""});
		testAgainstBB("hasFloat", "X,Y", new String[] {"tom,22.0", "ben,44.0"});
		testAgainstBB("hasDate", "X,Y", new String[] {"tom,\"2007-12-20\""});
		testAgainstBB("hasCity", "X,Y", new String[] {"tom,\"Durham\"", "tom,\"Winchester\""});
		testAgainstBB("hasTime", "X,Y", new String[] {"tom,\"20:16:57\""});
		testAgainstBB("hasBoolean", "X,Y", new String[] {"ben,false", "tom,true", "tom,false"});
		testAgainstBB("hasEMail", "X,Y", new String[] {"tom,\"t.g.klapiscak@durham.ac.uk\"", "ben,\"vhdfhg@FDgdfg\""});
		testAgainstBB("hasInteger", "X,Y", new String[] {"ben,3", "tom,2"});
		testAgainstBB("hasStreet", "X,Y", new String[] {"tom,\"Winchester\""});
		testAgainstBB("hasZipCode", "X,Y", new String[] {"tom,\"so237qy\""});
		*/
		
		// left-ground
		testAgainstBB("hasDateTime", "tom,Y", new String[] {"tom, \"2007-12-20T20:16:55\""});
		testAgainstBB("hasDateTime", "ben,Y");
		testAgainstBB("hasFloat", "tom,Y", new String[] {"tom,22.0"});
		testAgainstBB("hasDate", "tom,Y", new String[] {"tom,\"2007-12-20\""});
		testAgainstBB("hasDate", "ben,Y");		
		testAgainstBB("hasCity", "tom,Y", new String[] {"tom,\"Durham\"", "tom,\"Winchester\""});
		testAgainstBB("hasTime", "tom,Y", new String[] {"tom,\"20:16:57\""});
		testAgainstBB("hasBoolean", "ben,Y", new String[] {"ben,false"});
		testAgainstBB("hasEMail", "tom,Y", new String[] {"tom,\"t.g.klapiscak@durham.ac.uk\""});
		testAgainstBB("hasInteger", "ben,Y", new String[] {"ben,3"});
		testAgainstBB("hasStreet", "tom,Y", new String[] {"tom,\"Winchester\""});
		testAgainstBB("hasZipCode", "tom,Y", new String[] {"tom,\"so237qy\""});
		
		/*
		// right-ground
		testAgainstBB("hasDateTime", "X,\"2007-12-20T20:16:55\"", new String[] {"tom, \"2007-12-20T20:16:55\""});
		testAgainstBB("hasFloat", "X,44.0", new String[] {"ben,44.0"});
		testAgainstBB("hasFloat", "X,100.0");
		testAgainstBB("hasDate", "X,\"2007-12-20\"", new String[] {"tom,\"2007-12-20\""});
		testAgainstBB("hasCity", "X,\"Durham\"", new String[] {"tom,\"Durham\""});
		testAgainstBB("hasTime", "X,\"20:16:57\"", new String[] {"tom,\"20:16:57\""});
		testAgainstBB("hasBoolean", "X,false", new String[] {"ben,false", "tom,false"});
		testAgainstBB("hasEMail", "X,\"vhdfhg@FDgdfg\"", new String[] {"ben,\"vhdfhg@FDgdfg\""});
		testAgainstBB("hasInteger", "X,2", new String[] {"tom,2"});
		testAgainstBB("hasStreet", "X,\"Winchester\"", new String[] {"tom,\"Winchester\""});
		testAgainstBB("hasZipCode", "X,\"so237qy\"", new String[] {"tom,\"so237qy\""});
		*/
		
		// fully-ground
		testAgainstBB("hasDateTime", "tom,\"2007-12-20T20:16:55\"", new String[] {"tom, \"2007-12-20T20:16:55\""});
		testAgainstBB("hasFloat", "ben,44.0", new String[] {"ben,44.0"});
		testAgainstBB("hasFloat", "ben,22.0");
		testAgainstBB("hasDate", "tom,\"2007-12-20\"", new String[] {"tom,\"2007-12-20\""});
		testAgainstBB("hasCity", "tom,\"Durham\"", new String[] {"tom,\"Durham\""});
		testAgainstBB("hasTime", "tom,\"20:16:57\"", new String[] {"tom,\"20:16:57\""});
		testAgainstBB("hasBoolean", "ben,false", new String[] {"ben,false"});
		testAgainstBB("hasEMail", "ben,\"vhdfhg@FDgdfg\"", new String[] {"ben,\"vhdfhg@FDgdfg\""});
		testAgainstBB("hasInteger", "tom,2", new String[] {"tom,2"});
		testAgainstBB("hasStreet", "tom,\"Winchester\"", new String[] {"tom,\"Winchester\""});
		testAgainstBB("hasZipCode", "tom,\"so237qy\"", new String[] {"tom,\"so237qy\""});
	}		
	

	/**
	 * Test method for {@link jasdl.bb.JasdlBeliefBase#remove(jason.asSyntax.Literal)}.
	 */
	@Test
	public void testRemoveLiteral() {
		// CLASS ASSERTIONS (UNARY LITERALS)
		addToBB("luxuryHotel", "swish", true);
		//removeFromBB("hotel", "swish", true); // WILL work, we CAN remove individuals using more general assertions
		testAgainstBB("hotel", "swish", "swish");
		testAgainstBB("luxuryHotel", "swish", "swish");
		removeFromBB("luxuryHotel", "swish", true);
		testAgainstBB("luxuryHotel", "swish");
		testAgainstBB("hotel", "swish");
		
		// PROPERTY ASSERTIONS (BINARY LITERALS)
		addToBB("bedAndBreakfast", "paddys", true);
		addToBB("destination", "bristol", true);
		addToBB("hasAccommodation", "bristol, paddys", true);
		testAgainstBB("hasAccommodation", "bristol,paddys", "bristol,paddys");
		removeFromBB("hasAccommodation", "bristol,paddys", true);
		testAgainstBB("hasAccommodation", "bristol,paddys");
	}
	

	/**
	 * Test method for {@link jasdl.bb.JasdlBeliefBase#contains(jason.asSyntax.Literal)}.
	 */
	@Test
	public void testContainsLiteral() {
		addToBB("safari", "longleat", true);
		testAgainstBB("safari", "longleat", true);
		testAgainstBB("activity", "longleat", true); // true since contains includes inferences
		testAgainstBB("nonexistent", "longleat", false); //unknown concept
		testAgainstBB("accommodation", "safari", false); //untrue
	}
	
	
	/* AUXILIARY METHODS */
	
	public void checkConsistency(){
		try {
			for(OWLOntology ontology : agent.getReasoner().getLoadedOntologies()){
				if(!agent.getReasoner().isConsistent(ontology)){
					fail("Inconsistency detected");
				}	
			}
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}		
	}
	
	public void addToBB(String functor, String params, boolean expected){
		Literal l = Literal.parseLiteral(functor+"("+params+")");
		addOntologyAnnotation(l);
		boolean actual = bb.add(l);
		if(actual != expected){
			fail("On addition of "+l+". Expected "+expected+". Actual: "+actual+".");
		}
	}
	
	public void removeFromBB(String functor, String params, boolean expected){
		Literal l = Literal.parseLiteral(functor+"("+params+")");
		addOntologyAnnotation(l);
		boolean actual = bb.remove(l);
		if(actual != expected){
			fail("On removal of "+l+". Expected "+expected+". Actual: "+actual+".");
		}
	}
	


	
	public void testAgainstBB(String functor, String param, boolean expected){
		String l = functor+"(";
		String r = ")";
		Literal toTest = Literal.parseLiteral(l+param+r);
		addOntologyAnnotation(toTest);
		boolean actual = bb.contains(toTest)!=null;
		if(actual!=expected){//TODO: Check unification (if ever implemented) is working in bb.contains
			fail("On relevancy check for "+toTest+". Expected: "+expected+". Actual: "+actual+".");		
		}
	}
	
	public void testAgainstBB(String functor, String param){
		testAgainstBB(functor, param, new String[] {});
	}
	
	
	public void testAgainstBB(String functor, String param, String expectedParam){
		testAgainstBB(functor, param, new String[] {expectedParam});
	}

	
	/**
	 * Note: Set used for good reason since ordering of relevancies is not significant
	 * TODO: Actually use Jason testgoal mechanisms?
	 * @param functor
	 * @param param
	 * @param expectedParams
	 */
	public void testAgainstBB(String functor, String param, String[] expectedParams){		
		Literal toTest = Literal.parseLiteral(functor+"("+param+")");
		addOntologyAnnotation(toTest);
		
		// overrided to use value rather than reference equality testing for containsAll
		Set<Literal> expected = new HashSet<Literal>(){
			@Override
			public boolean containsAll(Collection<?> others){
				for(Object mine : this){
					boolean yep = false;
					for(Object other : others){
						if(mine.equals(other)){
							yep = true;
							break;
						}						
					}
					if(!yep){
						return false;
					}
				}
				return true;
			}
		};
		for(String expectedParam : expectedParams){
			Literal expectedLiteral = Literal.parseLiteral(functor+"("+expectedParam+")");
			addOntologyAnnotation(expectedLiteral);
			expected.add(expectedLiteral);
		}
		Set<Literal> actual = itToHashSet(bb.getRelevant(toTest));
		if(!expected.equals(actual)){
			fail("On relevancy check for "+toTest+". Expected: "+expected+". Actual: "+actual+".");
		}
	}
	
	public static final <T extends Object> HashSet<T> itToHashSet(Iterator<T> it){
		HashSet<T> set = new HashSet<T>();
		while(it.hasNext()){
			set.add(it.next());
		}
		return set;
	}
	
	
	private void addOntologyAnnotation(Literal l){		
		Structure o = new Structure(SELiteral.ONTOLOGY_ANNOTATION_FUNCTOR);
		o.addTerm(label);		
		l.addAnnot(o);
	}
	
}