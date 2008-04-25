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

import jason.asSyntax.Atom;

import java.awt.Point;

import commerce.env.CommerceEnvironment;
import commerce.exception.ModelMobileAgentException;

/**
 * Any agent that is capable of locomotion. Cannot be instantiated
 * @author tom
 *
 */
public abstract class ModelMobileAgent extends ModelAgent {

	public ModelMobileAgent(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
	}
	
	
	public void moveTowards(Point position) throws ModelMobileAgentException{
		//try{
			// TODO: Implement A* pathfinding
			int nx = getPosition().x;
			int ny = getPosition().y;
			if(position.x < getPosition().x){
				nx--;
			}else if(position.x > getPosition().x){
				nx++;
			}else if(position.y < getPosition().y){
				ny--;
			}else if(position.y > getPosition().y){
				ny++;
			}
			setPosition(new Point(nx, ny));	
		//}catch(ModelMobileAgentException e){
		//	throw new ModelMobileAgentException("Unable to move towards "+position, e);
		//}
	}	
		

}
