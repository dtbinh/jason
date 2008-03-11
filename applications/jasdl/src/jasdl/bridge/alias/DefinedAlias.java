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
package jasdl.bridge.alias;

import jason.asSyntax.Atom;

public class DefinedAlias extends Alias{
	
	private Atom origin;
	
	public DefinedAlias(String name, Atom origin){
		super(name);
		this.origin = origin;
	}
	
	public String toString(){
		return getName()+" (origin: "+origin.getFunctor()+")";
	}
	
	public int hashCode(){
		return (getName()+origin.getFunctor()).hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof DefinedAlias)){
			return false;
		}		
		return super.equals(other) && origin.equals(((DefinedAlias)other).getOrigin());
	}	
	
	public boolean defined(){
		return true;
	}
	
	public Atom getOrigin(){
		return origin;
	}
}
