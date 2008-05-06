package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.Structure;
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
 * the first argument is the formation id (one, two, ... 1, 2, ....)
 * 
 * the second is the cluster [ pos(X,Y), ..... ] (the term returned by jia.cluster)
 * 
 * if it is called with 3 args, returns in the thrid arg the formation, a list
 * in the format [pos(X,Y), .....]
 * 
 * if it is called with 4 args, returns the a free location in the formation
 * otherwise (3rd is X and 4th is Y)
 *  
 * @author jomi
 */
public class herd_position extends DefaultInternalAction {
    
    public static final int    agDistanceInFormation = 4;

    public enum Formation { 
    	one   { int[] getDistances() { return new int[] { 0 }; } }, 
    	two   { int[] getDistances() { return new int[] { sd, -sd }; } },
    	three { int[] getDistances() { return new int[] { 0, d, -d }; } },
    	four  { int[] getDistances() { return new int[] { sd, -sd, d+sd, -(d+sd) }; } },
    	five  { int[] getDistances() { return new int[] { 0, d, -d, d*2, -d*2 }; } },
    	six   { int[] getDistances() { return new int[] { sd, -sd, d+sd, -(d+sd), d*2+sd, -(d*2+sd) }; } };
    	abstract int[] getDistances();
    	private static final int d  = agDistanceInFormation;
    	private static final int sd = agDistanceInFormation/2;
    };
    
    LocalWorldModel model;
    List<Location> lastCluster = null;

