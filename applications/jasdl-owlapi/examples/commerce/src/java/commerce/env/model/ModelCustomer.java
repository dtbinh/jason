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

public class ModelCustomer extends ModelMobileAgent {
	
	private List<ModelCustomerListener> listeners;
	private List<Request> requests;

	public ModelCustomer(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
		requests = new Vector<Request>();
		listeners = new Vector<ModelCustomerListener>();
	}
	
	public List<Literal> getPercepts() throws JASDLException{
		List<Literal> percepts = new Vector<Literal>();
				
		Set<ModelObject> inVicinityList = model.getObjectsInVicinityOfPosition(getPosition());
		// don't want to add atLocation(me, me)
		inVicinityList.remove((ModelCustomer)this);
		
		// add my x and y coords
		percepts.add(Literal.parseLiteral("hasPosition("+getId()+","+getPosition().x+","+getPosition().y+")"));
		
		for(ModelObject inVicinity : inVicinityList){    				
			percepts.add(Literal.parseLiteral("inVicinityOf("+inVicinity.getId()+")"));
			percepts.add(Literal.parseLiteral("hasPosition("+inVicinity.getId()+","+inVicinity.getPosition().x+","+inVicinity.getPosition().y+")"));
			    				
		}
		
		return percepts;
	}
	
	
	public void addListener(ModelCustomerListener listener){
		listeners.add(listener);
	}
	
	@Override
	public void addPercepts() throws JASDLException {
		super.addPercepts();
		for(Request request : requests){
			env.addPercept(getId().toString(), Literal.parseLiteral("ui_product_request("+request.productDescription+","+request.shopDescription+","+request.qty+")"));
		}
		requests.clear();
	}

	
	public void request(String productDescription, String shopDescription, int qty){
		Request request = new Request(productDescription, shopDescription,  qty);
		System.out.println("Requested "+request);
		requests.add(request);
	}
	
	/**
	 * Returns true if all listeners approve of this choice as a purchase
	 * @param brand
	 * @return
	 */
	public boolean approve(String brand){
		boolean result = true;
		for(ModelCustomerListener listener : listeners){
			result&=listener.approve(brand); // do all listeners approve of this choice?
		}
		return result;
	}
	
	protected float getOffset(){
		return 0.6f;
	}
	
	protected Color getColour(){
		return new Color(0, 100, 0);
	}
	
	/**
	 * Associates a product description with a quantity
	 * @author tom
	 *
	 */
	class Request{
		public String shopDescription;
		public String productDescription;
		public int qty;
		public Request(String productDescription, String shopDescription, int qty) {
			super();
			this.shopDescription = shopDescription;
			this.productDescription = productDescription;
			this.qty = qty;
		}
		
		public String toString(){
			return productDescription+" ("+qty+")";
		}
		
	}
	
}
