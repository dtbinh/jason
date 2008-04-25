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

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import commerce.env.CommerceEnvironment;
import commerce.env.CommerceView;

/**
 * Manages addition, deletion and retrieval of ModelObject (and its extension) instances. 
 * 
 * @author Tom Klapiscak
 *
 */
public class CommerceModel{
	
	public static int DEFAULT_PERCEPTION_RANGE = 1;
	
	private Dimension gridSize;	

	private Set<ModelObject> objects;
	
	private CommerceView view;    
	
	private List<ModelObject> addQueue;
	private List<ModelObject> removeQueue;

	private CommerceEnvironment env;

	public CommerceModel(Dimension gridSize, CommerceEnvironment env) {
		this.gridSize = gridSize;
		
    	objects = Collections.synchronizedSet(new HashSet<ModelObject>());
    	addQueue = new Vector<ModelObject>();
    	removeQueue = new Vector<ModelObject>();
    	
    	this.env = env;
    	
    	// initialise agents
    	
    	// TODO: Read from ontology to establish agent society
    	// TODO: Read product catalogue from an external data source of some kind
    	
    	ModelShop shop1 = new ModelShop(new Atom("shop1"), new Point(0, 0), this, env);
    	shop1.addProductToCatalogue(new Product("bread", "hovis", 1.4, 800), 22);
    	shop1.addProductToCatalogue(new Product("bread", "kingsmill", 1.6, 750), 58);
    	shop1.addProductToCatalogue(new Product("milk", "cravendale", 0.9, 500), 33);
    	shop1.addProductToCatalogue(new Product("milk", "tescos_semi_skimmed", 0.7, 500), 85);
    	shop1.addProductToCatalogue(new Product("butter", "clover_butter", 0.8, 300), 74);
    	shop1.addProductToCatalogue(new Product("rootVegetable", "sweet_potatoes", 2.2, 600), 58);
    	
    	ModelShop shop2 = new ModelShop(new Atom("shop2"), new Point(7, 5), this, env);
    	shop2.addProductToCatalogue(new Product("beef", "farmer_jims_rump_steak", 10.5, 500), 22);
    	shop2.addProductToCatalogue(new Product("lamb", "farmer_jims_lamb_chops", 10.5, 400), 80);
    	shop2.addProductToCatalogue(new Product("beef", "tescos_sirloin_steak", 5, 600), 11);
    	shop2.addProductToCatalogue(new Product("fish", "tescos_salmon", 8, 200), 29);
    	shop2.addProductToCatalogue(new Product("fish", "tescos_chicken_breasts", 5.9, 450), 92);
    	
    	ModelShop shop3 = new ModelShop(new Atom("shop3"), new Point(15, 16), this, env);    	
    	shop3.addProductToCatalogue(new Product("greenVegetable", "iceburg_lettuce", 0.3, 90), 54);
    	shop3.addProductToCatalogue(new Product("rootVegetable", "potatoes", 1.2, 1000), 92);
    	
    	addObject(shop1);    	
    	addObject(shop2);
    	addObject(shop3);
    	
    	addObject(new ModelCustomer(new Atom("customer1"), new Point(3, 17), this, env));
    	addObject(new ModelCustomer(new Atom("customer2"), new Point(15, 8), this, env));
    	addObject(new ModelCustomer(new Atom("customer3"), new Point(11, 3), this, env));
    	addObject(new ModelDeliveryVan(new Atom("delivery_van1"), new Point(1, 0), this, env, shop1));
    	addObject(new ModelDeliveryVan(new Atom("delivery_van2"), new Point(8, 5), this, env, shop1));
    	addObject(new ModelDeliveryVan(new Atom("delivery_van3"), new Point(16, 16), this, env, shop1));
    	addObject(new ModelDeliveryVan(new Atom("delivery_van4"), new Point(2, 1), this, env, shop1));
    	addObject(new ModelPA(new Atom("pa1"), new Point(0, 0), this, env));
    	addObject(new ModelPA(new Atom("pa2"), new Point(0, 0), this, env));
    	addObject(new ModelPA(new Atom("pa3"), new Point(0, 0), this, env));
    	
    	updateObjects();
    	
	}
	
	public void setView(CommerceView view){
		this.view = view;
	}
	
	public void addObject(ModelObject o){
		addQueue.add(o);
	//	objects.add(o);
	}
	
	public void removeObject(ModelObject o){
		removeQueue.add(o);
	//	objects.remove(o);
	}
	
	public void updateObjects(){
		for(ModelObject o : removeQueue){
			objects.remove(o);
		}
		for(ModelObject o : addQueue){
			objects.add(o);
		}
		removeQueue.clear();
		addQueue.clear();
	}
		
	public Set<ModelObject> getObjects() {
		return objects;
	}
	
	public Set<ModelObject> getObjectsInVicinityOfPosition(Point position){
		return getObjectsInVicinityOfPosition(position, DEFAULT_PERCEPTION_RANGE);
	}
	
	private Set<ModelObject> getObjectsInVicinityOfPosition(Point position, int radius){
		Set<ModelObject> found = new HashSet<ModelObject>();
		for(int x = position.x-radius; x<position.x+radius; x++){
			for(int y = position.y-radius; y<position.y+radius; y++){
				found.addAll( getObjectsAtPosition(new Point(x, y)) );
			}		
		}
		return found;
	}
	
	public Set<ModelObject> getObjectsAtPosition(Point position){
		Set<ModelObject> found = new HashSet<ModelObject>();
		for(ModelObject o : getObjects()){
			if(o.getPosition().equals(position)){
				found.add(o);
			}
		}
		return found;
	}
	
	public ModelObject getObjectById(Atom id){
		for(ModelObject o : getObjects()){
			if(o.getId().equals(id)){
				return o;
			}
		}
		return null;
	}

	
	public Dimension getGridSize() {
		return gridSize;
	}

	public CommerceView getView() {
		return view;
	}
		


}
