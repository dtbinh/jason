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
package jmca.module;

import jason.asSemantics.Agent;

import java.util.List;

import jmca.util.JmcaException;

/**
 * One step in a Jmca composition chain. Generalised to cope with any type of aspect
 * @author Tom Klapiscak
 *
 */
public abstract class AgentModule<T>{
	protected Agent master;
	
	
	public AgentModule(Agent master){
		this.master = master;
	}
	
	/**
	 * Choose, from those provided, a set of aspects that this AgentModule deems acceptable
	 * @param from				the aspects to choose from
	 * @return					a set of acceptable aspects
	 * @throws JmcaExceptio
	 */
	public abstract List<T> select(List<T> from) throws JmcaException;
	
	public String toString(){
		return master.toString();
	}
	
	
}
