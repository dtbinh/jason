//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jIDE;



public class MAS2JEditorPane extends ASEditorPane {
    
    public MAS2JEditorPane(JasonID mainID) {
        super(mainID,0);
        extension   = "mas2j";
    }
    
	void createSyntaxHighlightThread() {
		syntaxThread = new MAS2JSyntaxHighLight(editor);
		syntaxThread.start();
	}
    
	String getDefaultText(String s) {
		return getDefaultText(s, "ag1;");
	}
	
	String getDefaultText(String s, String ags) {
		if (s.length() == 0) {
			s = "<replace with project name>";
		}
		return "MAS " + s + " {\n"
				+ "   architecture: Centralised\n\n"
				+ "   //environment: <replace with the environment class name>\n\n"
				+ "   agents:\n"
				+ "       "+ags+"\n" 
				+ "       //<add more agents's name here>\n\n" 
				+ "}";
	}
}