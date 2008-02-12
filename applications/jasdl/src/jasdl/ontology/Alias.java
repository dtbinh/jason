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

import java.net.URI;

/**
 * Associates a class alias with the name of agent that defined this class at runtime, or null
 * if base class (i.e. present in underlying ontology)
 * 
 * @author Tom Klapiscak
 *
 */
public class Alias {
	protected String name;
	
	/**
	 * Takes the local name of the supplied uri (i.e. the bit after the #)
	 * Used by entities with no alias->transposition
	 * @param real
	 */
	public Alias(URI real){
		this(real.toString().substring(real.toString().lastIndexOf("#")+1));
	}
	
	public Alias(String name) {
		this.name = name;
	}
	
	public boolean isBase(){
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object other){
		return ((Alias)other).getName().equals(this.getName());
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	public String toString(){
		return getName();
	}
	
}
