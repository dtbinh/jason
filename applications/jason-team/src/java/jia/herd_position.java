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
import busca.Nodo;
import env.WorldModel;

/** 
 * Gives a good location to herd cows
 *  
 * @author jomi
 */
public class herd_position extends DefaultInternalAction {
    
    public static final double maxStdDev = 3;
    
    public enum Formation { 
    	one { int[] getAngles() { return new int[] { 0 }; } }, 
    	six { int[] getAngles() { return new int[] { 10, -10, 30, -30, 60, -60 }; } };
    	abstract int[] getAngles();
    };
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
        	CowboyArch arch       = (CowboyArch)ts.getUserAgArch();
            LocalWorldModel model = arch.getModel();
            if (model == null)
            	return false;
            
            Location agLoc        = model.getAgPos(arch.getMyId());

            // update GUI
            if (arch.hasGUI())
            	setFormationLoc(model, Formation.valueOf(terms[0].toString()));

            Location agTarget = getAgTarget(model, Formation.valueOf(terms[0].toString()), agLoc);
            if (agTarget != null) {
            	agTarget = nearFreeForAg(model, agLoc, agTarget);
                return un.unifies(terms[1], new NumberTermImpl(agTarget.x)) && 
                       un.unifies(terms[2], new NumberTermImpl(agTarget.y));
            } else {
            	ts.getLogger().info("No target! I am at "+agLoc+" places are "+formationPlaces(model, Formation.valueOf(terms[0].toString())));
            }
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "herd_position error: "+e, e);    		
        }
        return false;
    }
    
    public Location getAgTarget(LocalWorldModel model, Formation formation, Location ag) throws Exception {
        Location r = null;
        List<Location> locs = formationPlaces(model, formation);
        if (locs != null) {
        	for (Location l : locs) {
        		r = l;
	            if (ag.equals(l) || // I am there
	                model.countObjInArea(WorldModel.AGENT, l, 1) == 0) { // no one else is there
	        		break;
	        	}
	        }
        }
        return r;
    }

    public void setFormationLoc(LocalWorldModel model, Formation formation) throws Exception {
    	model.removeAll(WorldModel.FORPLACE);
        List<Location> locs = formationPlaces(model, formation);
        if (locs != null) {
	        for (Location l : locs) {
	            if (model.inGrid(l)) {
	            	model.add(WorldModel.FORPLACE, l);
	            }
	    	}
        }
    }
    
    private List<Location> formationPlaces(LocalWorldModel model, Formation formation) throws Exception {
        List<Vec> cows = new ArrayList<Vec>();
        for (Location c: model.getCows()) {
            cows.add(new Vec(model, c));
        }
        if (cows.isEmpty())
            return null;
        
        cows = Vec.cluster(cows, 2); // find center/clusterise

        Vec mean = Vec.mean(cows);
        
        // run A* to see the cluster target in n steps
        Search s = new Search(model, mean.getLocation(model), model.getCorralCenter(), null, false, false, false, null);
        List<Nodo> np = s.normalPath(s.search());
        
        int stepsFromCenter = (int)Vec.max(cows).sub(mean).magnitude()+1;
        int n = Math.min(stepsFromCenter, np.size());

        Vec cowstarget = new Vec(model, s.getNodeLocation(np.get(n)));
        Vec agsTarget  = mean.sub(cowstarget);
        Vec agTarget   = agsTarget;
        List<Location> r = new ArrayList<Location>();
        for (int angle: formation.getAngles()) {
        	double nt = angle * (Math.PI / 180);
        	for (double varangle = nt; nt < 180; nt += 5) {
            	agTarget = agsTarget.newAngle(agsTarget.angle() + varangle);
                Location l = agTarget.add(mean).getLocation(model);

                // if l is in the path of cows, continue with next varangle
                boolean inpath = false;
                for (Nodo pn: np) {
                	if (l.equals(s.getNodeLocation(pn))) {
                		inpath = true;
                		break;
                	}
                }
                if (!inpath) {
                	r.add(l);
                	break;
                }
        	}
        }
        return r;    	
    }
    
    public Location nearFreeForAg(LocalWorldModel model, Location ag, Location t) throws Exception {
        // run A* to get the path from ag to t
        Search s = new Search(model, t, ag, null, false, false, true, null);
        List<Nodo> np = s.normalPath(s.search());
    	
        int i = 0;
        for (Nodo n: np) {
        	if (model.isFree(s.getNodeLocation(n))) {
        		return s.getNodeLocation(n);
        	}
        	i++;
        	if (i > 3) // do not go to far from target
        		break;
        }
        return model.nearFree(t);
    }
}

