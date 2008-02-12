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
package jasdl.junit;

import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.CentralisedEnvironment;
import jason.infra.centralised.CentralisedExecutionControl;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.io.File;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Tom Klapiscak
 * 
 * TODO: Generalise to Jason junit testing interface?
 */
public class JasdlAgentModuleTest extends TestCase{

	private String ASL_ROOT = getClass().getResource("asl").toString().replace("file:", "");

	private String ontology_uri =  getClass().getResource("travel.owl").toExternalForm();
	
	private CentralisedAgArch arch;
	private TestEnv testEnv;

	@Test
	public void testAllAslFiles() {
		File aslRoot = new File(ASL_ROOT);
		File[] asls = aslRoot.listFiles();
		for(File asl : asls){
			if(asl.getName().contains(".asl")){
				try {
					Settings stts = new Settings();
					stts.addOption("ontologies", "\"travel\"");
					stts.addOption("travel_uri", "\""+ontology_uri+"\"");
					stts.addOption("travel_automap_classes", "\"jasdl.automap.uncapitalise_individuals, jasdl.automap.uncapitalise_concepts\"");
					
					RunCentralisedMAS runner = new RunCentralisedMAS();		
					CentralisedEnvironment env = new CentralisedEnvironment(new ClassParameters("jasdl.junit.TestEnv"), runner);
					testEnv = (TestEnv)env.getUserEnvironment();		
					arch = new CentralisedAgArch();
					
					arch.initAg(
							"jason.architecture.AgArch",
							"jasdl.asSemantics.JasdlAgent",
							new ClassParameters("jasdl.bb.OwlBeliefBase"),
							asl.getAbsolutePath(),
							stts,
							runner);	
					
					runner.addAg(arch);
					arch.setEnvInfraTier(env);
					arch.setControlInfraTier(new CentralisedExecutionControl(new ClassParameters("jason.control.ExecutionControl"), runner));
					
					// because setting these options in user parameters has no effect under these circumstances
					arch.getUserAgArch().getTS().getSettings().setEvents(Settings.ORetrieve);
					arch.getUserAgArch().getTS().getSettings().setIntBels(Settings.ONewFocus);
					
				} catch (JasonException e) {
					fail("Init error with asl source: "+asl);
				}		
				
				arch.getLogger().setLevel(Level.SEVERE);
		
				TransitionSystem ts = arch.getUserAgArch().getTS();
				while(true){
					ts.reasoningCycle();
					if(testEnv.isFailure()){
						fail("Failed on: "+asl.getName()+". Asserted. Message: "+testEnv.getFailureMsg());
					}else if(testEnv.isSuccess()){
						break;
					}
					
					if(ts.canSleep() && ts.getC().getSelectedOption()==null && ts.getC().getSelectedIntention() == null){
						fail("Failed on: "+asl.getName()+". Reached end of execution.");
					}
				}	
			}
		}		
	}

}
