package jmoise;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.VarTerm;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import moise.oe.GoalInstance;
import moise.oe.MissionPlayer;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.Permission;
import moise.oe.SchemeInstance;

/**
  * Organisational Architecture, binds Jason agent to
  * the Moise+ infrastructure
  */
public class OrgAgent extends AgArch {

    OE                currentOE              = null;
    Set<GoalInstance> alreadyGeneratedEvents = new HashSet<GoalInstance>();
    Term              managerSource          = TermImpl.parse("source(orgManager)");
    Logger            logger                 = Logger.getLogger(OrgAgent.class.getName());

    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        logger = Logger.getLogger(OrgAgent.class.getName() + "." + getAgName());
        try {
            Message m = new Message("tell", null, "orgManager", "addAgent");
            super.sendMsg(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending addAgent to OrgManager!", e);
        }
    }

    public void checkMail() {
        super.checkMail(); // get the messages
        // check the MailBox (at TS) for org messages
        Iterator i = fTS.getC().getMB().iterator();
        while (i.hasNext()) {
            try {
                Message m = (Message) i.next();
                // check if content is and OE
                try {
                    currentOE = (OE) m.getPropCont();
                    i.remove();
                } catch (Exception e) {
                    // the content is a normal predicate
                    String content = m.getPropCont().toString();
                    if (content.startsWith("schemeGroup")) { 
                        // this message is generated when my group becomes
                        // responsible for a scheme
                        generateObligationPermissionEvents(Pred.parsePred(content));
                    } else if (content.startsWith("updateGoals")) { 
                        // I need to generate AS Trigger like !<orggoal>
                        i.remove();
                        updateGoalBels();
                        generateOrgGoalEvents();
                    } else if (content.startsWith("goalState")) { 
                        // the state of a scheme i belong to has changed
                        i.remove();
                        updateGoalBels(Pred.parsePred(content));
                        generateOrgGoalEvents();

                    } else if (m.getIlForce().equals("untell") && content.startsWith("scheme")) {
                        String schId = Pred.parsePred(content).getTerm(1).toString();
                        removeAchieveEvents(schId);
                        removeBeliefs(schId);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error!", e);
            }
        }
    }

    void generateObligationPermissionEvents(Pred m) {
        // computes this agent obligations in the scheme
        String schId = m.getTerm(0).toString();
        String grId = m.getTerm(1).toString();
        Set<Permission> obligations = new HashSet<Permission>();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Computing obl/per for " + m + " in obl=" + getMyOEAgent().getObligations() 
                    + " and per=" + getMyOEAgent().getPermissions());
        }
        // obligations
        for (Permission p : getMyOEAgent().getObligations()) {
            if (p.getRolePlayer().getGroup().getId().equals(grId) && p.getScheme().getId().equals(schId)) {
                obligations.add(p);
                Literal l = Literal.parseLiteral("obligation(" + p.getScheme().getId() + "," 
                        + p.getMission().getId() + ")[" + "role(" + p.getRolePlayer().getRole().getId()
                        + "),group(" + p.getRolePlayer().getGroup().getGrSpec().getId() + ")]");
                l.addAnnot(managerSource);
                fTS.getAg().addBel(l);
                if (logger.isLoggable(Level.FINE)) logger.fine("New obligation: " + l);
            }
        }

        // permissions
        for (Permission p : getMyOEAgent().getPermissions()) {
            if (p.getRolePlayer().getGroup().getId().equals(grId) && p.getScheme().getId().equals(schId) && !obligations.contains(p)) {
                Literal l = Literal.parseLiteral("permission(" + p.getScheme().getId() + "," 
                        + p.getMission().getId() + ")[" + "role(" + p.getRolePlayer().getRole().getId()
                        + "),group(" + p.getRolePlayer().getGroup().getGrSpec().getId() + ")]");
                l.addAnnot(managerSource);
                fTS.getAg().addBel(l);
                if (logger.isLoggable(Level.FINE)) logger.fine("New permission: " + l);
            }
        }
    }

