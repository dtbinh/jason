package commerce.env.model;

import jason.asSyntax.Atom;

public class Product {
	public String brand;
	public String classification;
	public double RRP;
	public double weight;	
	
	public Product(String classification, String brand, double rrp, double weight) {
		super();
		this.brand = brand;
		this.classification = classification;
		this.RRP = rrp;
		this.weight = weight;
	}	
	
}
