package commerce.env;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import commerce.env.model.CommerceModel;
import commerce.env.model.ModelCustomer;
import commerce.env.model.ModelObject;
import commerce.ui.customer.CustomerUIPanel;

public class CommerceView extends JFrame implements Runnable{
	
	private final CommerceView view;
	private final CommerceModel model;
	private final CommerceViewPane viewPane;
	private JTabbedPane tabs;
	private CommerceEnvironment env;
	private JPanel masterPane;

	public CommerceView(CommerceModel model, CommerceEnvironment env) {
		this.model = model;
		this.view = this;
		this.env = env;
		
		
			
		masterPane = new JPanel();		
			masterPane.setLayout(new BoxLayout(masterPane, BoxLayout.X_AXIS));
			add(masterPane);
		
			// Add the environment view pane
			viewPane = new CommerceViewPane();	
				viewPane.setPreferredSize(new Dimension(500, 300));
				masterPane.add( viewPane );		
			
			// Add the customer tabs
			tabs = new JTabbedPane();
				tabs.setVisible(true);
				masterPane.add(tabs);		
		
		
		new Thread(this).start();	
		
		setPreferredSize(new Dimension(980, 740));
		
		pack();
		setVisible(true);
	}
	
	
	/**
	 * Add a customer tab
	 * @param customer
	 */
	public void addCustomer(ModelCustomer customer){
		CustomerUIPanel panel = new CustomerUIPanel(env, customer);
		tabs.addTab(customer.getLabel(), panel);		
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
					getSize().width / model.getGridSize().width,
					getSize().height / model.getGridSize().height
					);
			
			Dimension borderSize = new Dimension(tileSize.width*2, tileSize.height*2);
			
			Point origin = new Point(borderSize.width, borderSize.height);
			
			Dimension renderSize = new Dimension(getSize().width - (borderSize.width*2), getSize().height - (borderSize.height*2));
			
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
