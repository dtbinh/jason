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
    	one { int[] getDistances() { return new int[] { 0 }; } }, 
    	six { int[] getDistances() { return new int[] { 2, -2, 6, -6, 10, -10 }; } };
    	abstract int[] getDistances();
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
        int stepsFromCenter = (int)Math.round(Vec.max(cows).sub(mean).magnitude())+1;
        //Vec max = Vec.max(cows);
        
        // run A* to see the cluster target in n steps
        Search s = new Search(model, mean.getLocation(model), model.getCorralCenter(), null, false, false, false, true, null);
        s.setMaxDistFromCluster(stepsFromCenter+Search.DIST_FOR_AG_OBSTACLE);
        List<Nodo> np = s.normalPath(s.search());
        int n = Math.min(stepsFromCenter, np.size());

        Vec cowstarget = new Vec(model, s.getNodeLocation(np.get(n)));
        Vec agsTarget  = mean.sub(cowstarget);
        List<Location> r = new ArrayList<Location>();
        int initAgTS = 1;
        for (int dist: formation.getDistances()) { // 2, -2, 6, -6, ....
        	Vec agTarget = agsTarget;
        	Location l = agTarget.add(mean).getLocation(model);
        	
        	//System.out.println(".......  "+dist+" antes angle "+agTarget);
        	if (dist >= 0)
        		agTarget = new Vec( -agTarget.y, agTarget.x);
        	else
        		agTarget = new Vec( agTarget.y, -agTarget.x);
        	
        	Location lastloc = null;
        	boolean  uselast = false;
        	for (int agTargetSize = initAgTS; agTargetSize <= Math.abs(dist); agTargetSize++) {
        		l = agTarget.newMagnitude(agTargetSize).add(mean).add(agsTarget).getLocation(model);
        		//System.out.println("pos angle "+agTargetSize);
            	uselast = !model.inGrid(l) || model.hasObject(WorldModel.OBSTACLE, l) && lastloc != null; 
        		if (uselast) {
                	r.add(pathToNearCow(model, lastloc));
        			break;
        		}
        		lastloc = l;
        	}
        	if (!uselast)
        		r.add(pathToNearCow(model, l));
        	if (dist < 0)
        		initAgTS = Math.abs(dist)+1;
        }
        //System.out.println("all places "+r);
        return r;
    }
    
    private Location pathToNearCow(LocalWorldModel model, Location t) {
    	Location near = null;
        for (Location c: model.getCows()) {
        	if (near == null || t.maxBorder(c) < t.maxBorder(near))
        		near = c;
        }
        if (near != null) {
        	Vec nearcv = new Vec(model,near);
        	Vec dircow = new Vec(model,t).sub(nearcv);
        	//System.out.println("Near cow to "+t+" is "+near+" vec = "+dircow);
        	for (int s = 1; s <= 20; s++) {
        		Location l = dircow.newMagnitude(s).add(nearcv).getLocation(model);
        		if (model.isFree(l))
        			return l;
        	}
        }
    	return t;
    }
    
    public Location nearFreeForAg(LocalWorldModel model, Location ag, Location t) throws Exception {
    	/*
        // run A* to get the path from ag to t
    	if (! model.inGrid(t))
    		t = model.nearFree(t);
    	
        Search s = new Search(model, ag, t, null, true, true, true, false, null);
        List<Nodo> np = s.normalPath(s.search());
    	
        int i = 0;
        ListIterator<Nodo> inp = np.listIterator(np.size());
        while (inp.hasPrevious()) {
        	Nodo n = inp.previous();
        	if (model.isFree(s.getNodeLocation(n))) {
        		return s.getNodeLocation(n);
        	}
        	if (i++ > 3) // do not go to far from target
        		break;
        }
        */
        return model.nearFree(t);
    }
}

