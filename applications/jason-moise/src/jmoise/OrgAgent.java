package jmoise;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.VarTerm;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
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

    private static final Term managerSource          = DefaultTerm.parse("source(orgManager)");
    private static final Atom rootAtom               = new Atom("root");

    private Logger            logger                 = Logger.getLogger(OrgAgent.class.getName());
    private OE                currentOE              = null;
    private Set<GoalInstance> alreadyGeneratedEvents = new HashSet<GoalInstance>();
    private String            orgManagerName         = "orgManager";
    
    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        logger = getTS().getLogger(); //Logger.getLogger(OrgAgent.class.getName() + "." + getAgName());
        introduceMySelf();
    }

    public String getOrgManagerName() {
        return orgManagerName;
    }
    
    public void setOrgManagerName(String name) {
        orgManagerName = name;
        introduceMySelf();
    }
    
    public OE getOE() {
        return currentOE;
    }
    
    private void introduceMySelf() {
        try {
            Message m = new Message("tell", null, getOrgManagerName(), "add_agent");
            super.sendMsg(m);
        } catch (Exception e) {
            logger.fine("Error sending add_agent to OrgManager!");
        }        
    }
    
    public void checkMail() {
        super.checkMail(); // get the messages from arch to circumstance
        
        Iterator<Message> i = getTS().getC().getMailBox().iterator();
        boolean updateGoalBels = false;
        boolean updateGoalEvt  = false;
        while (i.hasNext()) {
            try {
                Message m = i.next();
                // check if content is and OE
                if (m.getPropCont() instanceof OE) {
                    currentOE = (OE) m.getPropCont();
                    i.remove();
                } else {
                    // the content is a normal predicate
                    final String content = m.getPropCont().toString();
                    final boolean isTell = m.getIlForce().equals("tell");
                    if (isTell && content.startsWith("scheme(")) {
                    	i.remove();
                    	addAsBel(content);
                    //} else if (content.startsWith("update_goals")) { 
                    //    // I need to generate AS Triggers like !<orggoal>
                    //    i.remove();
                    //    updateGoalBels = true;
                    //    updateGoalEvt  = true;
                    } else if (content.startsWith("goal_state")) { 
                        // the state of a scheme i belong to has changed
                        i.remove();
                        updateGoalBels(Pred.parsePred(content));
                        updateGoalEvt  = true;
                    } else if (isTell && content.startsWith("scheme_group")) {
                    	i.remove();
                        // this message is generated when my group becomes
                        // responsible for a scheme
                    	Literal l = addAsBel(content);
                        generateObligationPermissionEvents(l);  			
                    } else if (isTell && content.startsWith("commitment")) { 
                        i.remove();
                        addAsBel(content);
                        // I need to generate AS Triggers like !<orggoal> since some scheme becomes well formed
                        updateGoalEvt  = true;

                    } else if (m.getIlForce().equals("untell") && content.startsWith("scheme")) {
                        String schId = Pred.parsePred(content).getTerm(1).toString();
                        removeAchieveEvents(schId);
                        removeBeliefs(schId);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error!", e);
            }
        } // while
        
        if (updateGoalBels)
            updateGoalBels();
        if (updateGoalEvt)
            generateOrgGoalEvents();

    }

    private Literal addAsBel(String b) {
        Literal l = Literal.parseLiteral(b);
        if (l.isAtom())
            l = new Literal(l.getFunctor());
        l.addAnnot(managerSource);
        getTS().getAg().addBel(l);
        return l;
    }
    
    void generateObligationPermissionEvents(Pred m) {
        // computes this agent obligations in the scheme
        String schId = m.getTerm(0).toString();
        String grId  = m.getTerm(1).toString();
        Set<Permission> obligations = new HashSet<Permission>();
        if (logger.isLoggable(Level.FINE)) logger.fine("Computing obl/per for " + m + " in obl=" + getMyOEAgent().getObligations() + " and per=" + getMyOEAgent().getPermissions());

        // obligations
        for (Permission p : getMyOEAgent().getObligations()) {
            if (p.getRolePlayer().getGroup().getId().equals(grId) && p.getScheme().getId().equals(schId)) {
                obligations.add(p);
                Literal l = Literal.parseLiteral("obligation(" + p.getScheme().getId() + "," 
                        + p.getMission().getId() + ")[" + "role(" + p.getRolePlayer().getRole().getId()
                        + "),group(" + p.getRolePlayer().getGroup().getGrSpec().getId() + ")]");
                l.addAnnot(managerSource);
                getTS().getAg().addBel(l);
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
                getTS().getAg().addBel(l);
                if (logger.isLoggable(Level.FINE)) logger.fine("New permission: " + l);
            }
        }
    }

   private void generateOrgGoalEvents() {
        for (GoalInstance gi : getMyOEAgent().getPossibleGoals()) {
            if (!alreadyGeneratedEvents.contains(gi)) {
                alreadyGeneratedEvents.add(gi);

                Literal l = Literal.parseLiteral(gi.getAsProlog());
                if (l.isAtom()) 
                    l = new Literal(l.getFunctor());
                Literal giID = new Literal("scheme");
                giID.addTerm(new Atom(gi.getScheme().getId()));
                l.addAnnot(giID);
                // "role(notimplemented),group(notimplemented)"+
                // TODO: add annots: role, group (percorrer as missoes do ag que
                // em GI, procurar os papel com obrigacao para essa missao)
                getTS().updateEvents(new Event(new Trigger(TEOperator.add, TEType.achieve, l), Intention.EmptyInt));
                if (logger.isLoggable(Level.FINE)) logger.fine("New goal: " + l);
            }
        }
    }

    void removeAchieveEvents(String schId) {
        Iterator<GoalInstance> i = alreadyGeneratedEvents.iterator();
        while (i.hasNext()) {
            GoalInstance gi = i.next();
            if (gi.getScheme().getId().equals(schId)) {
                i.remove();
            }
        }
    }

    private static final PredicateIndicator obligationLiteral  = new PredicateIndicator("obligation", 2);
    private static final PredicateIndicator permissionLiteral  = new PredicateIndicator("permission", 2);
    private static final PredicateIndicator schemeGroupLiteral = new PredicateIndicator("scheme_group", 2);
    private static final PredicateIndicator goalStateLiteral   = new PredicateIndicator("goal_state", 3);
    private static final PredicateIndicator schPlayersLiteral  = new PredicateIndicator("sch_players", 2);
    private static final PredicateIndicator commitmentLiteral  = new PredicateIndicator("commitment", 3);

    /** removes all bels related to a Scheme */
    void removeBeliefs(String schId) {
        Agent ag = getTS().getAg();
        ag.abolish(buildLiteralToCleanBB(schId, obligationLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(schId, permissionLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(schId, schemeGroupLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(schId, goalStateLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(schId, schPlayersLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(schId, commitmentLiteral, true), null);
    }

    private Literal buildLiteralToCleanBB(String schId, PredicateIndicator pred, boolean schInEnd) {
        Literal l = new Literal(pred.getFunctor());
        if (!schInEnd) {
            l.addTerm(new Atom(schId));
        }
        for (int i=1;i<pred.getArity();i++) {
            l.addTerm(new UnnamedVar());
        }
        if (schInEnd) {
            l.addTerm(new Atom(schId));            
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
        String schId  = arg.getTerm(0).toString();
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

    // goal states
    private static final Atom aWaiting    = new Atom("waiting");
    private static final Atom aPossible   = new Atom("possible");
    private static final Atom aImpossible = new Atom("impossible");
    private static final Atom aAchieved   = new Atom("achieved");

    void updateGoalBels(GoalInstance gi) {
        Pred gap = Pred.parsePred(gi.getAsProlog());
        
        if (gi.getScheme().getRoot() == gi) {
            gap.addAnnot(rootAtom);
        }

        Atom gState = aWaiting;
        if (gi.isPossible()) {
            gState = aPossible;
        } else if (gi.isAchieved()) {
            gState = aAchieved;
        } else if (gi.isImpossible()) {
            gState = aImpossible;
        }

        // create the literal to be added
        Literal gil = new Literal("goal_state");
        gil.addTerm(new Atom(gi.getScheme().getId()));
        gil.addTerm(gap);
        VarTerm S = new VarTerm("S");
        gil.addTerm(S);
        gil.addAnnot(managerSource);

        Unifier u = new Unifier();
        Literal gilInBB = getTS().getAg().findBel(gil, u);
        if (gilInBB != null) {
            // the agent believes in the goal, remove if different
        	// so that an event is produced
        	if (!u.get(S).equals(gState) || !gap.equals(gilInBB.getTerm(1))) { 
                if (!getTS().getAg().delBel(gilInBB))
                	logger.warning("Belief "+gilInBB+" should be deleted, but was not!");
                else if (logger.isLoggable(Level.FINE)) logger.fine("Remove goal belief: " + gil);
            }
        }

        gil = (Literal)gil.clone();
        gil.setTerm(2, gState);
        
        if (getTS().getAg().addBel(gil))
        	if (logger.isLoggable(Level.FINE)) logger.fine("New goal belief: " + gil);
    }
}
