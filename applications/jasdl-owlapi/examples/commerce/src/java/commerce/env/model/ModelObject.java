package commerce.env.model;

import jason.asSyntax.Atom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import commerce.env.CommerceEnvironment;

/**
 * Cannot be instantiated
 * @author tom
 *
 */
public abstract class ModelObject {
	
	private Atom id;
	private Point position;
	protected CommerceModel model;
	protected CommerceEnvironment env;
	
	public ModelObject(Atom id, Point position, CommerceModel model, CommerceEnvironment env){
		this.id = id;
		this.position = position;
		this.model = model;
		this.env = env;
	}
	
	

	public Atom getId() {
		return id;
	}
	
	public boolean isInVicinityOf(ModelObject o){		
		return model.getObjectsInVicinityOfPosition(getPosition()).contains(o);
	}
	

	public String getLabel(){
		return getId().toString();
	}


	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}
	
	protected float getOffset(){
		return 0.2f;
	}
	
	protected Color getColour(){
		return Color.GRAY;
	}
	
	public void render(Graphics g, Dimension tileSize, Point origin) {
		
		Dimension size = new Dimension(tileSize.width / 4, tileSize.height / 4);
		
		Point centre = new Point(
				origin.x+getPosition().x*tileSize.width + (int)(tileSize.width * getOffset()),
				origin.y+getPosition().y*tileSize.height + (int)(tileSize.height/2));
		
		
		g.setColor(getColour());
		g.fillOval(
				centre.x - size.width/2,
				centre.y - size.height/2,
				size.width,
				size.height);
		
		int lineBottom = centre.y+(int)((tileSize.height/2) + (tileSize.height*2 - (tileSize.height*getOffset()*2)));
		g.drawLine(centre.x, centre.y, centre.x, lineBottom);
		
		g.setFont(new Font("arial", tileSize.width, tileSize.height/2));
		g.drawString(getLabel(), centre.x, lineBottom+tileSize.height/2);
	}
	
}
