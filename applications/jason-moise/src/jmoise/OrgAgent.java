package jmoise;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Trigger;
import jason.runtime.Settings;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import moise.oe.GoalInstance;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.Permission;

import java.util.logging.*;

public class OrgAgent extends AgArch {

	OE currentOE = null;
	Set alreadyGeneratedEvents = new HashSet();
	
	Logger logger = Logger.getLogger(OrgAgent.class.getName());

	public void initAg(String agClass, String asSrc, Settings stts) throws JasonException {
		super.initAg(agClass, asSrc, stts);
		logger = Logger.getLogger(OrgAgent.class.getName()+"."+getAgName());
		try {
			Message m = new Message("tell", null, "orgManager", "addAgent");
			super.sendMsg(m);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error sending addAgent to OrgManager!",e);
		}
	}
	
	/*
	private static Term orgManagerTerm = new Term("orgManager");
	private static Term achievePerfTerm = new Term("achieve");
	
	public void act() {
    	ActionExec acExec = fTS.getC().getAction(); 
        if (acExec == null) {
            return;
        }
        Term acTerm = acExec.getActionTerm();
		// if an OE act, do it here
        if (acTerm.getFunctor().startsWith("oe_")) {
        	// send acTerm as message to OrgManager
        	try {
        		Term sendTerm = new Term(".send");
        		sendTerm.addTerm(orgManagerTerm);
        		sendTerm.addTerm(achievePerfTerm);
        		sendTerm.addTerm(acTerm);
        		if (logger.isDebugEnabled()) logger.debug("doing:"+acTerm);
        		fTS.getIA("jason.stdlib.send").execute(fTS, acExec.getIntention().peek().getUnif(), sendTerm.getTermsArray());
                acExec.setResult(true);
        	} catch (Exception e) {
        		logger.error("Error sending "+acTerm+" to OrgManager.",e);
                acExec.setResult(false);
        	}
        	fTS.getC().getFeedbackActions().add(acExec);
        } else {
        	super.act();
        }
	}
	*/

	public void checkMail() {
		super.checkMail(); // get the messages
		// check the MailBox (at TS) for org messages
		Iterator i = fTS.getC().getMB().iterator();
		while (i.hasNext()) {
			try {
				Message m = (Message)i.next();
				// check if content is and OE
				try {
					currentOE = (OE)m.getPropCont();
					i.remove();
				} catch (Exception e) {
					// the content is a normal predicate
					String content = m.getPropCont().toString();
					if (content.startsWith("schemeGroup")) { // this message is generated when my group becames responsible for a scheme
						generateObligationPermissionEvents(Pred.parsePred(content));
					} else if (content.startsWith("updateGoals")) { // I need to generate AS Trigger like !<orggoal>
						i.remove();
						generateOrgGoalEvents();
					} else if (content.startsWith("goalState")) { // the state of a scheme i belong has changed
						generateOrgGoalEvents();
					//} else if (m.getIlForce().equals("untell") && content.startsWith("schP")) {
					//	logger.info("** "+content);
						
					} else if (m.getIlForce().equals("untell") && content.startsWith("scheme")) {
						String schId = Pred.parsePred(content).getTerm(1).toString();
						removeAchieveEvents(schId);
						removeBeliefs(schId);
					}
				}
	    	} catch (Exception e) {
	    		logger.log(Level.SEVERE,"Error!",e);
	    	}
		}
	}

	void generateObligationPermissionEvents(Pred m) {
		// computes this agent obligations in the scheme
		String schId = m.getTerm(0).toString();
		String grId = m.getTerm(1).toString();
		Set obligations = new HashSet();
		
		Iterator iObl = getMyOEAgent().getObligations().iterator();
		while (iObl.hasNext()) {
			Permission p = (Permission)iObl.next();
			if (p.getRolePlayer().getGroup().getId().equals(grId) &&
				p.getScheme().getId().equals(schId)) {
					obligations.add(p);
					Literal l = Literal.parseLiteral("obligation("+p.getScheme().getId()+","+p.getMission().getId()+")["+
							"role("+p.getRolePlayer().getRole().getId()+"),group("+p.getRolePlayer().getGroup().getGrSpec().getId()+")]");
					fTS.getAg().addBel(l, null, fTS.getC(), Intention.EmptyInt);
					logger.fine("New obligation: "+l);
			}
		}

		Iterator iPer = getMyOEAgent().getPermissions().iterator();	
		while (iPer.hasNext()) {
			Permission p = (Permission)iPer.next();
			if (p.getRolePlayer().getGroup().getId().equals(grId) &&
				p.getScheme().getId().equals(schId) &&
				!obligations.contains(p)) {

				Literal l = Literal.parseLiteral("permission("+p.getScheme().getId()+","+p.getMission().getId()+")["+
						"role("+p.getRolePlayer().getRole().getId()+"),group("+p.getRolePlayer().getGroup().getGrSpec().getId()+")]");
				fTS.getAg().addBel(l, null, fTS.getC(), Intention.EmptyInt);
				logger.fine("New permission: "+l);
			}
		}
	}
	
	
	void generateOrgGoalEvents() {
		Iterator ig = getMyOEAgent().getPossibleAndPermittedGoals().iterator();
		while (ig.hasNext()) {
			GoalInstance gi = (GoalInstance)ig.next();
			if (!alreadyGeneratedEvents.contains(gi)) {
				alreadyGeneratedEvents.add(gi);
				
				
				Literal l = Literal.parseLiteral(gi.getAsProlog()+"["+
						"scheme("+gi.getSCH().getId()+")"+
						//"role(notimplemented),group(notimplemented)"+
						"]");
				// TODO: add annots: role, group (percorrer as missoes do ag que em GI, procurar os papel com obrigacao para essa missao)
				fTS.getAg().updateEvents(
						new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l), Intention.EmptyInt), 
						fTS.getC());
				logger.fine("New goal: "+l);
			}
		}
	}
	
	void removeAchieveEvents(String schId) {
		Iterator i = alreadyGeneratedEvents.iterator();
		while (i.hasNext()) {
			GoalInstance gi = (GoalInstance)i.next();
			if (gi.getSCH().getId().equals(schId)) {
				i.remove();
			}
		}
	}
	
	private static Literal obligationLiteral = Literal.parseLiteral("obligation(s,m)");
	private static Literal permissionLiteral = Literal.parseLiteral("permission(s,m)");
	private static Literal schemeGroupLiteral = Literal.parseLiteral("schemeGroup(s,g)");
	private static Literal goalStateLiteral = Literal.parseLiteral("goalState(s,g,state)");
	private static Literal schPlayersLiteral = Literal.parseLiteral("schPlayers(s,n)");
	
	void removeBeliefs(String schId) {
		fTS.getAg().getBS().removeAll(obligationLiteral);
		fTS.getAg().getBS().removeAll(permissionLiteral);
		fTS.getAg().getBS().removeAll(schemeGroupLiteral);
		fTS.getAg().getBS().removeAll(goalStateLiteral);
		fTS.getAg().getBS().removeAll(schPlayersLiteral);
	}
	
	OEAgent getMyOEAgent() {
		return currentOE.getAgent(getAgName());
	}
	
	/*
	void askUpdateOE() {
		try {
			Message m = new Message("askx", null, "orgManager", "getOE");
			super.sendMsg(m);
		} catch (Exception e) {
			logger.error("Error sending getOE to OrgManager!",e);
		}
	}
	*/
	
}
