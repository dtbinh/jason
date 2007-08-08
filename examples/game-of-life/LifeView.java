import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Graphics;

    
/** class that implements the View of the Game of Life application */
public class LifeView extends GridWorldView {

    private static final long serialVersionUID = 1L;

    LifeModel hmodel;
	
    public LifeView(LifeModel model) {
        super(model, "Game of Life", 700);
		hmodel = model;
        setVisible(true);
        repaint();
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        c = Color.white;
        if (hmodel.isAlive(x,y)) {
            c = Color.darkGray;            
        }
        super.drawAgent(g, x, y, c, -1);
    }
}
