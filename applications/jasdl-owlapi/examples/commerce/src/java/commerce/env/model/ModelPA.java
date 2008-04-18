package commerce.env.model;

import jasdl.util.exception.JASDLException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import commerce.env.CommerceEnvironment;

public class ModelPA extends ModelAgent {

	public ModelPA(Atom id, Point position, CommerceModel model, CommerceEnvironment env) {
		super(id, position, model, env);
	}
	
	@Override
	public void addPercepts() throws JASDLException{
		super.addPercepts();
	}
	
	public void render(Graphics g, Dimension tileSize, Point origin){
		return; // don't render the PA
	}
	
	

	
}
