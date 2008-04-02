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
package jasdl.util.owlapi.xsd;


import java.util.HashMap;

public class XSDDataTypeUtils {
	private static HashMap<String, XSDDataType> map = new HashMap<String, XSDDataType>();
	
	public static boolean isStringType(XSDDataType typ){
		return (typ == XSDDataType.XSD_TIME || typ == XSDDataType.XSD_STRING || typ == XSDDataType.XSD_DATE || typ == XSDDataType.XSD_DATETIME);
	}
	
	public static boolean isBooleanType(XSDDataType typ){
		return typ==XSDDataType.XSD_BOOLEAN;
	}
	
	public static XSDDataType get(String name){
		return map.get(name);
	}
	
	public static void put(String name, XSDDataType xsd){
		map.put(name, xsd);
	}
}
