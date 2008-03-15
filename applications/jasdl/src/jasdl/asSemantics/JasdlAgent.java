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

import static jasdl.util.Common.ANON_ONTOLOGY_LABEL_PREFIX;
import static jasdl.util.Common.DELIM;
import static jasdl.util.Common.getOntologyAnnotation;
import static jasdl.util.Common.strip;
import jasdl.asSyntax.JasdlPlanLibrary;
import jasdl.bb.JasdlBeliefBase;
import jasdl.bridge.JasdlOntology;
import jasdl.bridge.alias.Alias;
import jasdl.mapping.MappingStrategy;
import jasdl.util.InvalidSELiteralException;
import jasdl.util.JasdlException;
import jasdl.util.UnknownReferenceException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.net.SocketFactory;

import jmca.asSemantics.JmcaAgent;

import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * All .mas2j processing is confined to this class
 * @author tom
 *
 */
public class JasdlAgent extends JmcaAgent {
	private static String MAS2J_PREFIX					= "jasdl";
	private static String MAS2J_ONTOLOGIES 				= "_ontologies";
	private static String MAS2J_URI						= "_uri";
	private static String MAS2J_MAPPING_STRATEGIES		= "_mapping_strategies";
	private static String MAS2J_MANUAL_CLASS_MAPPINGS	= "_manual_class_mappings";
	private static String MAS2J_PROXIES					= "_proxy";
	private static String[] PROXY_PROTOCOLS				= new String[] {"http.", "https.", "ftp.", "socks"}; // no . after socks deliberate
	
	private static List<InnerProxy> proxies;
	
	private OWLOntologyManager manager;	
	private HashMap<Atom, JasdlOntology> labelToOntologyMap;
	private HashMap<URI, JasdlOntology> uriToOntologyMap;
	
	public JasdlAgent(){
		labelToOntologyMap = new HashMap<Atom, JasdlOntology>();
		uriToOntologyMap = new HashMap<URI, JasdlOntology>();
		proxies = new Vector<InnerProxy>();
		manager = OWLManager.createOWLOntologyManager();
		setPL( new JasdlPlanLibrary(this) );
	}

		
	@Override
	public TransitionSystem initAg(AgArch arch, BeliefBase bb, String src, Settings stts) throws JasonException {
		if(!(bb instanceof JasdlBeliefBase)){
			throw new JasdlException("JASDL must be used in combination with the jasdl.bb.OwlBeliefBase class");
		}
		
		((JasdlBeliefBase)bb).setAgent(this);		
		
		// setup log4j properties
		PropertyConfigurator.configure(System.getProperty("user.dir")+"/log4j.properties");
		
		loadProxies( stts );		
		loadOntologies( stts );
		applyManualMappings( stts );
		applyMappingStrategies( stts );	
		applyMiscMappings();
		
		
		return super.initAg(arch, bb, src, stts);
	}
	
	private void loadProxies(Settings stts) throws JasdlException{
		//TODO: use a more flexible approach for proxy specification (e.g. ProxySelector)
		for(String protocol : PROXY_PROTOCOLS){
			// strip . if present (i.e. not socks)
			String cleanProtocol = protocol.replace(".", "");
			
			String proxy = prepareUserParameter(stts, MAS2J_PREFIX + "_" + cleanProtocol + MAS2J_PROXIES);
			if(proxy.length() > 0){
				String[] triple = proxy.split(":");
				if(triple.length != 2){
					throw new JasdlException("Invalid proxy triple "+proxy);
				}				
				String ip		= triple[0].trim();
				String port		= triple[1].trim();			
				int portno;
				try {
					portno = Integer.parseInt(port);
				} catch (NumberFormatException e1) {
					throw new JasdlException("Invalid proxy port number "+port);
				}

				addProxy(protocol, ip, portno);
			}
		}
				
	}	
	
	/**
	 * Load ontologies as specified in .mas2j settings
	 * @param stts	.mas2j settings
	 * @throws JasdlException	if instantiation of an ontology fails
	 */
	private void loadOntologies(Settings stts) throws JasdlException{
		String[] labels = splitUserParameter( stts, MAS2J_PREFIX + MAS2J_ONTOLOGIES );
		for(String label : labels){
			URI physicalURI;
			try {
				physicalURI = new URI(prepareUserParameter( stts, MAS2J_PREFIX + "_" + label + MAS2J_URI ));
				createJasdlOntology(label, physicalURI);
			} catch (Exception e) {
				throw new JasdlException("Unable to instantiate ontology \""+label+"\". Reason: "+e.getMessage());
			}			
		}
	}	
	


