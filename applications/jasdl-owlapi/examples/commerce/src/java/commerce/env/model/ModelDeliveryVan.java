package commerce.env.model;

import jasdl.util.exception.JASDLException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;

import java.awt.Color;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import commerce.env.CommerceEnvironment;
import commerce.exception.ModelDeliveryVanException;
import commerce.exception.ModelMobileAgentException;

public class ModelDeliveryVan extends ModelMobileAgent{
	public static int DEFAULT_CARGO_SPACE;
	
	private Set<ModelCrate> cargo;
	private int cargoSpace;
	
	private ModelShop employer;
	
	
	public ModelDeliveryVan(Atom id, Point position, CommerceModel model, CommerceEnvironment env, ModelShop employer) {
		super(id, position, model, env);
		cargo = new HashSet<ModelCrate>();
		cargoSpace = DEFAULT_CARGO_SPACE;
		this.employer = employer;
	}
	
	
	
	public ModelShop getEmployer() {
		return employer;
	}

	public void load(Atom id) throws ModelDeliveryVanException{
		try{			
			ModelCrate toLoad = null;
			for(ModelObject inVicinity : model.getObjectsInVicinityOfPosition(getPosition())){
				if(inVicinity instanceof ModelCrate){					
					ModelCrate p = (ModelCrate)inVicinity;
					if(!p.isLoaded()){										// not already loaded on some van						
						if(p.getId().equals(id)){
							toLoad = p;
							break;
						}
					}
				}
			}
			if(toLoad == null){
				throw new ModelDeliveryVanException("Crate not found in vicinity of van");
			}
			// TODO: check cargo space
			cargo.add(toLoad);
			toLoad.setLoadedOn(this);
		}catch(ModelDeliveryVanException e){
			throw new ModelDeliveryVanException("Cannot load crate "+id, e);
		}
	}
	
	public void unload(Atom id) throws ModelDeliveryVanException{
		try{
			ModelCrate toUnload = null;
			for(ModelCrate loaded : cargo){
				if(loaded.getId().equals(id)){
					toUnload = loaded;
					break;
				}
			}			
			if(toUnload == null){
				throw new ModelDeliveryVanException("Not loaded on this van");
			}	
			cargo.remove(toUnload);
			toUnload.setLoadedOn(null);
		}catch(ModelDeliveryVanException e){
			throw new ModelDeliveryVanException("Cannot unload "+id, e);
		}		
	}
	
	
	
	/**
	 * Also move cargo (model purchases)
	 */
	@Override
	public void moveTowards(Point position) throws ModelMobileAgentException {
		super.moveTowards(position);
		for(ModelCrate loaded : cargo){
			loaded.setPosition(getPosition());
		}
	}

	@Override
	public void addPercepts() throws JASDLException {
		super.addPercepts();
		env.addPercept(getId().toString(), Literal.parseLiteral("cargo_space("+cargoSpace+")"));		
		for(ModelCrate loaded : cargo){
			env.addPercept(getId().toString(), Literal.parseLiteral("cargo("+loaded.getId()+")"));
		}		
	}

	protected float getOffset(){
		return 0.8f;
	}
	
	protected Color getColour(){
		return Color.MAGENTA;
	}
	
}
