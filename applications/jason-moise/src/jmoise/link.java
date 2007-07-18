package jmoise;

import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import moise.oe.GroupInstance;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.RolePlayer;
import moise.os.ss.Link;
import moise.os.ss.RoleRel.RoleRelScope;

public class link extends MoiseBaseIA {

    private static Logger logger = Logger.getLogger(link.class.getName());

    // it backtracks on first and third args
    
    @Override
    public Object execute(TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        try {
            // get OE
            OE currentOE = ((OrgAgent)ts.getUserAgArch()).getOE();
            if (currentOE == null) {
                logger.warning("I still do not have a copy of the organisational entity!");
                return false;
            }
            
            // get OEAgents
            OEAgent agSource = currentOE.getAgent(args[1].toString());
            if (agSource == null) {
                logger.warning("the source of the link '"+args[1]+"' does not exists! (agents="+currentOE.getAgents()+")");
                return false;
            }

            // get link type
            String linkType = null;
            if (!args[0].isVar()) {
                linkType = args[0].toString();
            }
            
            if (args[0].isVar() || args[2].isVar()) {
                // backtrack for all links to target
                // (create a list of all possible unifications)
                List<Unifier> resultList = new ArrayList<Unifier>();
                for (RolePlayer rp: agSource.getRoles()) { // all roles
                    for (Link link: rp.getLinks(linkType)) { // all links of this role
                        GroupInstance gr = null;
                        if (link.getScope() == RoleRelScope.IntraGroup) {
                            gr = rp.getGroup();
                        }
                        for (OEAgent ag: currentOE.getAgents( gr, link.getTarget())) { // all agents of the target role
                            Unifier c = (Unifier)un.clone();

                            // unifies the type and target
                            if (c.unifies(args[0], new Atom(link.getTypeStr())) && 
                                c.unifies(args[2], new Atom(ag.getId()))) {
                                resultList.add(c);
                            }
                        }
                    }
                }
                return resultList.iterator();

            } else {
                // simply test if the link exists
                OEAgent agTarget = currentOE.getAgent(args[2].toString());
                if (agTarget == null) {
                    logger.warning("the target of the link '"+args[2]+"' does not exists! (agents="+currentOE.getAgents()+")");
                    return false;
                }
    
                // test link
                return agSource.hasLink(linkType, agTarget);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error at jmoise.link.",e);
        }
        return false;
    }
}
