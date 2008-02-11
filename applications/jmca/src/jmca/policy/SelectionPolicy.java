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

import jmca.module.AgentModule;
import jmca.util.JmcaException;

/**
 * Implementations of this are responsible for mediating the selection process between AgentModules
 *  
 * @author Tom Klapiscak
 *
 * @param <T> The type of aspect this selection policy instance deals with
 */
public interface SelectionPolicy<T> {
	
	public void init(Settings stts);
	public List<T> apply(List<AgentModule<T>> modules, List<T> elements) throws JmcaException;
}
