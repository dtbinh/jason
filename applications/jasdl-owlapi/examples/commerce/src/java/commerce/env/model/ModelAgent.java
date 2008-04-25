/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
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

/**
 * Represents any object within the commerce world that is associated with some Jason agent.
 * In this case the object's id field should match up exactly both with a Jason agent name and the name of its corresponding
 * individual in society.owl.
 * 
 * @author Tom Klapiscak
 *
 */
public class ModelAgent extends ModelObject {

	public ModelAgent(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
	}
	
	/**
	 * Add the percepts for this type of agent. This adds percepts common to all types of model agent (position and vicinity information).
	 * Specialisations of this class should add percepts specific to themselves and their subtypes.
	 * @throws JASDLException
	 */
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
