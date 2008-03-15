package jasdl.bridge;

import jasdl.util.DuplicateMappingException;
import jasdl.util.UnknownMappingException;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class MappingManager<X extends Object, Y extends Object> {
	private Logger logger = Logger.getLogger(this.getClass().toString());
	
	/**
	 * Maps aliases to entities
	 */
	private HashMap<X, Y> xToYMap;
	
	/**
	 * Maps entities to aliases
	 */
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
			throw new UnknownMappingException("Unknown mapping "+y);
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
}
