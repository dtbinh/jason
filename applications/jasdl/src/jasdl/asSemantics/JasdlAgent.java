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
package jasdl.asSemantics;

import static jasdl.util.Common.DELIM;
import static jasdl.util.Common.strip;
import jasdl.asSyntax.JasdlPlanLibrary;
import jasdl.bb.OwlBeliefBase;
import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.TransitionSystem;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.net.URI;
import java.net.URISyntaxException;

import jmca.asSemantics.JmcaAgent;

import org.apache.log4j.PropertyConfigurator;

/**
 * Performs initialisation specific to JASDL agent module (ontology manager instantiation etc)
 * @author Tom Klapiscak
 *
 */
public class JasdlAgent extends JmcaAgent {
	public static String ONTOLOGY_ALIASES_PARAMETER = "ontologies";
	public static String ONTOLOGY_URI_PARAMETER = "_uri";
	public static String ONTOLOGY_AUTOMAP_CLASSES_PARAMETER = "_automap_classes";
	public static String DEFAULT_AUTOMAP_CLASSES_PARAMETER = "jasdl.automap.uncapitalise_individuals, jasdl.automap.uncapitalise_concepts";
	
	
	private OntologyManager manager;
	
	public JasdlAgent(){
		super();
		PropertyConfigurator.configure(System.getProperty("user.dir")+"/log4j.properties");
		this.manager = OntologyManager.getOntologyManager(this);
		JasdlPlanLibrary pl = new JasdlPlanLibrary(this.manager);
		setPL(pl);
	}
	
	
	@Override
	public TransitionSystem initAg(AgArch arch, BeliefBase bb, String asSrc, Settings stts) throws JasonException {		
		
		// Fetch and parse ontology alias definition string
		String _aliases = strip(stts.getUserParameter(ONTOLOGY_ALIASES_PARAMETER));		
		String[] aliases = _aliases.split("["+DELIM+"]");
		

		// for each given alias
		for(String alias : aliases){
			alias = alias.trim();
			// retrieve and validate physical namespace of supplied ontology
			String _physicalNs = strip(stts.getUserParameter(alias+ONTOLOGY_URI_PARAMETER));
			if(_physicalNs == null){
				throw new JasdlException("No physical namespace specified for "+alias);
			}
			URI physicalNs;
			try{
				physicalNs = new URI(_physicalNs);
			}catch(URISyntaxException e){
				throw new JasdlException("Invalid physical namespace specified for "+alias+": "+_physicalNs);
			}
			
			JasdlOntology ont = manager.createJasdlOntology(physicalNs, alias);
			
			if(bb instanceof OwlBeliefBase){ // if this is used in conjunction with OwlBeliefBase (likely but worth checking anyway) then set its manager
				((OwlBeliefBase)bb).setOntologyManager(manager);
			}
			
			String automapClassesParameter = stts.getUserParameter(alias+ONTOLOGY_AUTOMAP_CLASSES_PARAMETER);
			if(automapClassesParameter == null){
				automapClassesParameter = DEFAULT_AUTOMAP_CLASSES_PARAMETER;
			}else{
				automapClassesParameter = strip(automapClassesParameter, "\"");
			}
			ont.performAutomaps(automapClassesParameter);// add automated mappings in order supplied (if present)			
			
		}
		
		return super.initAg(arch, bb, asSrc, stts);
	}

}
