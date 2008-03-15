package jasdl.bridge.alias;

import jasdl.util.DuplicateMappingException;
import jasdl.util.UnknownMappingException;

import java.util.HashMap;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLEntity;

public class AliasManager {
	private Logger logger = Logger.getLogger(this.getClass().toString());
	
	/**
	 * Maps aliases to entities
	 */
	private HashMap<Alias, OWLEntity> aliasToEntityMap;
	
	/**
	 * Maps entities to aliases
	 */
	private HashMap<OWLEntity, Alias> entityToAliasMap;
	
	public AliasManager(){
		aliasToEntityMap = new HashMap<Alias, OWLEntity>();
		entityToAliasMap = new HashMap<OWLEntity, Alias>();
	}	
	
	/**
	 * Maps an alias to an entity and visa-versa.
	 * 1 <-> 1 relationships enforced to prevent ambiguous aliasing.
	 * @param alias
	 * @param entity
	 * @throws DuplicateMappingException	if either alias or entity is already mapped (thus breaking 1 <-> 1 constraint)
	 */
	public void put(Alias alias, OWLEntity entity) throws DuplicateMappingException{
		
		if(aliasToEntityMap.containsKey(alias)){
			throw new DuplicateMappingException("Duplicate mapping on alias "+alias);
		}
		if(entityToAliasMap.containsKey(entity)){
			throw new DuplicateMappingException("Duplicate mapping on entity "+entity);
		}
		
		logger.fine("Mapped: "+alias+" to "+entity);
		
		aliasToEntityMap.put(alias, entity);
		entityToAliasMap.put(entity, alias);
	}
	
	/**
	 * Gets the alias associated with an entity
	 * @param entity
	 * @return
	 * @throws UnknownMappingException	if entity is unknown (not mapped)
	 */
	public Alias get(OWLEntity entity) throws UnknownMappingException{
		Alias alias = entityToAliasMap.get(entity);
		if(alias == null){
			throw new UnknownMappingException("Unknown entity "+entity);
		}
		return alias;
	}
	
	/**
	 * Gets the entity associated with an alias
	 * @param alias
	 * @return
	 * @throws UnknownMappingException	if alias is unknown (not mapped)
	 */
	public OWLEntity get(Alias alias) throws UnknownMappingException{
		OWLEntity entity = aliasToEntityMap.get(alias);
		if(entity == null){
			throw new UnknownMappingException("Unknown alias "+alias);
		}
		return entity;
	}
	
	
	public boolean isKnown(Alias alias){
		return aliasToEntityMap.containsKey(alias);
	}
}
