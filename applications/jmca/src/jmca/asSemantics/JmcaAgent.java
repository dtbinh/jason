/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JMCA.
 *
 *  JMCA is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JMCA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JMCA.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package jmca.asSemantics;

import static jmca.util.Common.DELIM;
import static jmca.util.Common.strip;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.bb.BeliefBase;
import jason.runtime.Settings;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import jmca.module.AgentModule;
import jmca.policy.SelectionPolicy;
import jmca.util.Common;
import jmca.util.JmcaException;


/**
 * <pre>
 * "Jason Module Composition Architecture"
 * 
 * Overrides Jason Agent selection functions to provide modular composition services.
 * An aspect is (currently) one of: Option, Message, Intention, Event, ActionExec.
 * Aspect types are identified by their Java class.
 * Each step in a composition chain is known as an "AgentModule".
 * AgentModules select an "agreed" set of aspects, mediated by the "SelectionPolicy".
 * Each aspect type has its own composition chain and selection policy.
 * 
 * This class is responsible for:
 *  - initialising and managing AgentModules and SelectionPolicies
 *  - overriding key Jason Agent methods to interface with JMCA
 * </pre>
 * @author Tom Klapiscak
 *
 */
public class JmcaAgent extends jason.asSemantics.Agent {
	
	private static String DEFAULT_SELECTION_POLICY_CLASS = "jmca.policy.OverrulingIntersection";	
	private static String PARAM_DELIM = "_";
	private static String PARAM_PREFIX = "jmca"+PARAM_DELIM;		
	
	/**
	 * Maps aspect type to the list of AgentModules instantiated for it
	 */
	private HashMap<Class, List<AgentModule>> aspectModulesMap;
	
	/**
	 * Maps aspect type to its selection policy
	 */
	private HashMap<Class, SelectionPolicy> aspectSelectionPolicyMap;
	
	/**
	 * A static list of aspect types (classes) this Jmca supports (current Option, Message, Intention, Event, ActionExec)
	 */
	private static Vector<Class> aspects = new Vector<Class>();
	static{
		aspects.addAll(Arrays.asList(new Class[] {Option.class, Message.class, Intention.class, Event.class, ActionExec.class}));
	}	
	
	
	/**
	 * Instantiates AgentModules and SelectionPolicies for this JmcaAgent, then calls Jason's default initAg method
	 */
	@SuppressWarnings("unchecked")
	public TransitionSystem initAg(AgArch arch, BeliefBase bb, java.lang.String asSrc, Settings stts) throws JasonException{
		aspectModulesMap = new HashMap<Class, List<AgentModule>>();
		aspectSelectionPolicyMap = new HashMap<Class, SelectionPolicy>();
		for(Class aspect : aspects){
			aspectModulesMap.put(aspect, getAgentModuleClasses(this, arch, bb, asSrc, stts, aspect));
			aspectSelectionPolicyMap.put(aspect, getSelectionPolicyClass(stts, aspect));
		}
		return super.initAg(arch, bb, asSrc, stts);		
	}
	

	/**
	 * Apply module composition to the Option aspect
	 * May be passed a null Option list if events=retrieve is set, if so, an empty list is instantiated to allow for generated Options
	 */
	public Option selectOption(List<Option> options){
		if(options == null){ // if events=retrieve, option list may be null
			options = new Vector<Option>();
		}
		getLogger().finest("Options: "+options);
		return (Option)apply(Option.class, options);
	}
	
	/**
	 * Apply module composition to the ActionExec aspect
	 */
	public ActionExec selectAction(List<ActionExec> actions){
		getLogger().finest("Actions: "+actions);
		return (ActionExec)apply(ActionExec.class, actions);
	}

	/**
	 * Apply module composition to the Message aspect
	 */
	public Message selectMessage(Queue<Message> messages){
		getLogger().finest("Messages: "+messages);
		return (Message)apply(Message.class, messages);		
	}
	
	/**
	 * Apply module composition to the Event aspect
	 */
	public Event selectEvent(Queue<Event> events){
		getLogger().finest("Events: "+events);
		return (Event)apply(Event.class, events);	
	}
	
	/**
	 * Apply module composition to the Intention aspect
	 */
	public Intention selectIntention(Queue<Intention> intentions){
		getLogger().finest("Intentions: "+intentions);
		return (Intention)apply(Intention.class, intentions);
	}
	
