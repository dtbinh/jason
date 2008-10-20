package jmoise;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import moise.oe.SchemeInstance;
import moise.tools.SimOE;

/**
  * Organisational Manager, special agent that maintains the
  * organisation entity (OE) state.
  */
public class OrgManager extends AgArch {

    private static Logger logger = Logger.getLogger(OrgManager.class.getName());

    private OE     currentOE = null;                                        
    private SimOE  simOE     = null;                                      
    private Map<String, OrgManagerCommand> commands = new HashMap<String, OrgManagerCommand>();
    
    
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
        
        initCommandsMap();
        
        // starts GUI        
        if ("yes".equals(getTS().getSettings().getUserParameter("gui"))) {
            try {
                simOE = new SimOE(currentOE, false);
                simOE.setName("OrgManager");
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
    
    protected void initCommandsMap() {
        addCommand(new GetOE());
        
        addCommand(new AdoptRole());
        addCommand(new RemoveRole());
        
        addCommand(new CreateGroup());
        addCommand(new RemoveGroup());

        addCommand(new CommitMission());
        addCommand(new RemoveMission());

        addCommand(new CreateScheme());
        addCommand(new AddResponsibleGroup());
        addCommand(new SetGoalState());
        addCommand(new SetGoalArg());
        addCommand(new RemoveScheme());
        addCommand(new AbortScheme());
        
        addCommand(new AddAgent());
    }
    
    public void addCommand(OrgManagerCommand c) {
        commands.put(c.getId(), c);        
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
        Iterator<Message> i = getTS().getC().getMailBox().iterator();
        while (i.hasNext()) {
            Message m = i.next();
            i.remove(); // the agent do not receive this message
            if (currentOE == null) break;
            if (m.getSender() == null) break;
            
            OEAgent agSender = currentOE.getAgent(m.getSender());
            if (logger.isLoggable(Level.FINE)) logger.fine("Processing '" + m + "' for " + agSender);
            try {
                if (agSender == null) {
                    agSender = currentOE.addAgent(m.getSender());
                    updateMembersOE(currentOE.getAgent(m.getSender()), (String) null, true, true);
                }
                
                // get content
                Pred content = null;
                if (m.getPropCont() instanceof Pred) {
                    content = (Pred)m.getPropCont();
                } else {
                    content = Pred.parsePred(m.getPropCont().toString());
                }
                
                // check whether there is a command
                OrgManagerCommand cmd = commands.get(content.getFunctor());
                if (cmd != null) {
                    cmd.process(currentOE, content, agSender, m.getMsgId());
                    updateGUI();
                } else {
                    logger.info("Received an unknown message: " + m + "!");
                }
            } catch (MoiseException e) {
                logger.log(Level.SEVERE, "Error processing '" + m + "' for " + agSender + ". "+e);
                sendReply(agSender, m.getMsgId(), "error(\"" + e + "\")");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing '" + m + "' for " + agSender, e);
                sendReply(agSender, m.getMsgId(), "error(\"" + e + "\")");
            }
        }
    }
    
    class GetOE implements OrgManagerCommand {
        public String getId() {
            return "getOE";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            sendReply(sender, mId, currentOE.partialOE(sender));
        }        
    }

    class AdoptRole implements OrgManagerCommand {
        public String getId() { 
            return "adopt_role"; 
        }
        
        public void process(OE currentOE, Pred m, OEAgent sender, String mId) throws MoiseException {
            String roleId = m.getTerm(0).toString();
            String grId   = m.getTerm(1).toString();

            sender.adoptRole(roleId, grId);
            GroupInstance gr = currentOE.findGroup(grId);

            // send new OE to the members of the group
            updateMembersOE(gr.getAgents(true), null, true, true);

            /*
            // notify others in the group about this new player
            updateMembersOE(gr.getAgents(true), rolePlayer2str(nrp), true, true);

            // send players of the group to the sender
            for (RolePlayer rp : gr.getPlayers(null, true)) {
                if (!rp.getPlayer().equals(sender)) {
                    updateMembersOE(sender, rolePlayer2str(rp), false, true);
                }
            }
            */

            // send schemes of this group to sender
            for (SchemeInstance sch : gr.getRespSchemes()) {
                updateMembersOE(sender, "scheme_group(" + sch.getId() + "," + grId + ")", false, true);
            }
            
            sendReply(sender, mId, "ok");
        }        
    }
    
    class RemoveRole implements OrgManagerCommand {
        public String getId() {
            return "remove_role";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String roleId = command.getTerm(0).toString();
            String grId   = command.getTerm(1).toString();
            
            sender.removeRole(roleId, grId);
            GroupInstance gr = currentOE.findGroup(grId);

            // notify other players
            updateMembersOE(gr.getAgents(true), null, true, false);
            // and the sender (that is not in the group anymore)
            updateMembersOE(sender, null, true, false);
            
            /*
            // notify other players
            updateMembersOE(gr.getAgents(true), rolePlayer2str(orp), false, false);
            
            // and the sender
            updateMembersOE(sender, "play(" + sender + "," + roleId + "," + grId + ")", true, false);
            */
            
            // if the agent is not member of the group anymore, remove other informations of the group
            if (!gr.getAgents(false).contains(sender)) {
                /*for (RolePlayer rp : gr.getPlayers()) {
                    updateMembersOE(sender, rolePlayer2str(rp), false, false);                
                }*/
                for (SchemeInstance sch : gr.getRespSchemes()) {
                    updateMembersOE(sender, "scheme_group(" + sch.getId() + "," + grId + ")", false, false);
                }
            }
            sendReply(sender, mId, "ok");
        }
    }
    
    class CommitMission implements OrgManagerCommand {
        public String getId() {
            return "commit_mission";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String misId = command.getTerm(0).toString();
            String schId = command.getTerm(1).toString();
            MissionPlayer nmp = sender.commitToMission(misId, schId);

            SchemeInstance sch = currentOE.findScheme(schId);

            // notify to the scheme players the new player
            updateMembersOE(sch.getPlayers(), missionPlayer2str(nmp), true, true);

            // notify to the sender the other commitments of the scheme
            for (MissionPlayer mp : sch.getPlayers()) {
                if (!mp.getPlayer().equals(sender)) {
                    updateMembersOE(sender, missionPlayer2str(mp), false, true);
                }
            }

            if (sch.getPlayersQty() > 1) {
                updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + ",NP)", false, false);
            }
            updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + "," + sch.getPlayersQty() + ")", false, true);
            sendReply(sender, mId, "ok");
        }
    }
    
