package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import arch.CowboyArch;
import arch.LocalWorldModel;
import env.WorldModel;

/** 
 * Gives a good location to herd cows
 *  
 * @author jomi
 */
public class herd_position extends DefaultInternalAction {
    
    public static final double maxStdDev = 3;
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            
            LocalWorldModel model = ((CowboyArch)ts.getUserAgArch()).getModel();
            
            
            Location agTarget = getAgTarget(model);
            if (agTarget != null) {
                agTarget = model.nearFree(agTarget);
                return un.unifies(terms[0], new NumberTermImpl(agTarget.x)) && 
                       un.unifies(terms[1], new NumberTermImpl(agTarget.y));
            }
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "herd_position error: "+e, e);    		
        }
        return false;
    }
    
    public Location getAgTarget(LocalWorldModel model) throws Exception {
        List<Vec> cowsTarget = new ArrayList<Vec>();
        for (Location c: model.getCows()) {
            Search s = new Search(model, c, model.getCorralCenter(), null, false, false, false, null);
            Location cowTarget = WorldModel.getNewLocationForAction(c, s.firstAction(s.search()));
            cowsTarget.add(new Vec(model, cowTarget));
        }
        
        Vec stddev = Vec.stddev(cowsTarget);
        
        // remove max if stddev is too big
        while (stddev.magnitude() > maxStdDev) {
            cowsTarget.remove(Vec.max(cowsTarget));
            stddev = Vec.stddev(cowsTarget);
        }
        
        Vec mean = Vec.mean(cowsTarget);
        if (mean.magnitude() > 0) {
            double incvalue = (Vec.max(cowsTarget).sub(mean).magnitude()+2) / mean.magnitude();
            return mean.product(incvalue+1).getLocation(model);
        } else {
            return null;
        }
    }
}

