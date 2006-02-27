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
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.5  2006/02/27 18:46:26  jomifred
//   creation of the RuntimeServices interface
//
//   Revision 1.4  2006/02/18 15:27:04  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
//   Revision 1.3  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.2  2006/01/04 03:00:47  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/08/18 13:32:45  jomifred
//   initial implementation (based on createAg)
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.logging.Logger;

public class killAgent implements InternalAction {

    private static Logger logger = Logger.getLogger(killAgent.class.getName());

	/* args[0] the agent name */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		try {
            Term name = (Term)args[0].clone();
            un.apply(name);
            
            return ts.getUserAgArch().getArchInfraTier().getRuntimeServices().killAgent(name.toString());
		} catch (IndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'killAgent' received a wrong number of arguments");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
