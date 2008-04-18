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
