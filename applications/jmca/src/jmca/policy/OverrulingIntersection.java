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
package jmca.policy;

import jason.runtime.Settings;

import java.util.List;
import java.util.Vector;

import jmca.module.AgentModule;
import jmca.util.JmcaException;

/**
 * <pre>
 * The default (and currently only) selection policy bundled with JMCA
 * Ultimately results in the intersection of the acceptable sets chosen by each AgentModule being returned to the JmcaAgent
 * If at any point in the composition chain, this intersection is empty (i.e. no agreement exists between this, and previous modules)
 * then this module's whole acceptable set is passed on to subsequent modules. In this way, later modules take precedence over earlier
 * ones under circumstances where no mutually-agreed upon decision can be made.
 * </pre>
 * @author Tom Klapiscak
 *
 * @param <T>	The type of aspect this instance of ContingencyCheck deals with
 */
public class OverrulingIntersection<T> implements SelectionPolicy<T>{
	public void init(Settings stts){
		// do nothing
	}
	
	@SuppressWarnings("unchecked")
	public List<T> apply(List<AgentModule<T>> modules, List<T> elements) throws JmcaException{
		List<T> intersection = new Vector<T>();
		intersection.addAll(elements);
		for(AgentModule module : modules){
			List<T> chosen = module.select(elements);		
			intersection.retainAll(chosen);
			if(intersection.isEmpty()){ // no agreement, override earlier selections
				intersection = chosen;
			}
		}
		return intersection;
	}
}
