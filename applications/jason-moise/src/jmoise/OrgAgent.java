package jmoise;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.VarTerm;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.parser.ParseException;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import moise.oe.GoalInstance;
import moise.oe.GroupInstance;
import moise.oe.MissionPlayer;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.Permission;
import moise.oe.RolePlayer;
import moise.oe.SchemeInstance;
import moise.os.fs.Goal;
import moise.os.fs.Goal.GoalType;

/**
  * Organisational Architecture, binds Jason agent to
  * the Moise+ infrastructure
  */
public class OrgAgent extends AgArch {

    private static final Term managerSource          = Pred.createSource(new Atom("orgManager"));
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
        
        Circumstance C = getTS().getC();
        Iterator<Message> i    = C.getMailBox().iterator();
        boolean updateGoalBels = false;
        boolean updateGoalEvt  = false;
        while (i.hasNext()) {
            try {
                Message m = i.next();
                // check if content is and OE
                if (m.getPropCont() instanceof OE) {
                    currentOE = (OE) m.getPropCont();
                    i.remove();
                } else if (m.getSender().equals(getOrgManagerName())) {
                    // the content is a normal predicate
                    final String content   = m.getPropCont().toString();
                    
                    // test if it is the result of some org action    
                    if (m.getInReplyTo() != null) {
                        // find the intention
                        Intention pi = C.getPendingIntentions().remove("om/"+m.getInReplyTo());
                        if (pi != null) {
                            i.remove();
                            resumeIntention(pi, content, C);
                        }
                    } else {
                        // add all tells directly in the memory
                        if (m.getIlForce().equals("tell")) {
                            i.remove();
                            if (content.startsWith("goal_state")) { 
                                // the state of a scheme i belong to has changed
                                // add all goals of the scheme in BB
                                updateGoalBels = true;
                                updateGoalEvt  = true;
                                // Note: must change all goals, because the state of others may also change (waiting->possible)
                            } else {
                                Literal cl = addAsBel(content);
                                
                                if (content.startsWith("scheme_group")) {
                                    // this message is generated when my group becomes
                                    // responsible for a scheme
                                    generateObligationPermissionEvents(cl);
                                } else if (content.startsWith("commitment")) {
                                    // add all goals of the scheme in BB
                                    updateGoalBels = true;
                                    // I need to generate AS Triggers like !<orggoal> since some scheme becomes well formed
                                    updateGoalEvt  = true;
                                }
                            }

                        } else if ( m.getIlForce().equals("untell") ) {
                            i.remove();
                            Literal cl = delAsBel(content);
                            
                            if (content.startsWith("scheme_group")) {
                                Term sch = cl.getTerm(0);
                                Term gr  = cl.getTerm(1);
                                removeObligationPermissionBeliefs(sch, gr, "obligation");
                                removeObligationPermissionBeliefs(sch, gr, "permission");
                            } else if (content.startsWith("scheme")) {
                                String schId = cl.getTerm(1).toString();
                                cleanGoalsOfSch(schId);
                                removeBeliefs(schId);
                            } else if (content.startsWith("commitment")) {
                                // if I remove my commit, remove the goals from BB
                                String schId = cl.getTerm(2).toString();
                                SchemeInstance sch = currentOE.findScheme(schId);
                                if (sch != null && !sch.isPlayer(getMyOEAgent())) {
                                    cleanGoalsOfSch(schId);
                                    removeBeliefs(schId);
                                }
                            }
                        }                        
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error!", e);
            }
        } // while
        try {
            if (updateGoalBels)
                updateGoalBels();
            if (updateGoalEvt)
                generateOrgGoalEvents();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error!", e);
        }
    }

    private Literal addAsBel(String b) throws RevisionFailedException {
        Literal l = Literal.parseLiteral(b);
        l.addAnnot(managerSource);
        getTS().getAg().addBel(l);
        return l;
    }
    private Literal delAsBel(String b) throws RevisionFailedException {
        Literal l = Literal.parseLiteral(b);
        l.addAnnot(managerSource);
        getTS().getAg().delBel(l);
        return l;
    }

    private void resumeIntention(Intention pi, String content, Circumstance C) {
        pi.setSuspended(false);
        C.addIntention(pi); // add it back in I
        Structure body = (Structure)pi.peek().removeCurrentStep(); // remove the internal action
        
        if (content.startsWith("error")) {
            // fail the IA
            PlanBody pbody = pi.peek().getPlan().getBody();
            pbody.add(0, new PlanBodyImpl(BodyType.internalAction, new InternalActionLiteral(".fail")));
            getTS().getLogger().warning("Error in organisational action '"+body+"': "+content);
        } else {
            // try to unify the return value
            //System.out.println("answer is "+content+" or "+DefaultTerm.parse(content)+" with body "+body);
            // if the last arg of body is a free var
            Term lastTerm = body.getTerm(body.getArity()-1); 
            if (!lastTerm.isGround()) {
                try {
                    pi.peek().getUnif().unifies(lastTerm, ASSyntax.parseTerm(content));
                } catch (ParseException e) {
                    // no problem, the content is not a term
                }
                //System.out.println("un = "+pi.peek().getUnif());
            }
        }
        
    }
    
    private void generateObligationPermissionEvents(Literal m) throws RevisionFailedException {
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
                        + "),group(" + p.getRolePlayer().getGroup().getId() + ")]");
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
                        + "),group(" + p.getRolePlayer().getGroup().getId() + ")]");
                l.addAnnot(managerSource);
                getTS().getAg().addBel(l);
                if (logger.isLoggable(Level.FINE)) logger.fine("New permission: " + l);
            }
        }
    }
    
    
    private void removeObligationPermissionBeliefs(Term sch, Term gr, String type) throws RevisionFailedException {
        // computes this agent obligations in the scheme
        Structure giAnnot = new Structure("group");
        giAnnot.addTerm(gr);
        
        Literal obl = ASSyntax.createLiteral(type, sch, new UnnamedVar()).addAnnots(giAnnot);
        
        // find obligation(sch,_)[group(id)]
        Unifier un = new Unifier();
        Iterator<Literal> i = getTS().getAg().getBB().getCandidateBeliefs(obl, un);
        if (i != null) {
            List<Literal> todel = new ArrayList<Literal>();
            while (i.hasNext()) {
                Literal inbb = i.next();
                un.clear();
                if (un.unifies(obl, inbb))
                    todel.add(inbb);
            }
            for (Literal l: todel) {
                getTS().getAg().delBel(l);
            }
        }
    }

    private void generateOrgGoalEvents() {
	   OEAgent me = getMyOEAgent();
	   for (GoalInstance gi : getMyOEAgent().getPossibleGoals()) {
		   if (!alreadyGeneratedEvents.contains(gi)) {
                alreadyGeneratedEvents.add(gi);

                Literal l = Literal.parseLiteral(gi.getAsProlog());
                // add annot with scheme id
                Structure giID = new Structure("scheme", 1);
                giID.addTerm(new Atom(gi.getScheme().getId()));
                l.addAnnot(giID);
                
                // add annot with mission id
                Structure mission = new Structure("mission", 1);
                for (MissionPlayer mp: getMyOEAgent().getMissions()) {
                    if (mp.getMission().getGoals().contains(gi.getSpec())) {
                        mission.addTerm(new Atom(mp.getMission().getId()));                        
                    }
                }
                l.addAnnot(mission);
                
                // add annot with type of goal
                Structure type = new Structure("type", 1);
                type.addTerm(getGoalTypeAtom(gi.getSpec()));
                l.addAnnot(type);
                
                // add source annot
                l.addAnnot(managerSource);
                
                // try to find the role/group of this goal
                // (the first all resp group of the scheme where I am)
                for (GroupInstance g: gi.getScheme().getResponsibleGroups()) {
                	for (RolePlayer rp: g.getPlayers()) {
                		if (rp.getPlayer().equals(me)) {
                			Structure role = new Structure("role");
                			role.addTerm(new Atom(rp.getRole().getId()));
                			l.addAnnot(role);

                			Structure group = new Structure("group");
                			group.addTerm(new Atom(rp.getGroup().getId()));
                			l.addAnnot(group);
                		}
                	}
                }
                getTS().updateEvents(new Event(new Trigger(TEOperator.add, TEType.achieve, l), Intention.EmptyInt));
                if (logger.isLoggable(Level.FINE)) logger.fine("New goal: " + l);
		   }
	   }
    }
   
   	private static final Atom aAchievementGoal = new Atom(GoalType.achievement.toString()); 
   	private static final Atom aMaintenanceGoal = new Atom(GoalType.maintenance.toString());
   	
   	public static Atom getGoalTypeAtom(Goal g) {
   		switch (g.getType()) {
   		case achievement: return aAchievementGoal;
   		case maintenance: return aMaintenanceGoal;
   		}
   		return null;
   	}

    void cleanGoalsOfSch(String schId) {
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
    void removeBeliefs(String schId) throws RevisionFailedException {
        Agent ag = getTS().getAg();
        Atom aSchId = new Atom(schId);
        ag.abolish(buildLiteralToCleanBB(aSchId, obligationLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(aSchId, permissionLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(aSchId, schemeGroupLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(aSchId, goalStateLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(aSchId, schPlayersLiteral, false), null);
        ag.abolish(buildLiteralToCleanBB(aSchId, commitmentLiteral, true), null);
    }

    private Literal buildLiteralToCleanBB(Atom aSchId, PredicateIndicator pred, boolean schInEnd) {
        Literal l = new LiteralImpl(pred.getFunctor());
        if (!schInEnd) {
            l.addTerm(aSchId);
        }
        for (int i=1;i<pred.getArity();i++) {
            l.addTerm(new UnnamedVar());
        }
        if (schInEnd) {
            l.addTerm(aSchId);            
        }
        return l;
    }
    
    OEAgent getMyOEAgent() {
        return currentOE.getAgent(getAgName());
    }

    /** add/remove bel regarding the goals' state */
    void updateGoalBels() throws RevisionFailedException {
        // for all missions
        // for all goals of the mission's scheme
        // if not in BB, add
        // if different from BB, remove/add
        for (MissionPlayer mp : getMyOEAgent().getMissions()) {
            for (GoalInstance gi : mp.getScheme().getGoals()) {
                updateGoalBels(gi);
            }
        }
    }

    void updateGoalBels(Pred arg) throws RevisionFailedException {
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

    void updateGoalBels(GoalInstance gi) throws RevisionFailedException {
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
        VarTerm S = new VarTerm("S");
        Literal gil = new LiteralImpl("goal_state");
        gil.addTerms(new Atom(gi.getScheme().getId()), gap, S);
        gil.addAnnot(managerSource);

        Unifier u = new Unifier();
        Literal gilInBB = getTS().getAg().findBel(gil, u);
        if (gilInBB != null) {
            // the agent believes in the goal, remove if different
        	// so that an event is produced
        	if (!u.get(S).equals(gState) || !gap.equals(gilInBB.getTerm(1))) { 
                if (!getTS().getAg().delBel(gilInBB))
                	logger.warning("Belief "+gilInBB+" should be deleted, but was not!");
                else 
                    if (logger.isLoggable(Level.FINE)) logger.fine("Remove goal belief: " + gil);
            }
        }

        gil = (Literal)gil.clone();
        gil.setTerm(2, gState);
        
        if (getTS().getAg().addBel(gil))
        	if (logger.isLoggable(Level.FINE)) logger.fine("New goal belief: " + gil);
    }
}