    class RemoveMission implements OrgManagerCommand {
        public String getId() {
            return "remove_mission";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String misId = null;
            String schId = null;
            boolean all = command.getArity() == 1;
            if (all) {
                schId = command.getTerm(0).toString();
            } else {
                misId = command.getTerm(0).toString();
                schId = command.getTerm(1).toString();
            }
            SchemeInstance sch;
            if (all) {
                sch = currentOE.findScheme(schId);
                Iterator<MissionPlayer> mpi = sender.getMissions().iterator();
                while (mpi.hasNext()) {
                    MissionPlayer mp = mpi.next();
                    sender.removeMission(mp.getMission().getId(), schId);
                    mpi = sender.getMissions().iterator();
                    String evUnCom = missionPlayer2str(mp);
                    updateMembersOE(sch.getPlayers(), evUnCom, false, false);
                    //if (!sch.isPlayer(sender)) {
                        updateMembersOE(sender, evUnCom, false, false);
                    //}
                }
            } else {
                sch = currentOE.findScheme(schId);
                MissionPlayer omp = sender.removeMission(misId, schId);
                String evUnCom = missionPlayer2str(omp);
                updateMembersOE(sender, evUnCom, false, false);
                //if (!sch.isPlayer(sender)) {
                    updateMembersOE(sch.getPlayers(), evUnCom, false, false);
                //}
            }

            updateMembersOE(sender, (String) null, true, true);

            // notify owner that it can finish the scheme
            updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + ",NP)", false, false);
            updateMembersOE(sch.getOwner(), "sch_players(" + sch.getId() + "," + sch.getPlayersQty() + ")", false, true);
            sendReply(sender, mId, "ok");
        }
    }
    
    
    class CreateGroup implements OrgManagerCommand {
        public String getId() {
            return "create_group";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            boolean isNewRoot = command.getArity() == 1;
            GroupInstance newGr;
            String specId = command.getTerm(0).toString();
            if (isNewRoot) {
                newGr = currentOE.addGroup(specId);
            } else {
                String superGrId = command.getTerm(1).toString();
                GroupInstance superGr = currentOE.findGroup(superGrId);
                if (superGr == null) {
                    sendReply(sender, mId, "error(\"the group " + superGrId + " does not exist\")");
                    return;
                }
                newGr = superGr.addSubGroup(specId);
            }
            newGr.setOwner(sender);

            //updateMembersOE(currentOE.getAgents(), group2str(newGr), false, true);
            updateMembersOE(currentOE.getAgents(), null, true, true);
            sendReply(sender, mId, newGr.getId());
        }
    }
    
    
    class RemoveGroup implements OrgManagerCommand {
        public String getId() {
            return "remove_group";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String grId      = command.getTerm(0).toString();
            GroupInstance gr = currentOE.findGroup(grId);
            if (gr == null) {
                sendReply(sender, mId, "error(\"the group " + grId + " does not exist\")");
                return;
            }
            if (!sender.equals(gr.getOwner())) {
                sendReply(sender, mId, "error(\"you are not the owner of the group " + grId + ", so you can not remove it\")");
            }

            gr.checkRemove(); // TODO: does not work!

            // also send untell scheme_group (if it is the case)
            for (SchemeInstance sch: gr.getRespSchemes()) {
                updateMembersOE(gr.getPlayers(), "scheme_group(" + sch.getId() + "," + grId + ")", false, false);
            }
            
            /*
            // untell players
            for (RolePlayer rp: gr.getPlayers()) {
                updateMembersOE(gr.getAgents(true), rolePlayer2str(rp), false, false);
            }
            */

            // send changes for subgroups of gr
            for (GroupInstance sg: gr.getAllSubGroupsTree()) {
                //updateMembersOE(currentOE.getAgents(), group2str(sg), false, false);
                // also send untell scheme_group (if it is the case)
                for (SchemeInstance sch: sg.getRespSchemes()) {
                    updateMembersOE(sg.getPlayers(), "scheme_group(" + sch.getId() + "," + sg.getId() + ")", false, false);
                }
                // untell players
                /*
                for (RolePlayer rp: sg.getPlayers()) {
                    updateMembersOE(sg.getAgents(true), rolePlayer2str(rp), false, false);
                }
                */
            }
            
            // have to remove after sending the untells
            currentOE.removeGroup(grId);

            // sent a new copy of OE
            //updateMembersOE(currentOE.getAgents(), group2str(gr), true, false);
            updateMembersOE(currentOE.getAgents(), null, true, true);
            sendReply(sender, mId, "ok");
        }
    }

    class AddAgent implements OrgManagerCommand {
        public String getId() {
            return "add_agent";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            // send new OE to all
            updateMembersOE(currentOE.getAgents(), null, true, true);
            /*
            // send created groups
            for (GroupInstance gr: currentOE.getGroups()) {
                sendGr(sender, gr);
            }
            */
        }
        /*
        private void sendGr(OEAgent to, GroupInstance gr) {
            updateMembersOE(to, group2str(gr), false, true);
            for (GroupInstance sg: gr.getSubGroups())
                sendGr(to, sg);
        }
        */
    }

    class CreateScheme implements OrgManagerCommand {
        public String getId() {
            return "create_scheme";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String schSpecId = command.getTerm(0).toString();
            SchemeInstance sch = currentOE.startScheme(schSpecId.toString());
            sch.setOwner(sender);
            updateMembersOE(currentOE.getAgents(), scheme2str(sch), true, true);
            
            if (command.getArity() > 1) {
                // set the initial groups
                for (Term gr: (ListTerm)command.getTerm(1)) {
                    GroupInstance gi = currentOE.findGroup(gr.toString());
                    if (gi == null) {
                        sendReply(sender, mId, "error(\"the group " + gr + " does not exist\")");
                        return;
                    }
                    sch.addResponsibleGroup(gi);
                    updateMembersOE(gi.getPlayers(), "scheme_group(" + sch.getId() + "," + gi.getId() + ")", true, true);
                }
            }
            sendReply(sender, mId, sch.getId());
        }
    }

    class AddResponsibleGroup implements OrgManagerCommand {
        public String getId() {
            return "add_responsible_group";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String schId = command.getTerm(0).toString();
            String grId  = command.getTerm(1).toString();
            SchemeInstance sch = currentOE.findScheme(schId);
            if (sch == null) {
                sendReply(sender, mId, "error(\"the scheme " + schId + " does not exist\")");
                return;
            }

            GroupInstance gr = currentOE.findGroup(grId);
            if (gr == null) {
                sendReply(sender, mId, "error(\"the group " + grId + " does not exist\")");
                return;
            }
            if (!sender.equals(gr.getOwner())) {
                sendReply(sender, mId, "error(\"you are not the owner of the group " + schId + ", so you can not add it as responsible for a scheme.\")");
            }

            sch.addResponsibleGroup(gr);

            updateMembersOE(gr.getPlayers(), "scheme_group(" + schId + "," + grId + ")", true, true);
            sendReply(sender, mId, "ok");
        }
    }
    
    class RemoveScheme implements OrgManagerCommand {
        public String getId() {
            return "remove_scheme";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String schId       = command.getTerm(0).toString();
            SchemeInstance sch = currentOE.findScheme(schId);
            if (sch == null) {
                sendReply(sender, mId, "error(\"the scheme " + schId + " does not exist\")");
                return;
            }
            
            if (sch.getSpec().getFS().getBoolProperty("only-owner-can-remove-scheme", true) && !sender.equals(sch.getOwner())) {
                sendReply(sender, mId, "error(\"you are not the owner of the scheme " + schId + ", so you can not change it\")");
            }

            act(currentOE, sch);
            // send untell to agents
            updateMembersOE(currentOE.getAgents(), scheme2str(sch), false, false);
            sendReply(sender, mId, "ok");
        }
        protected void act(OE currentOE, SchemeInstance sch) throws MoiseException {
            currentOE.finishScheme(sch);        	
        }
    }
    
    class AbortScheme extends RemoveScheme {
        public String getId() {
            return "abort_scheme";
        }
        protected void act(OE currentOE, SchemeInstance sch) throws MoiseException {
            currentOE.abortScheme(sch);
        }
    }

    class SetGoalState implements OrgManagerCommand {
        public String getId() {
            return "set_goal_state";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String schId  = command.getTerm(0).toString();
            String goalId = command.getTerm(1).toString();
            String state  = command.getTerm(2).toString();

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

            if (state.equals("satisfied")) {
                gi.setSatisfied(sender);
            } else if (state.equals("impossible")) {
                gi.setImpossible(sender);
            }
            updateMembersOE(sch.getPlayers(), "goal_state(" + gi.getScheme().getId() + "," + gi.getSpec().getId() + ")", true, true);
            sendReply(sender, mId, "ok");
        }
    }

    class SetGoalArg implements OrgManagerCommand {
        public String getId() {
            return "set_goal_arg";
        }
        public void process(OE currentOE, Pred command, OEAgent sender, String mId) throws MoiseException {
            String schId  = command.getTerm(0).toString();
            String goalId = command.getTerm(1).toString();
            String arg    = command.getTerm(2).toString();
            if (arg.startsWith("\"")) {
                arg = arg.substring(1, arg.length() - 1);
            }
            String value  = command.getTerm(3).toString();
            if (command.getTerm(3).isString()) {
                value = ((StringTerm)command.getTerm(3)).getString();
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

            gi.setArgumentValue(arg, value);

            updateMembersOE(sch.getPlayers(), "goal_state(" + gi.getScheme().getId() + "," + gi.getSpec().getId() + ")", true, true);
            sendReply(sender, mId, "ok");
        }
    }

    /*
    String group2str(GroupInstance gr) {
        String sga = "root";
        if (gr.getSuperGroup() != null)
            sga = "super_gr("+gr.getSuperGroup().getId()+")";
        return "group(" + gr.getGrSpec().getId() + "," + gr.getId() + ")[owner(" + gr.getOwner().getId() +"),"+sga+"]";        
    }
    
    
    String rolePlayer2str(RolePlayer rp) {
        return "play(" + rp.getPlayer().getId() + "," + rp.getRole().getId() + "," + rp.getGroup().getId() + ")";
    }
    */
    
    String scheme2str(SchemeInstance sch) {
        return "scheme(" + sch.getSpec().getId() + "," + sch.getId() + ")[owner(" + sch.getOwner().getId() + ")]";        
    }
    
    String missionPlayer2str(MissionPlayer mp) {
        return "commitment(" + mp.getPlayer().getId() + "," + mp.getMission().getId() + "," + mp.getScheme().getId() + ")";
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

    @SuppressWarnings("unchecked")
	private void updateMembersOE(Collection ags, Object pEnv, boolean sendOE, boolean tell) {
        Set<OEAgent> all = new HashSet<OEAgent>(); // to remove duplicates
        Iterator iAgs = ags.iterator();
        while (iAgs.hasNext()) {
            Object next = iAgs.next();

            if (next instanceof OEAgent) // check if it is a list of OEAgents
                all.add((OEAgent) next);
            else if (next instanceof Player) // check if it is a list of Players
                all.add(((Player) next).getPlayer());

        }
        for (OEAgent ag : all) {
            updateMembersOE(ag, pEnv, sendOE, tell);
        }
    }

    private void updateMembersOE(OEAgent ag, Object pEnv, boolean sendOE, boolean tell) {
        if (!ag.getId().equals("orgManager")) {
            try {
                if (sendOE) {
                    Message moe = new Message("tell", null, ag.getId(), null);
                    moe.setPropCont(currentOE); //partialOE(ag)); // TODO: the clone of OE is not working!
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
