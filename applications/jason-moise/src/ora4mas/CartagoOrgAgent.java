package ora4mas;

import static jason.asSyntax.ASSyntax.createLiteral;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.parser.ParseException;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import moise.prolog.ToProlog;
import npl.NormativeProgram;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.OrgArt;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Player;
import c4jason.CAgentArch;
import c4jason.CartagoAction;
import c4jason.CartagoEnvironment;
import c4jason.PendingAction;
import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.Event;
import cartago.ICartagoContext;
import cartago.Op;

/**
  * Organisational Architecture, binds Jason agent to
  * the Moise+ infrastructure based on ORA4MAS
  */
public class CartagoOrgAgent extends CAgentArch {

    private Logger            logger                 = Logger.getLogger(CartagoOrgAgent.class.getName());
    private String            currentOS              = null;
    
    protected Map<PredicateIndicator,Runnable> runOnPerceive = new HashMap<PredicateIndicator,Runnable>();
    //public static final Structure ora4masSource = ASSyntax.createStructure("source", new Atom("ora4mas"));
    
    public static final PredicateIndicator piResponsibleGroup = new PredicateIndicator("responsible_group", 2);
    public static final PredicateIndicator piObligation       = new PredicateIndicator(NormativeProgram.OblFunctor, 4);
    public static final PredicateIndicator piGrSpec           = new PredicateIndicator("group_specification", 4);
    public static final PredicateIndicator piSchSpec          = new PredicateIndicator("scheme_specification", 3);
    public static final PredicateIndicator piGrPlayer         = new PredicateIndicator("play", 3);
    public static final PredicateIndicator piSchPlayer        = new PredicateIndicator("commitment", 3);

    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        logger = getTS().getLogger();
                
        // add org actions
        CartagoEnvironment.getInstance().putCartagoAction("create_group", new CreateGroup());
        CartagoEnvironment.getInstance().putCartagoAction("remove_group", new RemoveOrgArt());
        CartagoEnvironment.getInstance().putCartagoAction("create_scheme", new CreateScheme());
        CartagoEnvironment.getInstance().putCartagoAction("remove_scheme", new RemoveOrgArt());
        CartagoEnvironment.getInstance().putCartagoAction("add_responsible_group", new AddResponsibleGroup());
        CartagoEnvironment.getInstance().putCartagoAction("adopt_role", new AdoptRole());
        CartagoEnvironment.getInstance().putCartagoAction("leave_role", new LeaveRole());
        CartagoEnvironment.getInstance().putCartagoAction("commit_mission", new CommitMission());
        CartagoEnvironment.getInstance().putCartagoAction("leave_mission", new LeaveMission());
        CartagoEnvironment.getInstance().putCartagoAction("goal_achieved", new GoalAchieved());
        CartagoEnvironment.getInstance().putCartagoAction("set_goal_arg", new SetGoalArgValue());

