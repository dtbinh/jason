package commerce.env.model;

import jason.asSyntax.Atom;

import java.awt.Color;
import java.awt.Point;
import java.util.Set;

import commerce.env.CommerceEnvironment;

public class ModelCrate extends ModelObject {
	
	private Product product;
	private int quantity;
	
	private ModelDeliveryVan loadedOn = null;

	public ModelCrate(Atom id, Point position, CommerceModel model, CommerceEnvironment env, Product product, int quantity) {
		super(id, position, model, env);
		this.product = product;
		this.quantity = quantity;
	}
	
	public void setLoadedOn(ModelDeliveryVan van){
		loadedOn = van;
	}

	public ModelDeliveryVan getLoadedOn() {
		return loadedOn;
	}
	
	public boolean isLoaded(){
		return loadedOn != null;
	}
	protected float getOffset(){
		return 0.9f;
	}
	
	protected Color getColour(){
		return new Color(80, 80, 0);
	}
	
	@Override
	public String getLabel(){
		Set<ModelObject> os = model.getObjectsAtPosition(getPosition());
		int no = 0;
		for(ModelObject o : os){
			if(o instanceof ModelCrate){
				no++;
			}
		}
		if(no==1){
			return "crate";
		}else{
			return "crates ("+no+")";
		}
	}
	

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	

}
