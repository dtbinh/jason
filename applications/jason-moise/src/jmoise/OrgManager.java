package jmoise;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.Pred;
import jason.runtime.Settings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import moise.oe.GoalInstance;
import moise.oe.Group;
import moise.oe.MissionPlayer;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.Player;
import moise.oe.RolePlayer;
import moise.oe.SCH;
import moise.tools.SimOE;

import java.util.logging.*;

public class OrgManager extends AgArch {

		
	OE currentOE = null; // current org. entity
	SimOE simOE = null; // moise interface frame
	
	Logger logger = Logger.getLogger(OrgManager.class.getName());

	public void initAg(String agClass, String asSrc, Settings stts) throws JasonException {
		super.initAg(agClass, asSrc, stts);
		String osFile = fTS.getSettings().getUserParameter("osfile");
		if (osFile.startsWith("\"")) {
			osFile = osFile.substring(1,osFile.length()-1);
		}
		logger.fine("OS file is "+osFile);
		if (osFile == null) {
			logger.log(Level.SEVERE, "No osfile was informed for this OrgManager!");
			return;
		}
		try {
            currentOE = OE.createOE("noprop", osFile);
            logger.fine("Creation of OE from "+osFile+" is Ok.");
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Error creating current OE.",e);
        }

        // starts GUI
        if (fTS.getSettings().getUserParameter("gui") != null && fTS.getSettings().getUserParameter("gui").equals("yes")) {
            try {
                simOE = new SimOE(currentOE);
                simOE.setName("OrgManager");
                simOE.frame.getContentPane().remove(simOE.frame.OESimTabPanel);
                simOE.frame.addWindowListener(new java.awt.event.WindowAdapter() {
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            if (simOE != null) {
                                simOE.disposeWindow();
                            }
                            simOE = null;
                        }
                    });
                simOE.frame.centerScreen();
            } catch (Exception e) {
            	logger.log(Level.SEVERE, "Error creating OrgManager GUI!",e);
            }
        	
        }
	}

	public void stopAg() {
		if (simOE != null) {
			simOE.disposeWindow();
		}
		super.stopAg();
	}
	
	private void updateGUI() {
		if (simOE != null) {
            simOE.updateScreen();
        }		
	}

	public void checkMail() {
		super.checkMail(); // get the messages
		// check the MailBox (at TS) for org messages
		Iterator i = fTS.getC().getMB().iterator();
		while (i.hasNext()) {
			Message m = (Message)i.next();
			String content = m.getPropCont().toString();
			//if (content.startsWith("oe_")) {
				i.remove(); // the agent do not receive this message
		    	OEAgent agSender = currentOE.getAgent(m.getSender());
		    	try {
			    	if (agSender != null) {
			    		processMoiseMessage(Pred.parsePred(content), agSender, m.getMsgId(), m.getIlForce().equals("ask"));
			    	} else if (content.equals("addAgent")){
						currentOE.addAgent(m.getSender());
		            	updateMembersOE(currentOE.getAgent(m.getSender()), (Pred)null, true, true);
			    	} else {	    		
			    		logger.log(Level.SEVERE, "The message "+m+" has a sender not registered in orgManger!");
			    	}
		    	} catch (Exception e) {
		    		logger.log(Level.SEVERE, "Error!",e);
		    	}
			//}
		}
	}
	
	/**
	 * see JMoise+ doc for messages parameters and generated events.
	 */
	private void processMoiseMessage(Pred m, OEAgent sender, String mId, boolean needsReply) {
        try {
        	logger.fine("Processing '"+m+"' for "+sender);
		
    		if (m.getFunctor().equals("getOE")) {
	        	sendReply(sender, mId, currentOE.partialOE(sender));
	        	
	        // -----------------------------------	
            // Agent -----------------------------
	        // -----------------------------------
	        	
    		} else if (m.getFunctor().equals("adoptRole")) {
            	String roleId = m.getTerm(0).toString();
            	String grId = m.getTerm(1).toString();
            	sender.adoptRole(roleId,grId);
            	Group gr = currentOE.findGroup(grId);
            	
            	if (needsReply)	sendReply(sender, mId, "ok");

            	// send schemes of this group to sender
            	Iterator ig = gr.getRespSCHs().iterator();
            	while (ig.hasNext()) {
            		SCH sch = (SCH)ig.next();
            		updateMembersOE(sender,"schemeGroup("+sch.getId()+","+grId+")",false, true);
            	}
            	
            	// send players of this group to sender
            	Iterator irp = gr.getPlayers().iterator();
            	while (irp.hasNext()) {
            		RolePlayer rp = (RolePlayer)irp.next();
            		if (!rp.getPlayer().getId().equals(sender)) {
            			updateMembersOE(sender,"play("+rp.getPlayer().getId()+","+rp.getRole().getId()+","+grId+")",false, true);
            		}
            	}
            	
            	// send it the players of this group
        		updateMembersOE(gr.getAgents(),"play("+sender+","+roleId+","+grId+")",false, true);            		

    		} else if (m.getFunctor().equals("commitToMission")) {
            	String misId = m.getTerm(0).toString();
            	String schId = m.getTerm(1).toString();

    			sender.commitToMission(misId, schId);
            	if (needsReply) sendReply(sender, mId, "ok");

            	SCH sch = currentOE.findSCH(schId);

            	// notify to sender the current commitments
            	Iterator imp = sch.getPlayers().iterator();
            	while (imp.hasNext()) {
            		MissionPlayer mp = (MissionPlayer)imp.next();
                	updateMembersOE(sch.getPlayers(),"commitment("+mp.getPlayer().getId()+","+mp.getMission().getId()+","+sch.getId()+")", false, true);            		
            	}
            	
            	// notify the to the scheme players new player 
            	updateMembersOE(sch.getPlayers(),"commitment("+sender+","+misId+","+sch.getId()+")", false, true);

            	// send a message to generate new goals
        		updateMembersOE(sender,"updateGoals", true, true); 	

        		if (sch.getPlayersQty() > 1) {
        			updateMembersOE(sch.getOwner(),"schPlayers("+sch.getId()+",NP)", false, false);
        		}
            	updateMembersOE(sch.getOwner(),"schPlayers("+sch.getId()+","+sch.getPlayersQty()+")", false, true);
            
    		} else if (m.getFunctor().equals("removeMission")) {
    			boolean all = m.getTermsSize() == 1;
    			String misId = null;
            	String schId;
            	SCH sch;
    			if (all) {
    				schId = m.getTerm(0).toString();
                	sch = currentOE.findSCH(schId);
        			Iterator mpi = sender.getMissions().iterator();
            		while (mpi.hasNext()) {
            			MissionPlayer mp = (MissionPlayer)mpi.next();
                    	sender.removeMission(mp.getMission().getId(), schId);
                    	mpi = sender.getMissions().iterator();
                    	Pred evUnCom = Pred.parsePred("commitment("+sender+","+mp.getMission().getId()+","+sch.getId()+")");
                    	updateMembersOE(sch.getPlayers(), evUnCom, false, false);
                    	if (!sch.isPlayer(sender)) {
                        	updateMembersOE(sender, evUnCom, false, false);
                    	}
            		}
    			} else {
    				misId = m.getTerm(0).toString();
    				schId = m.getTerm(1).toString();
                	sch = currentOE.findSCH(schId);
                	sender.removeMission(misId, schId);
                	Pred evUnCom = Pred.parsePred("commitment("+sender+","+misId+","+sch.getId()+")");
                	updateMembersOE(sender, evUnCom, false, false);
                	if (!sch.isPlayer(sender)) {
                		updateMembersOE(sch.getPlayers(), evUnCom, false, false);
                	}
    			}
            	if (needsReply) sendReply(sender, mId, "ok");
            	
            	updateMembersOE(sender, (Pred)null, true, true);
            	
            	// notify owner that it can finish the scheme
            	updateMembersOE(sch.getOwner(),"schPlayers("+sch.getId()+",NP)", false, false);
            	updateMembersOE(sch.getOwner(),"schPlayers("+sch.getId()+","+sch.getPlayersQty()+")", false, true);
        		
            // -----------------------------------	
            // Scheme  ---------------------------  
            // -----------------------------------	

    		} else if (m.getFunctor().equals("startScheme")) {
            	String schSpecId = m.getTerm(0).toString();
            	SCH sch = currentOE.startSCH(schSpecId.toString());
            	sch.setOwner(sender);
            	if (needsReply) sendReply(sender, mId, sch.getId());
    			updateMembersOE(currentOE.getAgents(),"scheme("+schSpecId+","+sch.getId()+")[owner("+sender+")]",false, true);
            
    		} else if (m.getFunctor().equals("addResponsibleGroup")) {
            	String schId = m.getTerm(0).toString();
            	String grId = m.getTerm(1).toString();
                SCH sch = currentOE.findSCH(schId);
                if (sch == null) {
                	sendReply(sender, mId, "error(\"the scheme " + schId +" does not exist\")");
                	return;
                }

                if (!sender.equals(sch.getOwner())) {
                	sendReply(sender, mId, "error(\"you are not the owner of the scheme " + schId +", so you can not change it\")");
                }

                Group gr = currentOE.findGroup(grId);
                sch.addResponsibleGroup(gr);

                if (needsReply) sendReply(sender, mId, "ok");
    			updateMembersOE(gr.getPlayers(),"schemeGroup("+schId+","+grId+")", true, true);
    			
    		} else if (m.getFunctor().equals("setGoalState")) {
            	String schId = m.getTerm(0).toString();
            	String goalId = m.getTerm(1).toString();
            	String state = m.getTerm(2).toString();
                SCH sch = currentOE.findSCH(schId);
                if (sch == null) {
                	sendReply(sender, mId, "error(\"the scheme " + schId +" does not exist\")");
                	return;
                }

                GoalInstance gi = sch.getGoal(goalId);
                if (gi == null) {
                	sendReply(sender, mId, "error(\"the goal " + goalId +" does not exist in scheme "+schId+"\")");
                	return;
                }

                if (!gi.getCommittedAgents().contains(sender)) {
                	sendReply(sender, mId, "error(\"You are not committed to the goal " + goalId +", so you can not change its state.\")");
                	return;
                }
                
                if (state.equals("satisfied")) {
                	gi.setSatisfied(sender);
                } else if (state.equals("impossible")) {
                	gi.setImpossible(sender);
                }
                if (needsReply) sendReply(sender, mId, "ok");

                String gState = "unsatisfied";
                if (gi.isSatisfied()) {
                	gState = "satisfied";
                } else if (gi.isImpossible()) {
                	gState = "impossible";
                }
                updateMembersOE(sch.getPlayers(), "goalState("+gi.getSCH().getId()+","+gi.getSpec().getId()+","+gState+")", true, true);
                
            
    		} else if (m.getFunctor().equals("finishScheme")) {
            	String schId = m.getTerm(0).toString();
                SCH sch = currentOE.findSCH(schId);
                if (sch == null) {
                	sendReply(sender, mId, "error(\"the scheme " + schId +" does not exist\")");
                	return;
                }
                if (!sender.equals(sch.getOwner())) {
                	sendReply(sender, mId, "error(\"you are not the owner of the scheme " + schId +", so you can not change it\")");
                }
                
                currentOE.finishSCH(sch);
                if (needsReply) sendReply(sender, mId, "ok");
                
                // send untell to agents
                updateMembersOE(currentOE.getAgents(),"scheme("+sch.getSpec().getId()+","+sch.getId()+")[owner("+sch.getOwner()+")]",false,false);
    		
                
            // -----------------------------------	
            // Group -----------------------------
            // -----------------------------------
                
    		} else if (m.getFunctor().equals("createGroup")) {
    			boolean isNewRoot = m.getTermsSize() == 1;
    			Group newGr;
    			String annot = "root";
    			if (isNewRoot) {
    				 newGr = currentOE.addGroup(m.getTerm(0).toString());
    			} else {
        			String superGrId = m.getTerm(0).toString();
        			String specId = m.getTerm(1).toString();
        			Group superGr = currentOE.findGroup(superGrId);
        			if (superGr == null) {
                    	sendReply(sender, mId, "error(\"the group " + superGrId +" does not exist\")");
                    	return;
        			}
        			newGr = superGr.addSubGroup(specId);
                	annot = "superGr("+superGr.getId()+")";
    			}
            	newGr.setOwner(sender);
            	
            	if (needsReply) sendReply(sender, mId, newGr.getId());
    			
    			updateMembersOE(currentOE.getAgents(),"group("+m.getTerm(0).toString()+","+newGr.getId()+")[owner("+sender+"),"+annot+"]",false, true);
            	

    		} else if (m.getFunctor().equals("removeGroup")) {
    			String grId = m.getTerm(0).toString();
    			Group gr = currentOE.findGroup(grId);
    			if (gr == null) {
                	sendReply(sender, mId, "error(\"the group " + grId +" does not exist\")");
                	return;
    			}
    			if (!sender.equals(gr.getOwner())) {
                	sendReply(sender, mId, "error(\"you are not the owner of the group " + grId +", so you can not remove it\")");
    			}
    			
    			currentOE.removeGroup(grId);
    			if (needsReply) sendReply(sender, mId, "ok");

    			String annot = "root";
    			if (gr.getGrSpec().getFatherGroup() != null) {
    				// TODO: criar is root no moise+
    				// TODO: chamar the SuperGroup e nao father
    				annot = "superGr("+gr.getSuperGroup().getId()+")";
    			}
    			updateMembersOE(currentOE.getAgents(),"group("+gr.getGrSpec().getId()+","+gr.getId()+")[owner("+gr.getOwner()+"),"+annot+"]",false, false);

    		} else {
    			logger.log(Level.SEVERE, "Received an unknow message: "+m+"!");
    		}
    		
    		updateGUI();
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Error processing '"+m+"' for "+sender, e);
        	sendReply(sender, mId, "error(\"" + e+"\")");
        }
	}
	
	void sendReply(OEAgent to, String mId, String content) {
		try {
			Message r = new Message("tell", null, to.getId(), content);
			r.setInReplyTo(mId);
			super.sendMsg(r);		
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error sending reply '"+content+"' to "+to, e);
		}
	}
	void sendReply(OEAgent to, String mId, Object content) {
		try {
			Message r = new Message("tell", null, to.getId(), null);
			r.setInReplyTo(mId);
			r.setPropCont(content);
			super.sendMsg(r);		
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error sending reply '"+content+"' to "+to, e);
		}
	}
	
    void updateMembersOE(Collection ags, String event, boolean sendOE, boolean tell) {
    	updateMembersOE(ags, Pred.parsePred(event), sendOE, tell);
    }

    void updateMembersOE(OEAgent ag, String event, boolean sendOE, boolean tell) {
    	updateMembersOE(ag, Pred.parsePred(event), sendOE, tell);
    }

    private void updateMembersOE(Collection ags, Pred pEnv, boolean sendOE, boolean tell) {
    	Set all = new HashSet(); // to remove duplicates
    	Iterator iAgs = ags.iterator();
    	while (iAgs.hasNext()) {
    		Object next = iAgs.next();
    		OEAgent ag = null;

    		try {
    			// check if it is a list of OEAgents
    			ag = (OEAgent) next;
    		} catch (ClassCastException e) {
    			try {
    				// check if it is a list of Players
    				ag = ((Player) next).getPlayer();
    			} catch (ClassCastException e2) {
    			}
    		}

    		if (ag != null) {
    			all.add(ag);
            }
        }
    	Iterator i = all.iterator();
    	while (i.hasNext()) {
    		OEAgent ag = (OEAgent)i.next();
    		updateMembersOE(ag, pEnv, sendOE, tell);
    	}

    }

    private void updateMembersOE(OEAgent ag, Pred pEnv, boolean sendOE, boolean tell) {
        if (!ag.getAgentId().equals("orgManager")) {
        	try {
        		if (sendOE) {
            		Message moe = new Message("tell", null, ag.getId(), null);
            		moe.setPropCont(currentOE.partialOE(ag));
            		sendMsg(moe);
        		}
        		if (pEnv != null) {
        			String perf = "tell";
        			if (!tell) perf = "untell";
        			sendMsg(new Message(perf, null, ag.getId(), pEnv.toString()));
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, "Error sending update to "+ag+" ("+pEnv+").",e);
        	}
        }
    }


}
