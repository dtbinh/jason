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

import jason.asSyntax.Trigger;

import java.io.File;

public class Common {

	public static boolean surroundedBy(String text, String match) {
		return text.startsWith(match) && text.endsWith(match);
	}

	public static String strip(String text, String remove) {
		if (text == null) {
			return null;
		}
		if (surroundedBy(text, remove)) {
			return text.substring(remove.length(), text.length() - remove.length());
		} else {
			return text;
		}
	}

	/**
	 * TODO: Probably should be a part of jason's Trigger class?
	 * @param trigger
	 * @return
	 */
	public static Trigger.TEOperator getTEOp(Trigger trigger) {
		if (trigger.isAddition()) {
			return Trigger.TEOperator.add;
		} else {
			return Trigger.TEOperator.del;
		}
	}

	public static String getCurrentDir() {
		File dir1 = new File(".");
		String strCurrentDir = "";
		try {
			strCurrentDir = dir1.getCanonicalPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strCurrentDir;
	}

}
