package jia;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
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
    			Nodo solution = searchAlg.busca(new MinerState(iagx, iagy, itox, itoy, model, "initial"));
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
		
		MinerState initial = new MinerState(19, 17, 5, 7, model, "initial");
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
	int x,y;
	String op;
	int cost;
	int tox, toy;
	WorldModel model;
	
	public MinerState(int x, int y, int tox, int toy, WorldModel model, String op) {
		this.x = x;
		this.y = y;
		this.tox = tox;
		this.toy = toy;
		this.model = model;
		this.op = op;
		this.cost = 3;
		if (model.isUnknown(x,y)) this.cost = 2; // unknown places are preferable
	}
	
	public int custo() {
		return cost;
	}

	public boolean ehMeta() {
		return x == tox && y == toy;
	}

	public String getDescricao() {
		return "Jason team miners search";
	}

	public int h() {
		return (Math.abs(x - tox) + Math.abs(y - toy)) * 3;
	}

	public List sucessores() {
		List s = new ArrayList(4);
		// four directions
		if (model.isFree(x,y-1)) {
			s.add(new MinerState(x,y-1,tox,toy,model,"up"));
		}
		if (model.isFree(x,y+1)) {
			s.add(new MinerState(x,y+1,tox,toy,model,"down"));
		}
		if (model.isFree(x-1,y)) {
			s.add(new MinerState(x-1,y,tox,toy,model, "left"));
		}
		if (model.isFree(x+1,y)) {
			s.add(new MinerState(x+1,y,tox,toy,model, "right"));
		}
		return s;
	}
	
    public boolean equals(Object o) {
        try {
            MinerState m = (MinerState)o;
            return x == m.x && y == m.y;
        } catch (Exception e) {}
        return false;
    }
    
    public int hashCode() {
        return (x + "," + y).hashCode();
    }
            
	public String toString() {
		return "(" + x + "," + y + "-" + op + "/" + cost + ")"; 
	}
}