	private void applyManualMappings(Settings stts) throws JasdlException{
		for(JasdlOntology ont : getLoadedOntologies()){
			String[] mappings = splitUserParameter(stts, MAS2J_PREFIX + "_" + ont.getLabel() + MAS2J_MANUAL_CLASS_MAPPINGS);
			for(String mapping : mappings){
				String[] split = mapping.split("=");
				if(split.length == 2){
					Alias alias = new Alias(split[0].trim());
					URI real = URI.create(ont.getOwl().getURI() + "#" + split[1].trim());
					ont.addMapping(alias, ont.toObject(real));
				}
			}
		}
	}

	/**
	 * Extracts mapping strategy related entries from .mas2j settings and applies to relevant ontologies
	 * @param stts	.mas2j settings
	 */
	private void applyMappingStrategies(Settings stts) throws JasdlException{
		for(JasdlOntology ont : getLoadedOntologies()){
			List<MappingStrategy> strategies = new Vector<MappingStrategy>();
			String[] strategyNames = splitUserParameter(stts, MAS2J_PREFIX + "_" + ont.getLabel() + MAS2J_MAPPING_STRATEGIES);
			for(String strategyName : strategyNames){
				try {
					Class cls = Class.forName(strategyName);
					Constructor ct = cls.getConstructor(new Class[] {});
					MappingStrategy strategy = (MappingStrategy)ct.newInstance(new Object[] {});
					if(strategy == null){
						throw new JasdlException("Unknown mapping strategy class: "+strategy);
					}else{
						strategies.add(strategy);
					}
				}catch (Throwable e) {
					throw new JasdlException("Error instantiating mapping strategy "+strategyName+". Reason: "+e);
				}
			}
			ont.applyMappingStrategies(strategies);
		}		
	}
	
	/**
	 * Maps thing and nothing
	 * @throws JasdlException
	 */
	private void applyMiscMappings() throws JasdlException{
		for(JasdlOntology ont : getLoadedOntologies()){
			ont.addMapping(new Alias("thing"), manager.getOWLDataFactory().getOWLThing());
			ont.addMapping(new Alias("nothing"), manager.getOWLDataFactory().getOWLNothing());
		}
	}

	
	/**
	 * Convenience method to strip quotes and trim a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to prepare
	 * @return
	 */
	private String prepareUserParameter(Settings stts, String name){
		String p = stts.getUserParameter(name);
		if(p == null){
			return "";
		}else{
			return strip(p, "\"").trim();
		}
	}

	/**
	 * Convenience method to strip quotes, trim and split (by default delimiter) a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to split
	 * @return
	 */
	private String[] splitUserParameter(Settings stts, String name, String delim){
		String[] elems = prepareUserParameter(stts, name).split(delim);
		for(int i=0; i<elems.length; i++){
			elems[i] = strip(elems[i], "\"").trim();
		}
		return elems;
	}
	
	/**
	 * Convenience method to strip quotes, trim and split (by default delimiter) a .mas2j setting
	 * @param stts	.mas2j settings
	 * @param name	name of the setting to split
	 * @return
	 */
	private String[] splitUserParameter(Settings stts, String name){
		return splitUserParameter(stts, name, DELIM);
	}
	
	public JasdlOntology getOntology(String label) throws UnknownReferenceException{
		return getOntology(new Atom(label));
	}
	
	
	/**
	 * Get a known ontology from an atomic label
	 * @param label
	 * @return
	 * @throws UnknownReferenceException
	 */
	public JasdlOntology getOntology(Atom label) throws UnknownReferenceException{
		JasdlOntology ont = labelToOntologyMap.get(label);
		if(ont == null){
			throw new UnknownReferenceException("Unknown ontology label: "+label);
		}else{
			return ont;
		}
	}	
	
	/**
	 * Get ALL ontologies referred to by a semantically-enriched literal (i.e. handle ungrounded ontology annotations correctly)
	 * If ungrounded, only returns ontologies in which this alias is meaningful
	 * @param l
	 * @return
	 * @throws JasdlException
	 */
	public List<JasdlOntology> getOntologies(Literal l) throws JasdlException{
		List<JasdlOntology> onts = new Vector<JasdlOntology>();
		Structure o = getOntologyAnnotation(l);
		Term label = o.getTerm(0);
		if(label.isGround()){
			if(label.isAtom()){
				onts.add(getOntology((Atom)label));
			}else{
				throw new InvalidSELiteralException("Grounded ontology annotations must be atomic");
			}
		}else{
			for(JasdlOntology ont : getLoadedOntologies()){
				Alias alias = ont.toAlias(l);
				if(ont.isMapped(alias)){ // alias must be mapped in ontology
					onts.add(ont);
				}
			}
		}		
		return onts;
	}
	
