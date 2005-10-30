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
//   Revision 1.1  2005/10/30 18:37:27  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//
//----------------------------------------------------------------------------

package jason.architecture;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;

import java.util.List;

/** 
 * Base agent architecture class that defines the overall agent
 * architecture; the AS interpreter is only the reasoner (a kind of mind) within this
 * architecture (a kind of body).
 * 
 * <p>The agent reasoning cycle (implemented in TransitionSystem class)
 * calls these methods to get perception, action, and communication.
 * 
 * <p>This class just calls the AgArchInterface methods
 * implemented by the infrastructure tier (Centralised, Saci, ...). 
 * However, the user can customise
 * this methods overridding some of them in his/her arch. class.
 */
public class AgArch implements AgArchInterface {

	protected TransitionSystem fTS = null;
	
	/** the class that implements the architecture  tier for the MAS infrastructure */
	AgArchInterface archTier;

	/**
	 * args[0] Arch class
	 * args[1] Agent class
	 */
    public void initAg(String[] args) throws JasonException {
        // set the agent
        try {
            String agClassName = null;
            if (args.length < 2) { // error
                throw new JasonException("The Agent class name was not informed for the CentralisedAgArch creation!");
            } else {
                agClassName = args[1].trim();
            }
            Agent ag = (Agent)Class.forName(agClassName).newInstance();
            fTS = ag.initAg(args, this);
        } catch (Exception e) {
            throw new JasonException("as2j: error creating the agent class! - "+e);
        }
    }
	
    /** stop the agent */
    public void stopAg() {
    }

    public void setInfraArch(AgArchInterface ai) {
		archTier = ai;
	}
    public AgArchInterface getInfraArch() {
    	return archTier;
    }
	
    public TransitionSystem getTS() {
    	return fTS;
    }


	
    /** gets the agent's perception as a list of Literals */
	public List perceive() {
		return archTier.perceive();
	}

    /** reads the agent's mailbox and adds messages into the agent's circumstance */
	public void checkMail() {
		archTier.checkMail();
	}

    /** executes the action in agent's circumstance (C.A) */
	public void act() {
		archTier.act();
	}

    /** gets the agent's name */
	public String getAgName() {
		return archTier.getAgName();
	}

    /** sends a Jason message in a specific infrastructure */
	public void sendMsg(Message m) throws Exception {
		archTier.sendMsg(m);
	}

    /** broadcasts a Jason message in a specific infrastructure */
	public void broadcast(Message m) throws Exception {
		archTier.broadcast(m);
	}

    /** checks whether the agent is running */
	public boolean isRunning() {
		return archTier.isRunning();
	}

	/** 
	 *  Inform the (centralised/saci) controller that this agent's cycle 
	 *  has finished its reasoning cycle (used in sync mode).
	 *  
	 *  <p><i>breakpoint</i> is true in case the agent selected one plan 
	 *  with the "breakpoint"  annotation.  
	 */ 
	public void informCycleFinished(boolean breakpoint) {
		archTier.informCycleFinished(breakpoint);
	}
}