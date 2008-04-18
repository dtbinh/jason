package commerce.env;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;

import commerce.env.model.CommerceModel;
import commerce.env.model.ModelObject;

public class CommerceView extends JFrame implements Runnable{
	
	private final CommerceView view;
	private final CommerceModel model;
	private final CommerceViewPane viewPane;

	public CommerceView(CommerceModel model) {
		this.model = model;
		this.view = this;
		
		setPreferredSize(new Dimension(200, 200));
		
		viewPane = new CommerceViewPane();
		
		add( viewPane );
		
		new Thread(this).start();
				
		
		pack();
		setVisible(true);
	}
	
	public void update(){
		viewPane.repaint();
	}
	public void run() {
		while(true){
			synchronized(model.getObjects()){
				view.update();
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
	}
	
	
	
	
	class CommerceViewPane extends JPanel{
		
		

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// TODO: extract to window listener
			Dimension tileSize = new Dimension(
					view.getSize().width / model.getGridSize().width,
					view.getSize().height / model.getGridSize().height
					);
			
			Dimension borderSize = new Dimension(tileSize.width*2, (tileSize.height*2)+50);
			
			Point origin = new Point(borderSize.width, borderSize.height);
			
			Dimension renderSize = new Dimension(view.getSize().width - (borderSize.width*2), view.getSize().height - (borderSize.height*2));
			
			// adapt tileSize to respect borders
			tileSize = new Dimension(
					renderSize.width / model.getGridSize().width,
					renderSize.height / model.getGridSize().height
					);
			
			g.setColor(Color.GRAY);
			for(int x=0; x<model.getGridSize().width; x++){
				g.drawLine(origin.x+x*tileSize.width, origin.y, origin.x+x*tileSize.width, origin.y+renderSize.height);				
				for(int y=0; y<model.getGridSize().height; y++){
					g.drawLine(origin.x, origin.y+y*tileSize.height, origin.x+renderSize.width, origin.y+y*tileSize.height);
				}
			}
			
			g.setColor(Color.BLACK);
			g.drawRect(origin.x, origin.y, renderSize.width, renderSize.height);
			
			g.setColor(Color.RED);
			for(ModelObject o : model.getObjects()){
				o.render(g, tileSize, origin);
			}
			
		}


		
	}

}
