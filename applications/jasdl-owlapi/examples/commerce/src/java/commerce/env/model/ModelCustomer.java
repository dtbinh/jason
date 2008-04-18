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

public class ModelCustomer extends ModelMobileAgent {

	public ModelCustomer(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
	}
	
	public List<Literal> getPercepts() throws JASDLException{
		List<Literal> percepts = new Vector<Literal>();
				
		Set<ModelObject> inVicinityList = model.getObjectsInVicinityOfPosition(getPosition());
		// don't want to add atLocation(me, me)
		inVicinityList.remove((ModelCustomer)this);
		
		// add my x and y coords
		percepts.add(Literal.parseLiteral("hasPosition("+getId()+","+getPosition().x+","+getPosition().y+")"));
		
		for(ModelObject inVicinity : inVicinityList){    				
			percepts.add(Literal.parseLiteral("inVicinityOf("+inVicinity.getId()+")"));
			percepts.add(Literal.parseLiteral("hasPosition("+inVicinity.getId()+","+inVicinity.getPosition().x+","+inVicinity.getPosition().y+")"));
			    				
		}
		
		return percepts;
	}
	
	protected float getOffset(){
		return 0.6f;
	}
	
	protected Color getColour(){
		return new Color(0, 100, 0);
	}
	
}
