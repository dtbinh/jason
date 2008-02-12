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

public class DefinedAlias extends Alias {
	private String definedBy;
	
	/**
	 * The defining expression of this defined class
	 * Option - does not affect hashcode
	 * Stored for use when runtime-defined classes are referred to in messages
	 */
	private String expr;
	
	public DefinedAlias(String name, String definedBy, String expr){
		super(name);
		this.definedBy = definedBy;	
		this.expr = expr;
	}
	
	public DefinedAlias(String name, String definedBy){
		this(name, definedBy, "");
	}
	
	public boolean isBase(){
		return false;
	}
	
	public String getDefinedBy(){
		return definedBy;
	}
	
	public String getExpr(){
		return expr;
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof DefinedAlias)){
			return false;
		}
		return super.equals(other) && ((DefinedAlias)other).getDefinedBy().equals(this.getDefinedBy());
	}	
	
	public int hashCode(){
		return (name+definedBy).hashCode();
	}
	
	public String toString(){
		return definedBy+":"+name;
	}
}
