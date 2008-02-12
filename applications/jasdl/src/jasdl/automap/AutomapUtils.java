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
package jasdl.automap;

import static jasdl.util.Common.DELIM;
import jasdl.ontology.JasdlOntology;
import jasdl.util.JasdlException;

import java.lang.reflect.Constructor;

public class AutomapUtils {
	
	/**
	 * Accepts a DELIM delimited list of automap class identifier strings, instantiates using reflection and applies to the ontology
	 * Auxilliary to initAg
	 * @param uri			ontology to apply these mappings to
	 * @param automapList	DELIM delimited list of automap class identifier strings to be applied to the ontology given by uri
	 */
	public static void performAutomaps(JasdlOntology ont, String automapList) throws JasdlException{
		if(automapList != null){
			for(String automapName : automapList.split("["+DELIM+"]")){	
				automapName = automapName.trim();
				try {
					Class cls = Class.forName(automapName);
					Constructor ct = cls.getConstructor(new Class[] {});
					Automap automap = (Automap)ct.newInstance(new Object[] {});
					if(automap == null){
						throw new JasdlException("Unknown automap class: "+automapName);
					}else{
						automap.map(ont);
					}
				}catch (Throwable e) {
					throw new JasdlException("Error instantiating automap class "+automapName+". Reason: "+e);
				}				
			}
		}
	}
}
