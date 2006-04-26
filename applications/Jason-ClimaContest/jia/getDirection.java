package jia;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jasonteam.Location;
import jasonteam.WorldModel;

import java.util.ArrayList;
import java.util.List;

import busca.AEstrela;
import busca.Estado;
import busca.Heuristica;
import busca.Nodo;

public class getDirection implements InternalAction {
	
	public boolean execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
		try {
            String sAction = "skip";

            WorldModel model = WorldModel.get();
	
			NumberTerm agx = (NumberTerm)terms[0].clone(); un.apply((Term)agx);
			NumberTerm agy = (NumberTerm)terms[1].clone(); un.apply((Term)agy);
			NumberTerm tox = (NumberTerm)terms[2].clone(); un.apply((Term)tox);
			NumberTerm toy = (NumberTerm)terms[3].clone(); un.apply((Term)toy);
			int iagx = (int)agx.solve();
			int iagy = (int)agy.solve();
			int itox = (int)tox.solve();
			int itoy = (int)toy.solve();
			if (itox < model.getWidth() && itoy < model.getHeight()) {
    			AEstrela searchAlg = new AEstrela();
    			searchAlg.setQuieto(true);
    			//searchAlg.setMaxF(20);
    			//System.out.println("-- from "+iagx+","+iagy+" to "+tox+","+toy);
    			Nodo solution = searchAlg.busca(new MinerState(new Location(iagx, iagy), new Location(itox, itoy), model, "initial"));
    			//if (solution == null) {
    			//	solution = searchAlg.getTheBest();
    			//}
    			Nodo root = solution;
    			Estado prev1 = null;
    			Estado prev2 = null;
    			while (root != null) {
    				prev2 = prev1;
    				prev1 = root.getEstado();
    				root = root.getPai();
    			}
    			if (prev2 != null) {
    				//System.out.println("-- "+solution.montaCaminho());
    				sAction =  ((MinerState)prev2).op;
    			} 
            }
			return un.unifies(terms[4], new Term(sAction));
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	// just for testing 
	public static void main(String[] a) {
		WorldModel model = WorldModel.create(100,100,4);
		model.add(WorldModel.OBSTACLE, 1,1);
		model.add(WorldModel.OBSTACLE, 2,1);
		model.add(WorldModel.OBSTACLE, 2,2);

		model.add(WorldModel.EMPTY, 2,0);
		model.add(WorldModel.EMPTY, 3,0);
		model.add(WorldModel.EMPTY, 3,1);
		model.add(WorldModel.EMPTY, 3,2);
		
		MinerState initial = new MinerState(new Location(19, 17), new Location(5, 7), model, "initial");
		AEstrela searchAlg = new AEstrela();
		searchAlg.setQuieto(false);
		Nodo solution = searchAlg.busca(initial);
		System.out.println("Path="+solution.montaCaminho()+ "\ncost ="+solution.g());
		Nodo root = solution;
		Estado prev1 = null;
		Estado prev2 = null;
		while (root != null) {
			prev2 = prev1;
			prev1 = root.getEstado();
			root = root.getPai();
		}
		System.out.println("Action = "+ ((MinerState)prev2).op);
	}
	
}


class MinerState implements Estado, Heuristica {

	// State information
	Location l; // current location
	Location to;
	String op;
	int cost;
	WorldModel model;
	
	public MinerState(Location l, Location to, WorldModel model, String op) {
		this.l = l;
		this.to = to;
		this.model = model;
		this.op = op;
		this.cost = 3;
		if (model.isUnknown(l)) this.cost = 2; // unknown places are preferable
	}
	
	public int custo() {
		return cost;
	}

	public boolean ehMeta() {
		return l.equals(to);
	}

	public String getDescricao() {
		return "Jason team miners search";
	}

	public int h() {
		return l.distance(to) * 3;
	}

	public List sucessores() {
		List s = new ArrayList(4);
		// four directions
		suc(s,new Location(l.x,l.y-1),"up");
		suc(s,new Location(l.x,l.y+1),"down");
		suc(s,new Location(l.x-1,l.y),"left");
		suc(s,new Location(l.x+1,l.y),"right");
		return s;
	}
	
	private void suc(List s, Location newl, String op) {
		if (model.isFree(newl) || (newl.equals(to) && model.isFreeOfObstacle(newl))) {
			s.add(new MinerState(newl,to,model,op));
		}
	}
	
    public boolean equals(Object o) {
        try {
            MinerState m = (MinerState)o;
            return l.equals(m);
        } catch (Exception e) {}
        return false;
    }
    
    public int hashCode() {
        return l.toString().hashCode();
    }
            
	public String toString() {
		return "(" + l + "-" + op + "/" + cost + ")"; 
	}
}
