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

import java.awt.Color;
import java.awt.Point;
import java.util.Set;

import commerce.env.CommerceEnvironment;


/**
 * Used for representation of a purchase deployed by a shop.
 * @author Tom Klapiscak
 *
 */
public class ModelCrate extends ModelObject {
	
	/**
	 * The product contained within this crate.
	 */
	private Product product;
	
	/**
	 * The quanity of product contained within this crate.
	 */
	private int quantity;
	
	
	/**
	 * The delivery van this crate is loaded onto, null if not loaded.
	 */
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