        /*
        if (stts.getUserParameter("workspace") != null) {
            try {
                String wsp = stts.getUserParameter("workspace");
                if (wsp.startsWith("\""))
                    wsp = wsp.substring(1,wsp.length()-1);
                logger.info("Joining workspace "+wsp);
                getContext("default").getContext().joinWorkspace(wsp, null, null, new UserIdCredential(getAgName()));
            } catch (CartagoException e) {
                logger.log(Level.SEVERE, "Error joining workspace "+stts.getUserParameter("workspace"), e);
            }
        }*/
    }

    public String getCurrentOSFile() {
        return currentOS;
    }

    public void setCurrentOSFile(String currentOS) {
        this.currentOS = currentOS;
    }
    
    
    @Override
    public void notifyNewObsPropSet(ArtifactId aid,  ArtifactObsProperty[] props, int timestamp) {
        boolean org = false;
        if (isOrgArt(aid.getArtifactType())) 
            for (ArtifactObsProperty ob: props)
                if (updateBB(aid, ob.getName(), ob.getValue(0))) 
                    org = true;
        
        if (org)
            getArchInfraTier().wake();
        else
            super.notifyNewObsPropSet(aid, props, timestamp);
    }
    
    @Override
    public void notifyEnvChanged(Event ev) {
        if (isOrgArt(ev.getSourceId().getArtifactType()) && updateBB(ev.getSourceId(), ev.getLabel(), ev.getContent(0)) )
            getArchInfraTier().wake(); // ignore for cartago
        else
            super.notifyEnvChanged(ev); 
    };
    
    @Override
    public List<Literal> perceive() {
        synchronized (runOnPerceive) {
            for (PredicateIndicator pi: runOnPerceive.keySet()) {
                runOnPerceive.get(pi).run();
            }
            runOnPerceive.clear();
        }

        return super.perceive();
    }
    
    @SuppressWarnings("unchecked")
    private boolean updateBB(final ArtifactId source, String label, final Object content) {
        //System.out.println(" ["+getAgName()+ "] * "+label+" = "+content);
        synchronized (runOnPerceive) {
            if (label.equals(GroupBoard.obsPropSchemes)) {
                
                // focus on all schemes of the group
                try {
                    ICartagoContext actx = getCurrentContext();
                    for (String s: (Collection<String>)content) {
                        ArtifactId aid = actx.lookupArtifact(s);
                        actx.focus(aid, null);
                    }
                } catch (CartagoException e) {
                    logger.log(Level.SEVERE,"Error on focusing "+content, e);
                }

                runOnPerceive.put(piResponsibleGroup, new Runnable() {  public void run() {
                    updateResponsibleGroup(source, content);
                }});
            } else if (label.equals(SchemeBoard.obsPropGoals)) {
                runOnPerceive.put(SchemeBoard.piGoalState, new Runnable() {  public void run() {
                    updateGoals(source, content);
                }});
            } else if (label.equals(SchemeBoard.obsPropOblActv)) {
                runOnPerceive.put(piObligation, new Runnable() {  public void run() {
                    updateObligations(source, content);
                }});
            } else if (label.equals(SchemeBoard.obsPropPlayers) && source.getArtifactType().equals(SchemeBoard.class.getName())) {
                runOnPerceive.put(piSchPlayer, new Runnable() {  public void run() {
                    updatePlayers(source, content, piSchPlayer);
                }});
            } else if (label.equals(GroupBoard.obsPropPlayers) && source.getArtifactType().equals(GroupBoard.class.getName())) {
                runOnPerceive.put(piGrPlayer, new Runnable() {  public void run() {
                    updatePlayers(source, content, piGrPlayer);
                }});
            } else if (label.equals(GroupBoard.obsPropSpec) && source.getArtifactType().equals(GroupBoard.class.getName())) {
                runOnPerceive.put(piGrSpec, new Runnable() {  public void run() {
                    updateSpecification(source, content, piGrSpec);
                }});
            } else if (label.equals(SchemeBoard.obsPropSpec) && source.getArtifactType().equals(SchemeBoard.class.getName())) {
                runOnPerceive.put(piSchSpec, new Runnable() {  public void run() {
                    updateSpecification(source, content, piSchSpec);
                }});
            //} else if (label.equals(OrgArt.sglNormFailure)) {
            //    Literal reason = ((JasonLiteralWrapper)content).getLiteral();
            //    List<Term> failAnnots = JasonException.createBasicErrorAnnots( "norm_failure", "Error resuming pending intention related to organisational action: "+ev);
            //    failAnnots.add(ASSyntax.createStructure("norm_failure", reason.getTerm(0)));
                // TODO: fail the intention and put good annotations in the failure event
            } else if (label.equals(SchemeBoard.obsPropGroups)) { // ignore this obs prop in jason
            } else if (label.equals(OrgArt.sglDestroyed)) { // artifact destroyed
                runOnPerceive.put(piSchSpec, new Runnable() {  public void run() {
                    cleanupBB(source);
                }});
                return false;
            } else {
                return false;
            }
            return true;            
        }
    }
    
    private void updateSpecification(ArtifactId source, Object content, PredicateIndicator pi) {
        try {
            if (content instanceof ToProlog) {

                List<Literal> toAdd = new ArrayList<Literal>();                
                Literal l = ASSyntax.parseLiteral( ((ToProlog)content).getAsProlog() );
                l.addSource(ASSyntax.createAtom(source.getWorkspaceId().getName()));
                Term artAnnot = ASSyntax.createStructure("artifact", ASSyntax.createAtom(source.getName()));
                l.addAnnot(artAnnot);
                toAdd.add(l);
                orgBUF(toAdd, pi, artAnnot);
            }
        } catch (ParseException e) {
            logger.log(Level.SEVERE,"Error updating "+content, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateObligations(ArtifactId source, Object content) {
        List<Literal> toAdd = new ArrayList<Literal>();
        Term artAnnot = ASSyntax.createStructure("artifact", ASSyntax.createAtom(source.getName()));
        for (Literal o: (Collection<Literal>)content) {
            Literal l = o.copy();
            l.addSource(ASSyntax.createAtom(source.getWorkspaceId().getName()));
            l.addAnnot(artAnnot);
            toAdd.add(l);
        }
        orgBUF(toAdd, piObligation, artAnnot);
    }

    @SuppressWarnings("unchecked")
    private void updateResponsibleGroup(ArtifactId source, Object content)  {
        // content is a set of schemes
        Atom gId = new Atom(source.getName());
        Term grAnnot = ASSyntax.createStructure("artifact", ASSyntax.createAtom(source.getName()));
        List<Literal> toAdd = new ArrayList<Literal>();
        for (String s: (Collection<String>)content) {
            Atom schAtom = new Atom(s);
            Literal l = createLiteral(piResponsibleGroup.getFunctor(), gId, schAtom);
            l.addSource(ASSyntax.createAtom(source.getWorkspaceId().getName()));
            l.addAnnot(grAnnot);
            l.addAnnot(ASSyntax.createStructure("artifact", schAtom));
            toAdd.add(l);
        }
        orgBUF(toAdd, piResponsibleGroup, grAnnot);
    }

    @SuppressWarnings("unchecked")
    private void updatePlayers(ArtifactId source, Object content, PredicateIndicator pred) {
        // event arg is a collection of Players (from oe)
        Term artAnnot = ASSyntax.createStructure("artifact", ASSyntax.createAtom(source.getName()));
        List<Literal> toAdd = new ArrayList<Literal>();
        for (Player p: (Collection<Player>)content) {
            Literal l = createLiteral(pred.getFunctor(), new Atom(p.getAg()), new Atom(p.getTarget()), new Atom(source.getName()));
            l.addSource(ASSyntax.createAtom(source.getWorkspaceId().getName()));
            l.addAnnot(artAnnot);
            toAdd.add(l);
        }
        orgBUF(toAdd, pred, artAnnot);
    }
    
    @SuppressWarnings("unchecked")
    private void updateGoals(ArtifactId source, Object content) {
        // event arg is a collection of literals with goal state
        Term artAnnot = ASSyntax.createStructure("artifact", ASSyntax.createAtom(source.getName()));
        List<Literal> toAdd = new ArrayList<Literal>();
        for (Literal g: (Collection<Literal>)content) {
            Literal l = g.copy();
            l.addSource(ASSyntax.createAtom(source.getWorkspaceId().getName()));
            l.addAnnot(artAnnot);
            toAdd.add(l);
        }
        orgBUF(toAdd, SchemeBoard.piGoalState, artAnnot);
    }

    private void cleanupBB(ArtifactId source) {
        try {
            Literal v = new UnnamedVar();
            v.addAnnot( ASSyntax.createStructure("artifact", new Atom(source.getName())));
            getTS().getAg().abolish(v, null);
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
    }
    
    
    public boolean isOrgArt(String aType) {
        return aType.equals(GroupBoard.class.getName()) || 
               aType.equals(SchemeBoard.class.getName());
    }
    
    private List<Literal> orgBUF(List<Literal> toAdd, PredicateIndicator bel, Term source) {
        if (getTS().getLogger().isLoggable(Level.FINE)) getTS().getLogger().fine("orgBUF for "+bel+" with "+toAdd+" for "+source);

        List<Literal> toDel = new ArrayList<Literal>();
        Agent ag = getTS().getAg();
        
        // remove old bels
        Iterator<Literal> i = ag.getBB().getCandidateBeliefs(bel);
        if (i != null) {
            while (i.hasNext()) {
                Literal l = i.next();
                ListTerm artifacts = l.getAnnots("artifact");
                if (source == null || artifacts.contains(source)) { // consider only beliefs from source art
                    boolean isInOE = false;
                    Iterator<Literal> ip = toAdd.iterator();
                    while (ip.hasNext()) {
                        Literal p = ip.next();
                        if (p.equalsAsStructure(l)) { // if the beliefs is in the BB, remove from toAdd (ignore annots)
                            isInOE = true;
                            ip.remove();
                            break;
                        }
                    }
                    if (!isInOE) { // if the beliefs is not in toADD, remove from BB
                        toDel.add(l);
                    }
                }
            }
        }
        
        if (getTS().getLogger().isLoggable(Level.FINE)) getTS().getLogger().fine("org buf add: "+toAdd+"\norg bug del: "+toDel);        
        try {
            for (Literal l: toDel)
                ag.delBel(l);
            
            for (Literal l: toAdd)
                ag.addBel(l);
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
        
        if (getTS().getLogger().isLoggable(Level.FINE)) getTS().getLogger().fine("end of orgBUG");
        return toDel;
    }
    
    String arg2str(Term a) {
        if (a.isString())
            return ((StringTerm)a).getString();
        else 
            return a.toString();
    }

    boolean arg2bool(Term a) {
        if (a.isString())
            return ((StringTerm)a).getString().equals("true");
        else 
            return a.toString().equals("true");
    }

    class CreateGroup extends CartagoAction {
        public void execute(String agName, CAgentArch agent, ICartagoContext ctx, Structure action, ActionExec actionExec){                     
            try {
                // parameters
                String gId = arg2str(action.getTerm(0)); 

                ArtifactId aid = ctx.makeArtifact(
                        gId,   
                        GroupBoard.class.getName(),  
                        new ArtifactConfig( gId, arg2str(action.getTerm(1)), arg2str(action.getTerm(2)), arg2bool(action.getTerm(3)), arg2bool(action.getTerm(4))));
                ctx.focus(aid, null);

                notifyActionSuccess(agent, actionExec);
            } catch (CartagoException e) {
                notifyActionFailure(agent,actionExec); 
                logger.log(Level.SEVERE,"Cartago error: "+e, e);
            }
        }
    }

    class RemoveOrgArt extends CartagoAction {
        public void execute(String agName, CAgentArch agent, final ICartagoContext ctx, Structure action, ActionExec actionExec){                     
            try {
                String aId = arg2str(action.getTerm(0)); 

                final ArtifactId aid = ctx.lookupArtifact(aId);
                if (aid == null){
                    logger.warning("dispose by "+agName+" failed - artifact not found: "+aId);
                    notifyActionFailure(agent,actionExec);
                } else {
                    PendingAction act = agent.createPendingAction(agent, agName, action, actionExec);
                    ctx.use(act.getActionId(),aid,new Op("destroy"),null,Long.MAX_VALUE);
                    
                    // call dispose later
                    getTS().getAg().getScheduler().schedule(new Callable<Object>() {
                        public Object call() throws Exception {
                            ctx.disposeArtifact(aid);
                            return null;
                        }
                    }, 4, TimeUnit.SECONDS);
                }
                notifyActionSuccess(agent, actionExec);
            } catch (Exception e) {
                notifyActionFailure(agent,actionExec); 
                logger.log(Level.SEVERE,"Cartago error: "+e, e);
            }
        }
    }
    
    class CreateScheme extends CartagoAction {
        public void execute(String agName, CAgentArch agent, ICartagoContext ctx, Structure action, ActionExec actionExec){                     
            try {
                // parameters
                String sId = arg2str(action.getTerm(0)); 
                
                ArtifactId aid = ctx.makeArtifact(
                        sId,   
                        SchemeBoard.class.getName(),  
                        new ArtifactConfig( sId, arg2str(action.getTerm(1)), arg2str(action.getTerm(2)), arg2bool(action.getTerm(3)), arg2bool(action.getTerm(4))));
                ctx.focus(aid, null);

                notifyActionSuccess(agent, actionExec);
            } catch (CartagoException e) {
                notifyActionFailure(agent,actionExec); 
                logger.log(Level.SEVERE,"Cartago error: "+e, e);
            }
        }
    }

    // basic class for all other actions
    abstract class OrgAction extends CartagoAction {
        public void execute(String agName, CAgentArch agent, ICartagoContext ctx, Structure action, ActionExec actionExec){                     
            try {
                PendingAction act = agent.createPendingAction(agent, agName, action, actionExec);
                ArtifactId aid = ctx.lookupArtifact( arg2str(action.getTerm( getArtPosition()) ));
                ctx.use(act.getActionId(), aid, getOp(action), null,Long.MAX_VALUE);
                if (focusAfter())
                    ctx.focus(aid,null);

            } catch (Exception e) {
                notifyActionFailure(agent,actionExec); 
                logger.log(Level.SEVERE,"Cartago error: "+e, e);
            }
        }
        abstract Op getOp(Structure action);
        int getArtPosition() { return 1; } // the position of the art id in the parameters
        boolean focusAfter() { return false; }
    }


    class AddResponsibleGroup extends OrgAction {
        Op getOp(Structure action) {
            return new Op("addScheme", arg2str(action.getTerm(0)));
        }
    }
    
    class AdoptRole extends OrgAction {
        Op getOp(Structure action) {
            return new Op("adoptRole", arg2str(action.getTerm(0)));
        }
        boolean focusAfter() { 
            return true; 
        }
    }
    
    class LeaveRole extends OrgAction {
        Op getOp(Structure action) {
            return new Op("leaveRole", arg2str(action.getTerm(0)));
        }
    }

    class CommitMission extends OrgAction {
        Op getOp(Structure action) {
            return new Op("commitMission", arg2str(action.getTerm(0)));
        }
        boolean focusAfter() { 
            return true; 
        }
    }

    class LeaveMission extends OrgAction {
        Op getOp(Structure action) {
            return new Op("leaveMission", arg2str(action.getTerm(0)));
        }
    }

    class GoalAchieved extends OrgAction {
        Op getOp(Structure action) {
            return new Op("goalAchieved", arg2str(action.getTerm(0)));
        }
    }

    class SetGoalArgValue extends OrgAction {
        Op getOp(Structure action) {
            return new Op("setArgumentValue", arg2str(action.getTerm(1)), arg2str(action.getTerm(2)), arg2str(action.getTerm(3)));
        }
        int getArtPosition() {
            return 0;
        }
    }

    
    /*
    private void resumeIntention(Event ev) {
        // get intention's key
        String intentionKey = null;
        String evKey = String.valueOf(ev.getSourceId().getId() + ev.getRelatedOpId().getOpName());
        for (String k: getTS().getC().getPendingIntentions().keySet()) {
            if (k.startsWith(evKey)) {
                intentionKey = k;
                break;
            }
        }
        
        // resume/fail intention
        if (intentionKey != null) {
            if (ev.getContent().getLabel().equals("op_exec_completed")) {
                ConcurrentInternalAction.resume(getTS(), intentionKey, false, null);                    
            } else if (ev.getContent().getLabel().equals(OrgArt.sglNormFailure)) { // fail the intention                    
                Literal reason = ((JasonLiteralWrapper)ev.getContent(0)).getLiteral();
                List<Term> failAnnots = JasonException.createBasicErrorAnnots( "norm_failure", "Error resuming pending intention related to organisational action: "+ev);
                failAnnots.add(ASSyntax.createStructure("norm_failure", reason.getTerm(0)));
                ConcurrentInternalAction.resume(getTS(), intentionKey, true, failAnnots);  
            } else if (ev.getContent().getLabel().equals("op_exec_failed")) { // fail the intention
                ConcurrentInternalAction.resume(getTS(), intentionKey, true, JasonException.createBasicErrorAnnots( "fail_resume", "Error resuming pending intention related to organisational action"));                    
            }
        }
    }
    */
    
}
