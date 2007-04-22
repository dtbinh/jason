package jmoise;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.Pred;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import moise.common.MoiseException;
import moise.oe.GoalInstance;
import moise.oe.GroupInstance;
import moise.oe.MissionPlayer;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.Player;
import moise.oe.RolePlayer;
import moise.oe.SchemeInstance;
import moise.tools.SimOE;

/**
  * Organisational Manager, special agent that maintains the
  * organisation entity state.
  */
public class OrgManager extends AgArch {

    OE     currentOE = null;                                        
    SimOE  simOE     = null;                                      
    
    Logger logger    = Logger.getLogger(OrgManager.class.getName());

    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        String osFile = getTS().getSettings().getUserParameter("osfile");
        if (osFile.startsWith("\"")) {
            osFile = osFile.substring(1, osFile.length() - 1);
        }
        logger.fine("OS file is " + osFile);
        if (osFile == null) {
            logger.log(Level.SEVERE, "No osfile was informed for this OrgManager!");
            return;
        }
        try {
            currentOE = OE.createOE("noprop", osFile);
            logger.fine("Creation of OE from " + osFile + " is Ok.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating current OE.", e);
        }

        // starts GUI
        if (getTS().getSettings().getUserParameter("gui") != null && getTS().getSettings().getUserParameter("gui").equals("yes")) {
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
                logger.log(Level.SEVERE, "Error creating OrgManager GUI!", e);
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
        Iterator i = getTS().getC().getMailBox().iterator();
        while (i.hasNext()) {
            Message m = (Message) i.next();
            String content = m.getPropCont().toString();
            // if (content.startsWith("oe_")) {
            i.remove(); // the agent do not receive this message
            OEAgent agSender = currentOE.getAgent(m.getSender());
            try {
                if (agSender != null) {
                    processMoiseMessage(Pred.parsePred(content), agSender, m.getMsgId());
                } else if (content.equals("add_agent")) {
                    currentOE.addAgent(m.getSender());
                    updateMembersOE(currentOE.getAgent(m.getSender()), (String) null, true, true);
                } else {
                    logger.log(Level.SEVERE, "The message " + m + " has a sender not registered in orgManger!");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error!", e);
            }
            // }
        }
    }

    /**
     * see JMoise+ doc for messages parameters and generated events.
     */
    private void processMoiseMessage(Pred m, OEAgent sender, String mId) {
        try {
            if (logger.isLoggable(Level.FINE)) logger.fine("Processing '" + m + "' for " + sender);

            if (m.getFunctor().equals("getOE")) {
                sendReply(sender, mId, currentOE.partialOE(sender));

            // -----------------------------------
            // Agent -----------------------------
            // -----------------------------------

            } else if (m.getFunctor().equals("adopt_role")) {
            	adopt_role(sender, m.getTerm(0).toString(), m.getTerm(1).toString());

            } else if (m.getFunctor().equals("remove_role")) {
            	remove_role(sender,m.getTerm(0).toString(),m.getTerm(1).toString());
            	
            } else if (m.getFunctor().equals("commit_mission")) {
            	commit_mission(sender, m.getTerm(0).toString(), m.getTerm(1).toString());

            } else if (m.getFunctor().equals("remove_mission")) {
                String misId = null;
                String schId = null;
                boolean all = m.getTermsSize() == 1;
                if (all) {
                    schId = m.getTerm(0).toString();
                } else {
                    misId = m.getTerm(0).toString();
                    schId = m.getTerm(1).toString();
                }
                remove_mission(sender, all, schId, misId);

            // -----------------------------------
            // Scheme ---------------------------
            // -----------------------------------

            } else if (m.getFunctor().equals("start_scheme")) {
                String schSpecId = m.getTerm(0).toString();
                SchemeInstance sch = currentOE.startScheme(schSpecId.toString());
                sch.setOwner(sender);
                updateMembersOE(currentOE.getAgents(), "scheme(" + schSpecId + "," + sch.getId() + ")[owner(" + sender + ")]", false, true);

            } else if (m.getFunctor().equals("add_responsible_group")) {
                String schId = m.getTerm(0).toString();
                String grId = m.getTerm(1).toString();
                SchemeInstance sch = currentOE.findScheme(schId);
                if (sch == null) {
                    sendReply(sender, mId, "error(\"the scheme " + schId + " does not exist\")");
                    return;
                }

                if (!sender.equals(sch.getOwner())) {
                    sendReply(sender, mId, "error(\"you are not the owner of the scheme " + schId + ", so you can not change it\")");
                }

                GroupInstance gr = currentOE.findGroup(grId);
                sch.addResponsibleGroup(gr);

                updateMembersOE(gr.getPlayers(), "scheme_group(" + schId + "," + grId + ")", true, true);

            } else if (m.getFunctor().equals("set_goal_state") || m.getFunctor().equals("set_goal_arg")) {
                boolean isSetState = m.getFunctor().equals("set_goal_state");
                String schId = m.getTerm(0).toString();
                String goalId = m.getTerm(1).toString();
                String state = m.getTerm(2).toString();

                String arg = null;
                String value = null;
                if (!isSetState) {
                    arg = m.getTerm(2).toString();
                    if (arg.startsWith("\"")) {
                        arg = arg.substring(1, arg.length() - 1);
                    }
                    value = m.getTerm(3).toString();
                }

                SchemeInstance sch = currentOE.findScheme(schId);
                if (sch == null) {
                    sendReply(sender, mId, "error(\"the scheme " + schId + " does not exist\")");
                    return;
                }

                GoalInstance gi = sch.getGoal(goalId);
                if (gi == null) {
                    sendReply(sender, mId, "error(\"the goal " + goalId + " does not exist in scheme " + schId + "\")");
                    return;
                }

                if (!gi.getCommittedAgents().contains(sender)) {
                    sendReply(sender, mId, "error(\"You are not committed to the goal " + goalId + ", so you can not change its state.\")");
                    return;
                }

                if (isSetState) {
                    if (state.equals("satisfied")) {
                        gi.setSatisfied(sender);
                    } else if (state.equals("impossible")) {
                        gi.setImpossible(sender);
                    }
                } else {
                    gi.setArgumentValue(arg, value);
                }

                updateMembersOE(sch.getPlayers(), "goal_state(" + gi.getScheme().getId() + "," + gi.getSpec().getId() + ")", true, true);

               
            } else if (m.getFunctor().equals("finish_scheme")) {
                String schId = m.getTerm(0).toString();
                SchemeInstance sch = currentOE.findScheme(schId);
                if (sch == null) {
                    sendReply(sender, mId, "error(\"the scheme " + schId + " does not exist\")");
                    return;
                }
                if (!sender.equals(sch.getOwner())) {
                    sendReply(sender, mId, "error(\"you are not the owner of the scheme " + schId + ", so you can not change it\")");
                }

                currentOE.finishScheme(sch);

                // send untell to agents
                updateMembersOE(currentOE.getAgents(), "scheme(" + sch.getSpec().getId() + "," + sch.getId() + ")[owner(" + sch.getOwner() + ")]", false, false);

                // -----------------------------------
                // Group -----------------------------
                // -----------------------------------

            } else if (m.getFunctor().equals("create_group")) {
                boolean isNewRoot = m.getTermsSize() == 1;
                GroupInstance newGr;
                String annot = "root";
                if (isNewRoot) {
                    newGr = currentOE.addGroup(m.getTerm(0).toString());
                } else {
                    String superGrId = m.getTerm(0).toString();
                    String specId = m.getTerm(1).toString();
                    GroupInstance superGr = currentOE.findGroup(superGrId);
                    if (superGr == null) {
                        sendReply(sender, mId, "error(\"the group " + superGrId + " does not exist\")");
                        return;
                    }
                    newGr = superGr.addSubGroup(specId);
                    annot = "super_gr(" + superGr.getId() + ")";
                }
                newGr.setOwner(sender);

                updateMembersOE(currentOE.getAgents(), "group(" + m.getTerm(0).toString() + "," + newGr.getId() + ")[owner(" + sender + ")," + annot + "]", false, true);

            } else if (m.getFunctor().equals("remove_group")) {
                String grId = m.getTerm(0).toString();
                GroupInstance gr = currentOE.findGroup(grId);
                if (gr == null) {
                    sendReply(sender, mId, "error(\"the group " + grId + " does not exist\")");
                    return;
                }
                if (!sender.equals(gr.getOwner())) {
                    sendReply(sender, mId, "error(\"you are not the owner of the group " + grId + ", so you can not remove it\")");
                }

                currentOE.removeGroup(grId);

                String annot = "root";
                if (gr.getGrSpec().getFatherGroup() != null) {
                    // TODO: criar is root no moise+
                    // TODO: chamar the SuperGroup e nao father
                    annot = "super_gr(" + gr.getSuperGroup().getId() + ")";
                }
                updateMembersOE(currentOE.getAgents(), "group(" + gr.getGrSpec().getId() + "," + gr.getId() + ")[owner(" + gr.getOwner() + ")," + annot + "]", false, false);

            } else {
                logger.fine("Received an unknown message: " + m + "!");
            }

            updateGUI();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing '" + m + "' for " + sender, e);
            sendReply(sender, mId, "error(\"" + e + "\")");
        }
    }

    void adopt_role(OEAgent sender, String roleId, String grId) throws MoiseException {
        sender.adoptRole(roleId, grId);
        GroupInstance gr = currentOE.findGroup(grId);

        // send schemes of this group to sender
        for (SchemeInstance sch : gr.getRespSchemes()) {
            updateMembersOE(sender, "scheme_group(" + sch.getId() + "," + grId + ")", true, true);
        }

        // send players of this group to sender
        for (RolePlayer rp : gr.getPlayers()) {
            if (!rp.getPlayer().getId().equals(sender)) {
                updateMembersOE(sender, "play(" + rp.getPlayer().getId() + "," + rp.getRole().getId() + "," + grId + ")", false, true);
            }
        }

        // notify other in the group about this new player
        updateMembersOE(gr.getAgents(), "play(" + sender + "," + roleId + "," + grId + ")", false, true);
    }

    void remove_role(OEAgent sender, String roleId, String grId) throws MoiseException {
    	sender.removeRole(roleId, grId);
    	GroupInstance gr = currentOE.findGroup(grId);

    	// notify other players
    	updateMembersOE(gr.getAgents(), "play(" + sender + "," + roleId + "," + grId + ")", false, false);
    }

    void commit_mission(OEAgent sender, String misId, String schId) throws MoiseException {
        sender.commitToMission(misId, schId);

        SchemeInstance sch = currentOE.findScheme(schId);

        // notify to the sender the current commitments of the scheme
        for (MissionPlayer mp : sch.getPlayers()) {
            updateMembersOE(sch.getPlayers(), "commitment(" + mp.getPlayer().getId() + "," + mp.getMission().getId() + "," + sch.getId() + ")", false, true);
        }

        // notify to the scheme players the new player
        updateMembersOE(sch.getPlayers(), "commitment(" + sender + "," + misId + "," + sch.getId() + ")", false, true);

        // send a message to generate new goals
        updateMembersOE(sender, "update_goals", true, true);

        if (sch.getPlayersQty() > 1) {
            updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + ",NP)", false, false);
        }
        updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + "," + sch.getPlayersQty() + ")", false, true);
    }
    
    void remove_mission(OEAgent sender, boolean all, String schId, String misId) throws MoiseException {
        SchemeInstance sch;
        if (all) {
            sch = currentOE.findScheme(schId);
            Iterator mpi = sender.getMissions().iterator();
            while (mpi.hasNext()) {
                MissionPlayer mp = (MissionPlayer) mpi.next();
                sender.removeMission(mp.getMission().getId(), schId);
                mpi = sender.getMissions().iterator();
                String evUnCom = "commitment(" + sender + "," + mp.getMission().getId() + "," + sch.getId() + ")";
                updateMembersOE(sch.getPlayers(), evUnCom, false, false);
                if (!sch.isPlayer(sender)) {
                    updateMembersOE(sender, evUnCom, false, false);
                }
            }
        } else {
            sch = currentOE.findScheme(schId);
            sender.removeMission(misId, schId);
            String evUnCom = "commitment(" + sender + "," + misId + "," + sch.getId() + ")";
            updateMembersOE(sender, evUnCom, false, false);
            if (!sch.isPlayer(sender)) {
                updateMembersOE(sch.getPlayers(), evUnCom, false, false);
            }
        }

        updateMembersOE(sender, (String) null, true, true);

        // notify owner that it can finish the scheme
        updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + ",NP)", false, false);
        updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + "," + sch.getPlayersQty() + ")", false, true);

    }
    
    void sendReply(OEAgent to, String mId, String content) {
        try {
            Message r = new Message("tell", null, to.getId(), content);
            r.setInReplyTo(mId);
            super.sendMsg(r);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending reply '" + content + "' to " + to, e);
        }
    }

    void sendReply(OEAgent to, String mId, Object content) {
        try {
            Message r = new Message("tell", null, to.getId(), null);
            r.setInReplyTo(mId);
            r.setPropCont(content);
            super.sendMsg(r);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending reply '" + content + "' to " + to, e);
        }
    }

    private void updateMembersOE(Collection ags, String pEnv, boolean sendOE, boolean tell) {
        Set<OEAgent> all = new HashSet<OEAgent>(); // to remove duplicates
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
        for (OEAgent ag : all) {
            updateMembersOE(ag, pEnv, sendOE, tell);
        }
    }

    private void updateMembersOE(OEAgent ag, String pEnv, boolean sendOE, boolean tell) {
        if (!ag.getId().equals("orgManager")) {
            try {
                if (sendOE) {
                    Message moe = new Message("tell", null, ag.getId(), null);
                    moe.setPropCont(currentOE.partialOE(ag));
                    sendMsg(moe);
                }
                if (pEnv != null) {
                    String perf = "tell";
                    if (!tell)
                        perf = "untell";
                    sendMsg(new Message(perf, null, ag.getId(), pEnv));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error sending update to " + ag + " (" + pEnv + ").", e);
            }
        }
    }
}
