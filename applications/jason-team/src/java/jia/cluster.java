package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import arch.CowboyArch;
import arch.LocalWorldModel;
import env.WorldModel;

/** 
 * Computes a cluster of cows for the agent
 * 
 * @author jomi
 */
public class cluster extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	CowboyArch arch = (CowboyArch)ts.getUserAgArch();
        	LocalWorldModel model = arch.getModel();
            if (model == null)
            	return false;
            //Location agLoc = model.getAgPos(arch.getMyId());

        	List<Location> locs = getCluster(model, WorldModel.cowPerceptionRatio);
        	
        	if (args.length == 1) {
                return un.unifies(args[0], new ObjectTermImpl(locs));        	    
        	} else {
        		ListTerm r = new ListTermImpl();
        		ListTerm tail = r;
        		for (Location l: locs) {
        			Structure p = new Structure("pos",2);
        			p.addTerms(new NumberTermImpl(l.x), new NumberTermImpl(l.y));
        			tail = tail.append(p);
        		}
                return un.unifies(args[0], new ObjectTermImpl(locs)) && 
                       un.unifies(args[1], r);               
        	}
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "cluster error: "+e, e);    		
        }
        return false;
    }
    
    public static List<Location> getCluster(LocalWorldModel model, int maxDist) {
    	/*
			Vs = set of all seen cows (sorted by distance to the centre of cluster)
			Cs  = { the cow near to the center of Vs }

			add = true
			while (add)
  				add = false
  				for all v in Vs
    				if (some cow in Cs sees v)
      					move v from Vs to Cs
      					add = true
    	*/
        Collection<Vec> cows = model.getCows();
    	Vec mean = Vec.mean( cows );
    	List<Vec> vs = new ArrayList<Vec>();
    	// place all cows in ref to mean
    	for (Vec v: cows)
    		vs.add(v.sub(mean));
    	
    	Collections.sort(vs);

    	List<Vec> cs = new ArrayList<Vec>();
    	if (!vs.isEmpty()) 
    		cs.add(vs.remove(0));
    	
    	boolean add = true;
    	while (add) {
    		add = false;
    		Iterator<Vec> i = vs.iterator();
    		while (i.hasNext()) {
        		Vec v = i.next();
        		
        		Iterator<Vec> j = cs.iterator();
        		while (j.hasNext()) {
            		Vec c = j.next();
        			if (c.sub(v).magnitude() < maxDist) {
        				cs.add(v);
        				i.remove();
        				add = true;
        				break;
        			}
        		}
    		}
    	}
        List<Location> clusterLocs = new ArrayList<Location>();
        for (Vec v: cs) {
            // place all cows in ref to 0,0
            clusterLocs.add(v.add(mean).getLocation(model));
        }
    	
        return clusterLocs;
    }
    
    public static List<Vec> location2vec(LocalWorldModel model, List<Location> clusterLocs) {
        List<Vec> cows = new ArrayList<Vec>();
        for (Location l: clusterLocs) 
            cows.add( new Vec(model, l));
        return cows;
    }
}

