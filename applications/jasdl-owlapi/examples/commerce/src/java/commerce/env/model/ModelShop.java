package commerce.env.model;

import jasdl.util.exception.JASDLException;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import commerce.env.CommerceEnvironment;
import commerce.exception.ModelShopException;

public class ModelShop extends ModelAgent {
	
	private Set<Product> catalogue;
	private HashMap<Product, Integer> stock;

	public ModelShop(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
		this.catalogue = new HashSet<Product>();
		stock = new HashMap<Product, Integer>();
	}
	
	public Product getProductByBrand(String brand) throws ModelShopException{
		for(Product p : catalogue){
			if(p.brand.equals(brand)){
				return p;
			}
		}
		throw new ModelShopException("Unknown brand "+brand);
	}
	
	public void addProductToCatalogue(Product product, int initialStockLevel){
		catalogue.add(product);
		setStockLevel(product, initialStockLevel);
	}
	
	public void setStockLevel(Product product, int level){
		stock.remove(product);
		stock.put(product, level);
	}
	
	public int getStockLevel(Product product){
		return stock.get(product);
	}
	
	public void decreaseStockLevel(Product product, int amount){
		setStockLevel(product, getStockLevel(product)-amount);
	}
	
	public void deploy(Atom id, Product p, int quantity) throws ModelShopException{
		if(!catalogue.contains(p)){
			throw new ModelShopException("We do not stock this product");
		}		
		if(quantity > getStockLevel(p)){
			throw new ModelShopException("Requested quantity exceeds stock level");
		}
		decreaseStockLevel(p, quantity);
		model.addObject( 
				new ModelCrate(
						id,
						getPosition(),
						model,
						env,
						p,
						quantity));
	}

	/**
	 * Adds details of product catalogue as SE-percepts
	 */
	@Override
	public void addPercepts() throws JASDLException {
		super.addPercepts();
		
		List<Atom> differentIndividuals = new Vector<Atom>();
		
		for(Product product : catalogue){
			
			// add product
			env.addPercept(
					getId().toString(),
					env.getSELiteralFactory().construct(
						true,
						new Atom(product.classification),
						new Atom(product.brand),
						new Term[0],
						env.c)
					.getLiteral());
			
			// add product stock
			env.addPercept(
					getId().toString(),
					env.getSELiteralFactory().construct(
							true,
							new Atom("hasInStock"),
							new Atom(product.brand),
							new NumberTermImpl(getStockLevel(product)),
							new Term[0],
							env.c)
						.getLiteral());
			
			// add product weight
			env.addPercept(
					getId().toString(),
					env.getSELiteralFactory().construct(
							true,
							new Atom("hasWeight"),
							new Atom(product.brand),
							new NumberTermImpl(product.weight),
							new Term[0],
							env.c)
						.getLiteral());
			
			// add product price
			env.addPercept(
					getId().toString(),
					env.getSELiteralFactory().construct(
							true,
							new Atom("hasPrice"),
							new Atom(product.brand),
							new NumberTermImpl(product.RRP),
							new Term[0],
							env.c)
						.getLiteral());	
						
			differentIndividuals.add(new Atom(product.brand));
		}
		
		// enter all products into an all_different assertion
		Atom[] is = (Atom[]) differentIndividuals.toArray(new Atom[differentIndividuals.size()]);
		env.addPercept(
			getId().toString(),
			env.getSELiteralFactory().construct(
					true,
					is,
					new Atom[0],
					env.c)
				.getLiteral());
		
	}

	protected float getOffset(){
		return 0.4f;
	}
	
	protected Color getColour(){
		return Color.BLUE;
	}
	
	
	

	
}
