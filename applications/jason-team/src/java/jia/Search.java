package jia;

import jason.architecture.AgArch;
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
    final boolean         considerCorralAsObstacles;
    final boolean         considerCowsAsObstacles;
    WorldModel.Move[]     actionsOrder;    
    int                   nbStates = 0;
    AgArch                agArch;
    
    public static final WorldModel.Move[] defaultActions = {  // initial order of actions
              WorldModel.Move.west,
              WorldModel.Move.east,
              WorldModel.Move.north,
              WorldModel.Move.northeast,
              WorldModel.Move.northwest,
              WorldModel.Move.south,
              WorldModel.Move.southeast,
              WorldModel.Move.southwest
    }; 

    Logger logger = Logger.getLogger(Search.class.getName());
    
    public Search(LocalWorldModel m, Location from, Location to, WorldModel.Move[] actions, boolean considerAgentsAsObstacles, boolean considerCorralAsObstacles, boolean considerCowsAsObstacles, AgArch agArch) {
    	this.model = m;
    	this.from  = from;
    	this.to    = to;
    	this.considerAgentsAsObstacles = considerAgentsAsObstacles;
        this.considerCorralAsObstacles = considerCorralAsObstacles;
        this.considerCowsAsObstacles   = considerCowsAsObstacles;
    	this.agArch = agArch;
    	if (actions != null) {
    		this.actionsOrder = actions;
    	} else {
    		this.actionsOrder = defaultActions;
    	}
    }

    /** used normally to discover the distance from 'from' to 'to' */
    Search(LocalWorldModel m, Location from, Location to, AgArch agArch) {
    	this(m,from,to,null,false, false, false, agArch);
    }
    
    public Nodo search() throws Exception { 
    	Busca searchAlg = new AEstrela();
    	//searchAlg.ssetMaxAbertos(1000);
    	GridState root = new GridState(from, WorldModel.Move.skip, this, 1);
    	root.setAsRoot();
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
    int                   cost = 1;
    
    public GridState(Location l, WorldModel.Move op, Search ia, int cost) {
        this.pos  = l;
        this.op   = op;
        this.ia   = ia;
        this.cost = cost;
        hashCode  = pos.hashCode();
        
        ia.nbStates++;
    }
    
    public void setAsRoot() {
    	isRoot = true;
    }
    
    public int custo() {
        return cost;
    }

    public boolean ehMeta() {
        return pos.equals(ia.to);
    }

    public String getDescricao() {
        return "Grid search";
    }

    public int h() {
        return pos.maxBorder(ia.to);
    }
    
    public List<Estado> sucessores() {
        List<Estado> s = new ArrayList<Estado>();
        if (ia.nbStates > 50000) {
        	ia.logger.info("*** It seems I am in a loop!");
        	return s; 
        } else if (ia.agArch != null && !ia.agArch.isRunning()) {
            return s;
        }
                
        // all directions
        for (int a = 0; a < ia.actionsOrder.length; a++) {
            suc(s, WorldModel.getNewLocationForAction(pos, ia.actionsOrder[a]), ia.actionsOrder[a]);
        }
        
        // if it is root state, sort the option by least visited
        if (isRoot) {
        	Collections.sort(s, new VisitedComparator(ia.model));
        }
        return s;
    }

    private void suc(List<Estado> s, Location newl, WorldModel.Move op) {

        if (!ia.model.inGrid(newl))
            return;
        if (ia.model.hasObject(WorldModel.OBSTACLE, newl)) 
            return;
        if (ia.considerCorralAsObstacles && ia.model.hasObject(WorldModel.CORRAL, newl)) 
            return;
        if (ia.considerAgentsAsObstacles && ia.model.hasObject(WorldModel.AGENT,newl) && ia.from.maxBorder(newl) <= 2) 
            return;
        if (ia.considerCowsAsObstacles   && ia.model.hasObject(WorldModel.COW,newl)   && ia.from.maxBorder(newl) <= 2) 
            return;

        int cost = 1;
        
        if (ia.considerCowsAsObstacles) { 
            cost += countCowsBeside(newl,1);
            cost += countCowsBeside(newl,2);
        }

        s.add(new GridState(newl,op,ia, cost));
    }
    
    private int countCowsBeside(Location l, int d) {
        int c = 0;
        for (int x = l.x-d; x <= l.x+d; x++) {
            for (int y = l.y-d; y <= l.y+d; y++) {
                if (ia.model.hasObject(WorldModel.COW, x, y)) {
                    c++;
                }
            }
        }
        return c;
        /*
        if (ia.model.hasObject(WorldModel.COW, l.x, l.y)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x+1, l.y)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x-1, l.y)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x, l.y+1)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x, l.y-1)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x+1, l.y+1)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x-1, l.y+1)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x+1, l.y-1)) c++;
        if (ia.model.hasObject(WorldModel.COW, l.x-1, l.y-1)) c++;
        */
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
