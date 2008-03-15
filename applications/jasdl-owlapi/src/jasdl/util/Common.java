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
package jasdl.util;


public class Common {

	public static String DELIM=",";
	public static int DOMAIN = 0;
	public static int RANGE = 1;
	/**
	 * Prefix to use when creating unique ontology labels
	 */
	public static String ANON_ONTOLOGY_LABEL_PREFIX = "_ontology_";
	
	public static boolean surroundedBy(String text, String match){
		return text.startsWith(match) && text.endsWith(match);
	}
	
	public static String strip(String text, String remove){
		if(text == null){ return null; }
		if(surroundedBy(text, remove)){
			return text.substring(remove.length(), text.length() - remove.length());
		}else{
			return text;
		}
	}
}