    void generateOrgGoalEvents() {
        for (GoalInstance gi : getMyOEAgent().getPossibleAndPermittedGoals()) {
            if (!alreadyGeneratedEvents.contains(gi)) {
                alreadyGeneratedEvents.add(gi);

                Literal l = Literal.parseLiteral(gi.getAsProlog());
                Term giID = new Literal(true,"scheme");
                giID.addTerm(new Literal(true,gi.getScheme().getId()));
                l.addAnnot(giID);
                // "role(notimplemented),group(notimplemented)"+
                // TODO: add annots: role, group (percorrer as missoes do ag que
                // em GI, procurar os papel com obrigacao para essa missao)
                fTS.updateEvents(new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l), Intention.EmptyInt));
                if (logger.isLoggable(Level.FINE)) logger.fine("New goal: " + l);
            }
        }
    }

    void removeAchieveEvents(String schId) {
        Iterator i = alreadyGeneratedEvents.iterator();
        while (i.hasNext()) {
            GoalInstance gi = (GoalInstance) i.next();
            if (gi.getScheme().getId().equals(schId)) {
                i.remove();
            }
        }
    }

    private static PredicateIndicator obligationLiteral  = new PredicateIndicator("obligation", 2);
    private static PredicateIndicator permissionLiteral  = new PredicateIndicator("permission", 2);
    private static PredicateIndicator schemeGroupLiteral = new PredicateIndicator("schemeGroup", 2);
    private static PredicateIndicator goalStateLiteral   = new PredicateIndicator("goalState", 3);
    private static PredicateIndicator schPlayersLiteral  = new PredicateIndicator("schPlayers", 2);
    private static PredicateIndicator commitmentLiteral  = new PredicateIndicator("commitment", 3);

    /** remove all bels related to a Scheme */
    void removeBeliefs(String schId) {
        fTS.getAg().abolish(buildLiteralToCleanBB(schId, obligationLiteral, false), new Unifier());
        fTS.getAg().abolish(buildLiteralToCleanBB(schId, permissionLiteral, false), new Unifier());
        fTS.getAg().abolish(buildLiteralToCleanBB(schId, schemeGroupLiteral, false), new Unifier());
        fTS.getAg().abolish(buildLiteralToCleanBB(schId, goalStateLiteral, false), new Unifier());
        fTS.getAg().abolish(buildLiteralToCleanBB(schId, schPlayersLiteral, false), new Unifier());
        fTS.getAg().abolish(buildLiteralToCleanBB(schId, commitmentLiteral, true), new Unifier());
    }

    private Literal buildLiteralToCleanBB(String schId, PredicateIndicator pred, boolean schInEnd) {
        Literal l = new Literal(Literal.LPos, pred.getFunctor());
        if (!schInEnd) {
            l.addTerm(new TermImpl(schId));
        }
        for (int i=1;i<pred.getArity();i++) {
            l.addTerm(new UnnamedVar());
        }
        if (schInEnd) {
            l.addTerm(new TermImpl(schId));            
        }
        return l;
    }
    
    OEAgent getMyOEAgent() {
        return currentOE.getAgent(getAgName());
    }

    /** add/remove bel regarding the goals' state */
    void updateGoalBels() {
        // for all missions
        // for all goals
        // if not in BB, add
        // if different from BB, remove/add
        for (MissionPlayer mp : getMyOEAgent().getMissions()) {
            for (GoalInstance gi : mp.getScheme().getGoals()) {
                updateGoalBels(gi);
            }
        }
    }

    void updateGoalBels(Pred arg) {
        String schId = arg.getTerm(0).toString();
        String goalId = arg.getTerm(1).toString();
        for (SchemeInstance sch : getMyOEAgent().getAllMySchemes()) {
            if (sch.getId().equals(schId)) {
                GoalInstance gi = sch.getGoal(goalId);
                if (gi != null) {
                    updateGoalBels(gi);
                }
            }
        }
    }

    void updateGoalBels(GoalInstance gi) {
        Pred gap = Pred.parsePred(gi.getAsProlog());
        //if (!gap.isGround()) {
        //    return;
        //}
        if (gi.getScheme().getRoot() == gi) {
            gap.addAnnot(new TermImpl("root"));
        }
        //BeliefBase bb = fTS.getAg().getBS();
        String gState = "unsatisfied";
        if (gi.isSatisfied()) {
            gState = "satisfied";
        } else if (gi.isImpossible()) {
            gState = "impossible";
        }

        // create the literal to be added
        Literal gil = new Literal(Literal.LPos, "goalState");
        gil.addTerm(new TermImpl(gi.getScheme().getId()));
        gil.addTerm(gap);
        gil.addTerm(new VarTerm("S"));
        
        
        // remove it from BB
        Unifier u = new Unifier();
        Literal gilInBB = fTS.getAg().believes(gil, u);
        if (gilInBB != null) {
            // the agent believes in the goal, remove if different
            if (!u.get("S").equals(gState)) {
                fTS.getAg().delBel(gilInBB);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Remove goal belief: " + gilInBB);
                }
            }
        }

        gil = new Literal(Literal.LPos, "goalState");
        gil.addTerm(new TermImpl(gi.getScheme().getId()));
        gil.addTerm(gap);
        gil.addTerm(new TermImpl(gState));
        gilInBB = fTS.getAg().believes(gil, u);
        if (gilInBB == null) {
            gil.addAnnot(managerSource);
            fTS.getAg().addBel(gil);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("New goal belief: " + gil);
            }
        }
    }
}