	/**
     * Applies selection policy for all AgentModules of this aspect and returns the first aspect from the "agreed" set
	 * 
	 * @param aspect		the aspect type we are applying selection/generation policies to
	 * @param aspects		a collection of aspects to apply selection/generation policies to - collection allows lists and queues to be passed
	 * @return				the first aspect present within the "agreed" set.
	 */
	@SuppressWarnings("unchecked")
	private Object apply(Class aspect, Collection aspects){
		SelectionPolicy selectionPolicy = aspectSelectionPolicyMap.get(aspect);
		List<AgentModule> modules = aspectModulesMap.get(aspect);	
		List asList = new Vector(); // auxilliary list required since aspects could be a queue or a list
		asList.addAll(aspects);
		try{
			asList = selectionPolicy.apply(modules, asList);
		}catch(JmcaException e){
			getLogger().severe("Error in jmca "+getUCN(aspect)+" selection. Reason: "+e);
			return null;
		}
		if(asList.isEmpty()){
			return null;
		}else{
			Object selected = asList.remove(0);
			aspects.remove(selected); // affect master queue
			return selected;
		}
	}

	/**
	 * Instantiates (using Java Reflect) the selection policy implementation class speficied in the JmcaAgent's .mas2j file.
	 * If none present, the default selection policy class is used.
	 * 
	 * @param stts				.mas2j settings of this JmcaAgent
	 * @param aspect			the type of aspect we are instantiating this selection policy for
	 * @return					the selection policy instance
	 * @throws JmcaException	if instantiation fails
	 */
	private static SelectionPolicy getSelectionPolicyClass(Settings stts, Class aspect) throws JmcaException{
		SelectionPolicy policy;
		String paramName = PARAM_PREFIX+getUCN(aspect)+PARAM_DELIM+"policy"+PARAM_DELIM+"class";
		String param = stts.getUserParameter(paramName);
		if(param == null){ // if none specified, use default
			param = DEFAULT_SELECTION_POLICY_CLASS;
		}else{
			param = strip(param, "\"");
		}
		try {
			Class cls = Class.forName(param);
			Constructor ct = cls.getConstructor(new Class[] {});
			policy = (SelectionPolicy)ct.newInstance();		
			policy.init(stts);
		}
		catch (Throwable e) {
			throw new JmcaException("Error instantiating option generation policy class. Reason: "+e);
		}			
		return policy;
	}
	
	/**
	 * Instantiates (using Java Reflect), based on .mas2j parameters, the AgentModule composition chain for a particular type of aspect
	 * 
	 * @param agent				the JmcaAgent these AgentModules are to be associated with (passed to module for later use)
	 * @param arch				the AgArch of the JmcaAgent (used for module initialisation)
	 * @param bb				the BeliefBase of the JmcaAgent (used for module initialisation)
	 * @param asSrc				the AgentSpeak source of the JmcaAgent (used for module initialisation)
	 * @param stts				the .mas2j settings of the JmcaAgent (used for module initialisation)
	 * @param aspect			the type of aspect for which we are instantiating this AgentModule composition chain
	 * @return					a list of AgentModules representing the composition chain for this type of aspect
	 * @throws JmcaException	if AgentModule instantiation fails
	 * @throws JasonException	if AgentModule initialisation fails
	 */
	private static List<AgentModule> getAgentModuleClasses(Agent agent, AgArch arch, BeliefBase bb, String asSrc, Settings stts, Class aspect) throws JmcaException, JasonException{		
		List<AgentModule> modules = new Vector<AgentModule>();
		String paramName = PARAM_PREFIX+getUCN(aspect)+PARAM_DELIM+"module"+PARAM_DELIM+"classes";
		String param = stts.getUserParameter(paramName);
		if(param != null){ // no AgentModules specific for this aspect
			for(String moduleClassName : Common.strip(param, "\"").split(DELIM)){
				moduleClassName = moduleClassName.trim();
				AgentModule module = null;
				// use java reflect to instantiate agent module class
				try {
					Class cls = Class.forName(moduleClassName);
					Constructor ct = cls.getConstructor(new Class[] {Agent.class});
					module = (AgentModule)ct.newInstance(new Object[] {agent});
				}
				catch (Throwable e) {
					throw new JmcaException("Error instantiating "+getUCN(aspect)+" module class "+moduleClassName+". Reason: "+e);
				}
				modules.add(module);
			}
		}
		return modules;
	}
	
	/**
	 * Gets the unqualified (bit after last .) class name of a class
	 * @param aspect	the class to get the unqualified name of
	 * @return			the unqualified name of the supplied class
	 */
	private static String getUCN(Class aspect){
		String name =  aspect.getName();
		if(name.contains(".")){
			name = name.substring(name.lastIndexOf(".")+1);
		}
		return name;
	}	
	
}