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
package jasdl.bridge.xsd;


import java.util.HashMap;

/**
 * Maps OWL-API datatype labels to enumeration elements
 * This functionality should really be part of OWL-API!
 * TODO: this is by no means yet exhaustive
 * @author Tom Klapiscak
 *
 */
public enum XSDDataType {
	XSD_STRING("string"),
	XSD_DATE("date"),
	XSD_DATETIME("dateTime"),
	XSD_TIME("time"),
	XSD_BOOLEAN("boolean"),
	XSD_FLOAT("float"),
	XSD_INT("int"),
	XSD_DOUBLE("double");
	
	private String name;
	
	
	private XSDDataType(String name){
		this.name = name;
		XSDDataTypeUtils.put(name, this);
	}
	
	public String toString(){
		return name;
	}
	

	
	
}
