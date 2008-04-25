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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import commerce.env.CommerceEnvironment;

/**
 * Provides functionality common to all types of object present within the commerce world.
 * Cannot be instantiated.
 * @author Tom Klapiscak
 *
 */
public abstract class ModelObject {
	
	/**
	 * A unique identifier for this agent.
	 */
	private Atom id;
	
	/**
	 * The grid position within the commerce world.
	 */
	private Point position;
	
	/**
	 * The model this object resides within.
	 */
	protected CommerceModel model;
	
	/**
	 * The environment this object resides within.
	 */
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

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}
	
	/**
	 * The on-screen label for this agent drawn in {@link ModelObject#render(Graphics, Dimension, Point)}. Commonly extended by
	 * specialisations of this class.
	 * @return
	 */
	public String getLabel(){
		return getId().toString();
	}
	
	/**
	 * The length of the line connecting the object oval to object name drawn in {@link ModelObject#render(Graphics, Dimension, Point)}. Commonly extended by
	 * specialisations of this class.
	 * @return
	 */
	protected float getOffset(){
		return 0.2f;
	}
	
	/**
	 * The colour this object will be rendered using in {@link ModelObject#render(Graphics, Dimension, Point)}. Commonly extended by
	 * specialisations of this class.
	 * @return
	 */
	protected Color getColour(){
		return Color.GRAY;
	}
	
	/**
	 * The default representation of an object within the commerce world. Uses (commonly overrided) parameters taken
	 * from {@link ModelObject#getOffset()}, {@link ModelObject#getLabel()} and {@link ModelObject#getColour()} when
	 * rendering. 
	 * @param g
	 * @param tileSize
	 * @param origin
	 */
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
