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
package jasdl.ontology;

import static jasdl.util.Common.ONTOLOGY_ANNOTATION;
import static jasdl.util.Common.getAnnot;
import jasdl.util.JasdlException;
import jason.asSemantics.Agent;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Provides a single agent ontological services
 * 
 * Note: Individuals may, or may not have annotations (e.g. optional [direct] annotation)
 * 
 * @author Tom Klapiscak
 *
 */
public class OntologyManager {
	
	/**
	 * Used for retrieving the ontology manager assigned to each agent. 
	 * Currently used by internal actions and JasdlAgArch
	 */
	private static HashMap<Agent, OntologyManager> agentToOntologyManagerMap = new HashMap<Agent, OntologyManager>();
	
	private HashMap<String, JasdlOntology> aliasToOntologyMap;
	private HashMap<URI, JasdlOntology> physicalNsToOntologyMap;
	private HashMap<URI, JasdlOntology> logicalNsToOntologyMap;
	
	private Vector<JasdlOntology> loadedOntologies;
	
	/**
	 * The agent this manager is serving
	 */
	private Agent agent;
	
	
	public OntologyManager(Agent agent){
		this.agent = agent;
		agentToOntologyManagerMap.put(agent, this);
		aliasToOntologyMap = new HashMap<String, JasdlOntology>();
		physicalNsToOntologyMap = new HashMap<URI, JasdlOntology>();
		logicalNsToOntologyMap = new HashMap<URI, JasdlOntology>();
		loadedOntologies = new Vector<JasdlOntology>();
	}

	/**
	 * Creates an anonymous ontology
	 * 
	 * @param physicalNs
	 * @return
	 * @throws JasdlException
	 */
	public JasdlOntology createJasdlOntology(URI physicalNs) throws JasdlException{
		return createJasdlOntology(physicalNs, getUniqueAlias());
	}
	
	/**
	 * Creates a named ontology
	 * 
	 * @param physicalNs
	 * @param alias
	 * @return
	 * @throws JasdlException	if alias already in use
	 */
	public JasdlOntology createJasdlOntology(URI physicalNs, String alias) throws JasdlException{
		JasdlOntology ont = new JasdlOntology(physicalNs, alias, this);		
		physicalNsToOntologyMap.put(physicalNs, ont);
		logicalNsToOntologyMap.put(ont.getLogicalNs(), ont);
		loadedOntologies.add(ont);
		mapLabelToOntology(ont, ont.getLabel());
		return ont;
	}
	
	/**
	 * Change an ontology alias mapping
	 * 
	 * @param ont				the ontology that the given alias should map to
	 * @param alias				the alias to map to the given ontology
	 * @throws JasdlException	if alias already in use
	 */
	public void mapLabelToOntology(JasdlOntology ont, String alias) throws JasdlException{
		if(aliasToOntologyMap.containsKey(alias)){
			throw new JasdlException("alias "+alias+"already in use");
		}		
		aliasToOntologyMap.remove(ont.getLabel());
		aliasToOntologyMap.put(alias, ont);
		getLogger().finest("added: "+aliasToOntologyMap.get(alias)+" with key "+alias);	
	}
	

	
	public List<JasdlOntology> getLoadedOntologies(){
		return loadedOntologies;
	}
	
	public String getUniqueAlias(){
		return "anon_alias_"+loadedOntologies.size();
	}
	

	/**
	 * Get the ontology manager associated with the supplied agent
	 * Instantiates a new manager if no association found
	 * 
	 * @param agent		the agent whose ontology manager is required
	 * @return			the ontology manager associated with the supplied agent
	 */
	public static OntologyManager getOntologyManager(Agent agent){
		OntologyManager instance =  agentToOntologyManagerMap.get(agent);
		if(instance == null){
			instance = new OntologyManager(agent);
		}
		return instance;
	}
	
	/* Various methods providing alternatives for retrieving a JasdlOntology */
	
	/**
	 * Retrieves a JasdlOntology based on the ontology annotation assigned to the supplied predicate.
	 * Annotation must be an alias (indicated by atomic ontology annotation argument)
	 * Returns null if no such annotation is present.
	 * 
	 * @param	p	the predicate whose annotations should be scanned for the ontology annotation
	 * @return		the JasdlOntology associated with the alias given by the ontology annotation of this predicate, if found and valid. Null if no ontology annotation is present.
	 * @throws	JasdlException	if ontology annotation is invalid
	 */
	public JasdlOntology getJasdlOntology(Pred p) throws JasdlException{
		List<JasdlOntology> onts = listJasdlOntologies(p);
		if(onts.isEmpty()){
			return null;
		}else{
			return onts.get(0);
		}
	}
	
	/**
	 * For dealing with unground ontology annotations
	 * @param p
	 * @return	empty list if no ontology annotation found
	 * @throws JasdlException	if invalid ontology annotation found
	 */
	public List<JasdlOntology> listJasdlOntologies(Pred p) throws JasdlException{
		Vector<JasdlOntology> onts = new Vector<JasdlOntology>();
		
		Term _annot = getAnnot(p, ONTOLOGY_ANNOTATION);
		if(_annot == null){
			return onts;
		}
		if(_annot.isGround()){ // ground annotation, just one ontology to return
			if(_annot.isStructure()){
				Structure annot = (Structure)_annot;
				if(annot.getArity() != 1){
					throw new JasdlException("Invalid ontology annotation on "+p);
				}
				Term ref = annot.getTerm(0);
				if(ref.isAtom()){
					 // we have an alias
					onts.add(getJasdlOntology(ref.toString()));
				}else{
					throw new JasdlException("Invalid ontology annotation on "+p);
				}
			}
		}else{ // unground, add all known aliases
			onts.addAll(getLoadedOntologies());
		}
		return onts;		
	}
	
	
	public JasdlOntology getJasdlOntology(String alias) throws JasdlException{		
		JasdlOntology ont = aliasToOntologyMap.get(alias);
		if(ont == null){
			throw new JasdlException("Unknown ontology alias: "+alias);
		}
		return ont;
	}
	
	/**
	 * Attempt to get a Jasdl ontology by its physical uri.
	 * Changes: Does not instantiate ontology if non-existent!
	 * @param physicalNs
	 * @return	found jasdl ontology, or null if none found
	 */
	public JasdlOntology getJasdlOntology(URI physicalNs) throws JasdlException{
		return physicalNsToOntologyMap.get(physicalNs);
	}

	/**
	 * Return the agent this manager is serving
	 * @return
	 */
	public Agent getAgent() {
		return agent;
	}
	
	/**
	 * Convenience method to return the name of the agent this manager is serving
	 * @return
	 */
	public String getAgentName(){
		try{
			return getAgent().getTS().getUserAgArch().getAgName();
		}catch(NullPointerException e){
			return "uninitialised";
		}
	}
	
	public Logger getLogger(){
		return Logger.getLogger(getAgentName());
	}
	
	
	


	

}
