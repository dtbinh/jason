import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

    
/** class that implements the View of Domestic Robot application */
public class HouseView extends GridWorldView {

    public HouseView(HouseModel model) {
        super(model, "Domestic Robot", 600);
        defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
        setVisible(true);
        repaint();
    }

    /** draw application objects */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        g.setColor(Color.black);
        switch (object) {
            case HouseModel.FRIDGE: drawString(g, x, y, defaultFont, "Fridge");  break;
            case HouseModel.OWNER: drawString(g, x, y, defaultFont, "Owner");  break;
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        super.drawAgent(g, x, y, Color.yellow, -1);
        g.setColor(Color.black);
        super.drawString(g, x, y, defaultFont, "Robot");
    }
}