    public void setModel(LocalWorldModel model) {
    	this.model = model;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	CowboyArch arch       = (CowboyArch)ts.getUserAgArch();
        	model = arch.getModel();
            if (model == null)
            	return false;
            Location agLoc = model.getAgPos(arch.getMyId());

            // identify the formation id
            Formation formation = Formation.six;
            if (args[0].isNumeric()) {
            	int index = (int)((NumberTerm)args[0]).solve();
            	formation = Formation.values()[ index-1 ];
            } else {
            	formation = Formation.valueOf(args[0].toString());
            }
            
            // get the cluster
            List<Location> clusterLocs = (List<Location>)((ObjectTerm)args[1]).getObject();
            
            // update GUI
            if (arch.hasGUI())
            	setFormationLoc(clusterLocs, formation);

            // if the return is a location for one agent
            if (args.length == 4) {
	            Location agTarget = getAgTarget(clusterLocs, formation, agLoc);
	            if (agTarget != null) {
	                return un.unifies(args[2], new NumberTermImpl(agTarget.x)) && 
	                       un.unifies(args[3], new NumberTermImpl(agTarget.y));
	            } else {
	            	ts.getLogger().info("No target! I am at "+agLoc+" places are "+formationPlaces(clusterLocs, formation)+" cluster is "+lastCluster);
	            }
            } else {
            	// return all the locations for the formation
            	List<Location> locs = formationPlaces(clusterLocs, formation);
            	if (locs != null && locs.size() > 0) {
            		ListTerm r = new ListTermImpl();
            		ListTerm tail = r;
            		for (Location l: locs) {
            			Structure p = new Structure("pos",2);
            			p.addTerms(new NumberTermImpl(l.x), new NumberTermImpl(l.y));
            			tail = tail.append(p);
            		}
            		return un.unifies(args[2], r);
            	} else {
	            	ts.getLogger().info("No possible formation! I am at "+agLoc+" places are "+formationPlaces(clusterLocs, formation));            		
            	}
            }
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "herd_position error: "+e, e);    		
        }
        return false;
    }
    
    public Location getAgTarget(List<Location> clusterLocs, Formation formation, Location ag) throws Exception {
        Location r = null;
        List<Location> locs = formationPlaces(clusterLocs, formation);
        if (locs != null) {
        	for (Location l : locs) {
	            if (ag.equals(l) || // I am there
	                //model.countObjInArea(WorldModel.AGENT, l, 1) == 0) { // no one else is there
	            	!model.hasObject(WorldModel.AGENT, l)) {
	        		r = l;
	        		break;
	        	}
	        }
        }
        if (r != null)
        	r = model.nearFree(r);
        return r;
    }

    public void setFormationLoc(List<Location> clusterLocs, Formation formation) throws Exception {
    	model.removeAll(WorldModel.FORPLACE);
        List<Location> locs = formationPlaces(clusterLocs, formation);
        if (locs != null) {
	        for (Location l : locs) {
	            if (model.inGrid(l)) {
	            	model.add(WorldModel.FORPLACE, l);
	            }
	    	}
        }
    }
    
    public List<Location> formationPlaces(List<Location> clusterLocs, Formation formation) throws Exception {
        lastCluster = clusterLocs;

        List<Vec> cows = cluster.location2vec(model, clusterLocs);

        if (cows.isEmpty())
            return null;
        
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
        	//Location l = agTarget.add(mean).getLocation(model);
        	
        	//System.out.println(".......  "+dist+" antes angle "+agTarget);
        	if (dist >= 0)
        		agTarget = agTarget.turn90CW();
        	else
        		agTarget = agTarget.turn90ACW();
        	
        	Location l = findFirstFreeLocTowardsTarget(agTarget, mean.add(agsTarget), initAgTS, dist, model);
        	//System.out.println(" =       "+dist+" result  "+l);
        	if (l != null) {
        	    l = pathToNearCow(l, clusterLocs);
        	    if ( ! model.inGrid(l) || model.hasObject(WorldModel.OBSTACLE, l))
        	        l = model.nearFree(l);
                r.add( l );
        	}
        	
        	/*
        	Location lastloc = null;
        	boolean  uselast = false;
        	for (int agTargetSize = initAgTS; agTargetSize <= Math.abs(dist); agTargetSize++) {
        		l = agTarget.newMagnitude(agTargetSize).add(mean).add(agsTarget).getLocation(model);
        		//System.out.println("pos angle "+agTargetSize);
            	uselast = (!model.inGrid(l) || model.hasObject(WorldModel.OBSTACLE, l)) && lastloc != null; 
        		if (uselast) {
                	r.add(pathToNearCow(model, lastloc));
        			break;
        		}
        		lastloc = l;
        	}
        	if (!uselast)
        		r.add(pathToNearCow(model, l));
        	*/
        	
        	if (dist < 0)
        		initAgTS = Math.abs(dist)+1;
        }
        //System.out.println("all places "+r);
        return r;
    }
    
    public static Location findFirstFreeLocTowardsTarget(Vec target, Vec ref, int initialSize, int maxSize, LocalWorldModel model) {
    	Location lastloc = null;
    	maxSize = Math.abs(maxSize);
    	Location l = ref.getLocation(model);
    	for (int s = initialSize; s <= maxSize; s++) {
    		l = target.newMagnitude(s).add(ref).getLocation(model);
    		//System.out.println("pos angle "+s+" = "+l);
        	if ( (!model.inGrid(l) || model.hasObject(WorldModel.OBSTACLE, l)) && lastloc != null)
        		return lastloc;
    		lastloc = l;
    	}
    	return l; //ref.getLocation(model); //target.add(ref).getLocation(model);
    }
    
    private Location pathToNearCow(Location t, List<Location> cluster) {
    	Location near = null;
        for (Location c: cluster) {
        	if (near == null || t.maxBorder(c) < t.maxBorder(near))
        		near = c;
        }
        if (near != null) {
        	Vec nearcv = new Vec(model,near);
        	Vec dircow = new Vec(model,t).sub(nearcv);
        	//System.out.println("Near cow to "+t+" is "+near+" vec = "+dircow);
        	for (int s = 1; s <= 20; s++) {
        		Location l = dircow.newMagnitude(s).add(nearcv).getLocation(model);
        		if (!model.hasObject(WorldModel.COW,l))
        			return l;
        	}
        }
    	return t;
    }
    
	/*
    public Location nearFreeForAg(LocalWorldModel model, Location ag, Location t) throws Exception {
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
        return model.nearFree(t);
    }
        */
}

