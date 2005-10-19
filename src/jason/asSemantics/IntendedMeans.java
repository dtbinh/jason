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
//   Revision 1.5  2005/10/19 15:09:49  bordini
//   Fixed 2 bugs related to the plan failure mechanism:
//     - generated event, in case a plan failed by an action, was
//       not post as originally (lacked unification), see generateGoalDeletion,
//       rather than generateGoalDeletionFromEvent
//     - old (faild) plan which is kept in the intention wasn't removed
//       when the goal deletion plan finished (ClrInt)
//   To solve the first problem, the IntendedMeans class now has an extra
//   member variable called "Trigger" which record the original event.
//
//   Revision 1.4  2005/08/12 22:18:37  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.Plan;
import jason.asSyntax.Trigger; // To remember the triggering event that generated
                               // this intended means (e.g., for failure)

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IntendedMeans implements Serializable {

	protected Unifier unif = null;
	protected Plan plan;
	protected Trigger trigger;
    
    public IntendedMeans(Option opt) {
    	plan = (Plan)opt.plan.clone();
    	unif = (Unifier)opt.unif.clone();
    }

    public Plan getPlan() {
    	return plan;
    }
    
    public Unifier getUnif() {
    	return unif;
    }

    public Trigger getTrigger() {
    	return trigger;
    }
    public void setTrigger(Trigger tr) {
    	trigger = tr;
    }

	public boolean isAtomic() {
		if (plan != null) {
			return plan.isAtomic();
		} else {
			return false;
		}
	}
	
    public String toString() {
        return plan + " : " + unif;
    }

    /** get as XML */
	public Element getAsDOM(Document document) {
		Element eim = (Element) document.createElement("intended-means");
		if (plan != null) {
			eim.appendChild(plan.getAsDOM(document));
		}
		if (unif != null) {
			eim.appendChild(unif.getAsDOM(document));
		}
		return eim;
	}

}