	/**
	 * Get a known ontology from a physical URI
	 * @param uri
	 * @return
	 * @throws UnknownReferenceException
	 */
	public JasdlOntology getOntology(URI uri) throws UnknownReferenceException{
		JasdlOntology ont = uriToOntologyMap.get(uri);
		if(ont == null){
			throw new UnknownReferenceException("Unknown ontology uri: "+uri);
		}else{
			return ont;
		}
	}
	
	/**
	 * Returns true if the supplied ontology label refers to an ontology known by this agent
	 * @param label
	 * @return
	 */
	public boolean knownOntology(String label){
		try{
			getOntology(label);
		}catch(UnknownReferenceException e){
			return false;
		}
		return true;
	}
	
	/**
	 * Returns true if supplied literal is a valid semantically-enriched literal
	 * @param l
	 * @return
	 */
	public boolean isSELiteral(Literal l){
		try{
			getOntologies(l);
			return true;
		}catch(JasdlException e){
			return false;
		}
	}
	
	

	/**
	 * Instantiate a new Jasdl Ontology with a supplied atomic label and physical URI.
	 * Sets System proxies to agent proxies before ontology instantiation
	 * @param label
	 * @param physicalURI
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public JasdlOntology createJasdlOntology(Atom label, URI physicalURI) throws OWLOntologyCreationException{
		// set proxies
		for(InnerProxy proxy : proxies){
			try {
				proxy.set();
			} catch (JasdlException e) {
				getLogger().warning("Error setting proxies. Reason: "+e);
			}
		}
		JasdlOntology ont = new JasdlOntology(this, label, physicalURI);
		labelToOntologyMap.put(label, ont);
		uriToOntologyMap.put(physicalURI, ont);
		return ont;
	}
	
	/**
	 * Instantiate a new Jasdl Ontology with a supplied physical URI, associating it with an anonymous unique label
	 * @param physicalURI
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public JasdlOntology createJasdlOntology(URI physicalURI) throws OWLOntologyCreationException{
		// create a guaranteed unique anonymous label
		String label;
		int i = 0;
		while(true){
			label = ANON_ONTOLOGY_LABEL_PREFIX+i;
			if(!knownOntology(label)){
				break;
			}
			i++;
		}
		return createJasdlOntology(label, physicalURI);		
	}
	
	/**
	 * Instantiate a new Jasdl Ontology with a supplied string label and physical URI
	 * @param label
	 * @param physicalURI
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public JasdlOntology createJasdlOntology(String label, URI physicalURI) throws OWLOntologyCreationException{
		return createJasdlOntology(new Atom(label), physicalURI);		
	}
	
	
	
	public Collection<JasdlOntology> getLoadedOntologies(){
		return labelToOntologyMap.values();
	}	
	
	public OWLOntologyManager getManager(){
		return manager;
	}	
	
	public String getAgentName(){
		String name = getTS().getUserAgArch().getAgName();
		// hack, unable to set name in bb testing
		if(name.length() == 0){
			name = "undefined";
		}
		return name;
	}
	
	
	public List<Literal> getABoxState() throws JasdlException{
		List<Literal> bels = new Vector<Literal>();
		for(JasdlOntology ont : getLoadedOntologies()){
			bels.addAll(ont.getABoxState());
		}
		return bels;
	}
	
	public InnerProxy addProxy(String protocol, String ip, int portno){
		return addProxy( new InnerProxy(protocol, ip, portno) );
	}
	
	public InnerProxy addProxy(InnerProxy proxy){
		proxies.add(proxy);
		return proxy;
	}
	
	private class InnerProxy{
		private String protocol;
		private String ip;
		private int portno;
		
		InnerProxy(String protocol, String ip, int portno){
			this.protocol = protocol;
			this.ip = ip;
			this.portno = portno;
		}
		
		public void set() throws JasdlException{
			String cleanProtocol = protocol.replace(".", "");
			String name = ip+":"+portno;
			
			System.setProperty(protocol+"proxyHost", ip);
			System.setProperty(protocol+"proxyPort", ""+portno);
			
			try {
				Socket sock = SocketFactory.getDefault().createSocket(ip, portno);
			} catch (IOException e) {
				throw new JasdlException("Unable to connect to "+cleanProtocol+" proxy "+name+". Reason: "+e);
			}
			
			getLogger().info("Using "+cleanProtocol+" proxy "+name);
		}
	}

}
