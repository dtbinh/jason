package jia;

import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import arch.LocalWorldModel;
import busca.AEstrela;
import busca.Busca;
import busca.Estado;
import busca.Heuristica;
import busca.Nodo;
import env.WorldModel;

public class Search {
    
	final LocalWorldModel model;
    final Location        from, to;
    final boolean         considerAgentsAsObstacles;
	final boolean         corralIsTarget; 
    int[]                 actionsOrder;    
    int                   nbStates = 0;
    
    static final int[] defaultActions = { 1, 2, 3, 4, 5, 6, 7, 8 }; // initial order of actions

    Logger logger = Logger.getLogger(Search.class.getName());
    
    Search(LocalWorldModel m, Location from, Location to, int[] actions, boolean considerAgentsAsObstacles) {
    	this.model = m;
    	this.from  = from;
    	this.to    = to;
    	this.considerAgentsAsObstacles = considerAgentsAsObstacles;
    	if (actions != null) {
    		this.actionsOrder = actions;
    	} else {
    		this.actionsOrder = defaultActions;
    	}
    	corralIsTarget = model.inCorral(to); 
    }

    /** used normally to discover the distance from 'from' to 'to' */
    Search(LocalWorldModel m, Location from, Location to) {
    	this(m,from,to,null,false);
    }
    
    public Nodo search() throws Exception { 
    	Busca searchAlg = new AEstrela();
    	//searchAlg.ssetMaxAbertos(1000);
    	GridState root = new GridState(from, WorldModel.Move.skip, this);
    	root.setIsRoot();
        return searchAlg.busca(root);
    }
    
    public WorldModel.Move firstAction(Nodo solution) {
	    Nodo root = solution;
	    Estado prev1 = null;
	    Estado prev2 = null;
	    while (root != null) {
	        prev2 = prev1;
	        prev1 = root.getEstado();
	        root = root.getPai();
	    }
	    if (prev2 != null) {
	        return ((GridState)prev2).op;
	    }
	    return null;
    }

    // test
    /*
    public static  void main(String[] a) throws Exception {
    	System.out.println("init");
    	Location pos = new Location(2,2);
    	
    	Search ia = new Search(WorldFactory.world9(), pos, new Location(40,40));

    	List<GridState> options = new ArrayList<GridState>(4);
    	options.add(new GridState(new Location(pos.x,pos.y+1),"down", ia));
    	options.add(new GridState(new Location(pos.x-1,pos.y), "left", ia));
    	options.add(new GridState(new Location(pos.x,pos.y-1),"up", ia));
    	options.add(new GridState(new Location(pos.x+1,pos.y),"right", ia));
    	//ia.model.incVisited(options.get(0).pos);
    	ia.model.incVisited(options.get(3).pos);
    	ia.model.incVisited(options.get(2).pos);
    	ia.model.incVisited(options.get(2).pos);
    	ia.model.incVisited(options.get(1).pos);
    	ia.model.incVisited(options.get(1).pos);
    	System.out.println(options);
    	for (GridState l: options) {
    		System.out.println(l+"="+ia.model.getVisited(l.pos));
    	}
    	Collections.sort(options, new VisitedComparator(ia.model));
    	System.out.println(options);

    	Nodo solution = ia.search();
    	System.out.println("action = "+ia.firstAction(solution) + ", path size = "+ solution.getProfundidade());
    	//System.out.println(solution.montaCaminho());
    }
    */
}


final class GridState implements Estado, Heuristica {

    // State information
    final Location        pos; // current location
    final WorldModel.Move op;
    final Search          ia;
    final int             hashCode;
    boolean               isRoot = false;
    
    public GridState(Location l, WorldModel.Move op, Search ia) {
        this.pos = l;
        this.op  = op;
        this.ia  = ia;
        hashCode = pos.hashCode();
        
        ia.nbStates++;
    }
    
    public void setIsRoot() {
    	isRoot = true;
    }
    
    public int custo() {
        return 1;
    }

    public boolean ehMeta() {
        return pos.equals(ia.to);
    }

    public String getDescricao() {
        return "Grid search";
    }

    public int h() {
        return pos.distance(ia.to);
    }
    
    public List<Estado> sucessores() {
        List<Estado> s = new ArrayList<Estado>();
        if (ia.nbStates > 50000) {
        	ia.logger.info("*** It seems I am in a loop!");
        	return s; 
        }
                
        // all directions
        for (int a = 0; a < ia.actionsOrder.length; a++) {
        	switch (ia.actionsOrder[a]) {
        	case 1: suc(s,new Location(pos.x-1,pos.y), WorldModel.Move.west); break;
        	case 2: suc(s,new Location(pos.x+1,pos.y), WorldModel.Move.east); break;
        	case 3: suc(s,new Location(pos.x,pos.y-1), WorldModel.Move.north); break;
            case 4: suc(s,new Location(pos.x+1,pos.y-1), WorldModel.Move.northeast); break;
            case 5: suc(s,new Location(pos.x-1,pos.y-1), WorldModel.Move.northwest); break;
        	case 6: suc(s,new Location(pos.x,pos.y+1), WorldModel.Move.south); break;
            case 7: suc(s,new Location(pos.x+1,pos.y+1), WorldModel.Move.southeast); break;
            case 8: suc(s,new Location(pos.x-1,pos.y+1), WorldModel.Move.southwest); break;
        	}
        }
        
        // if it is root state, sort the option by least visited
        if (isRoot) {
        	Collections.sort(s, new VisitedComparator(ia.model));
        }
        return s;
    }
    
    private void suc(List<Estado> s, Location newl, WorldModel.Move op) {

        if (ia.model.isFreeOfObstacle(newl)) {
        	// the depot is an obstacle if not the target.
        	if (ia.corralIsTarget || !ia.model.hasObject(WorldModel.CORRAL, newl)) {
	        	if (ia.considerAgentsAsObstacles) {
	        		if (ia.model.isFree(newl) || ia.from.distance(newl) > 1) {
	        			s.add(new GridState(newl,op,ia));
	        		}
	        	} else {
	                s.add(new GridState(newl,op,ia));
	            }
        	}
    	}
    }
    
    public boolean equals(Object o) {
        if (o != null && o instanceof GridState) {
            GridState m = (GridState)o;
            return pos.equals(m.pos);
        }
        return false;
    }
    
    public int hashCode() {
    	return hashCode;
    }
            
    public String toString() {
        return "(" + pos + "-" + op + ")"; 
    }
}

class VisitedComparator implements Comparator<Estado> {

	LocalWorldModel model;
	VisitedComparator(LocalWorldModel m) {
		model = m;
	}
	
	public int compare(Estado o1, Estado o2) {
		int v1 = model.getVisited(((GridState)o1).pos);
		int v2 = model.getVisited(((GridState)o2).pos);
		if (v1 > v2) return 1;
		if (v2 > v1) return -1;
		return 0;
	}

}
