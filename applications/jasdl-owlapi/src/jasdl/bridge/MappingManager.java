package jasdl.bridge;

import jasdl.util.DuplicateMappingException;
import jasdl.util.UnknownMappingException;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A bi-directional hash map that enforces 1 to 1 mappings.
 * 
 * @author Tom Klapiscak
 *
 * @param <X>
 * @param <Y>
 */
public class MappingManager<X extends Object, Y extends Object> {
	private Logger logger = Logger.getLogger(this.getClass().toString());
	
	private HashMap<X, Y> xToYMap;
	
	private HashMap<Y, X> yToXMap;
	
	public MappingManager(){
		xToYMap = new HashMap<X, Y>();
		yToXMap = new HashMap<Y, X>();
	}	
	
	/**
	 * Maps an x to a y and visa-versa.
	 * 1 <-> 1 relationships enforced to prevent ambiguous mapping.
	 * @param alias
	 * @param entity
	 * @throws DuplicateMappingException	if either alias or entity is already mapped (thus breaking 1 <-> 1 constraint)
	 */
	public void put(X x, Y y) throws DuplicateMappingException{
		logger.fine("mapping "+x+" <-> "+y);
		
		if(isKnownLeft(x)){
			throw new DuplicateMappingException("Duplicate mapping on "+x);
		}
		if(isKnownRight(y)){
			throw new DuplicateMappingException("Duplicate mapping on "+y);
		}
		
		logger.fine("Mapped: "+x+" to "+y);
		
		xToYMap.put(x, y);
		yToXMap.put(y, x);
	}
	
	/**
	 * Gets the alias associated with an entity
	 * @param entity
	 * @return
	 * @throws UnknownMappingException	if entity is unknown (not mapped)
	 */
	public X getLeft(Y y) throws UnknownMappingException{
		X x = yToXMap.get(y);
		if(x == null){
			throw new UnknownMappingException("Unknown mapping "+y);
		}
		return x;
	}
	
	/**
	 * Gets the entity associated with an alias
	 * @param alias
	 * @return
	 * @throws UnknownMappingException	if alias is unknown (not mapped)
	 */
	public Y getRight(X x) throws UnknownMappingException{
		Y y = xToYMap.get(x);
		if(y == null){
			throw new UnknownMappingException("Unknown mapping "+x);
		}
		return y;
	}
	
	
	public boolean isKnownLeft(X x){
		return xToYMap.containsKey(x);
	}
	
	public boolean isKnownRight(Y y){
		return yToXMap.containsKey(y);
	}	
	
	public Set<X> getLefts(){
		return xToYMap.keySet();
	}	
	
	public Set<Y> getRights(){
		return yToXMap.keySet();
	}

	public void removeByLeft(X x){
		Y y = xToYMap.remove(x);
		yToXMap.remove(y);		
	}
	
	public void removeByRight(Y y){
		X x = yToXMap.remove(y);
		xToYMap.remove(x);		
	}
}
