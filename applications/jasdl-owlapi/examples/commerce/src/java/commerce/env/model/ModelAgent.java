package commerce.env.model;

import jasdl.util.exception.JASDLException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import commerce.env.CommerceEnvironment;

public class ModelAgent extends ModelObject {

	public ModelAgent(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
	}
	
	public void addPercepts() throws JASDLException{
				
		Set<ModelObject> inVicinityList = model.getObjectsInVicinityOfPosition(getPosition());
		// don't want to add atLocation(me, me)
		inVicinityList.remove((ModelAgent)this);
		
		// add my x and y coords
		env.addPercept(getId().toString(), Literal.parseLiteral("hasPosition("+getId()+","+getPosition().x+","+getPosition().y+")"));
		
		for(ModelObject inVicinity : inVicinityList){    				
			env.addPercept(getId().toString(), Literal.parseLiteral("inVicinityOf("+inVicinity.getId()+")"));
			env.addPercept(getId().toString(), Literal.parseLiteral("hasPosition("+inVicinity.getId()+","+inVicinity.getPosition().x+","+inVicinity.getPosition().y+")"));
			    				
		}
	}
	
	protected float getOffset(){
		return 0.6f;
	}
	
	protected Color getColour(){
		return Color.CYAN;
	}
	
}
