package mining;

import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Graphics;


public class WorldView extends GridWorldView {

    // singleton pattern
    private static WorldView view      = null;

    synchronized public static WorldView create(WorldModel model) {
        if (view == null) {
            view = new WorldView(model);
        }
        return view;
    }

    public static void destroy() {
        if (view != null) {
            view.setVisible(false);
            view = null;
        }
    }

    private WorldView(WorldModel model) {
        super(model, "Mining World", 600);
        setVisible(true);
        repaint();
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
        case WorldModel.DEPOT:   drawDepot(g, x, y);  break;
        case WorldModel.GOLD:    drawGold(g, x, y);  break;
        case WorldModel.ENEMY:   drawEnemy(g, x, y);  break;
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Color idColor = Color.black;
        if (((WorldModel)model).isCarryingGold(id)) {
            super.drawAgent(g, x, y, Color.yellow, -1);
        } else {
            super.drawAgent(g, x, y, c, -1);
            idColor = Color.white;
        }
        g.setColor(idColor);
        drawString(g, x, y, defaultFont, String.valueOf(id+1));
    }

    public void drawDepot(Graphics g, int x, int y) {
        g.setColor(Color.gray);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.pink);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        g.drawLine(x * cellSizeW + 2, y * cellSizeH + 2, (x + 1) * cellSizeW - 2, (y + 1) * cellSizeH - 2);
        g.drawLine(x * cellSizeW + 2, (y + 1) * cellSizeH - 2, (x + 1) * cellSizeW - 2, y * cellSizeH + 2);
    }

    public void drawGold(Graphics g, int x, int y) {
        g.setColor(Color.yellow);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        int[] vx = new int[4];
        int[] vy = new int[4];
        vx[0] = x * cellSizeW + (cellSizeW / 2);
        vy[0] = y * cellSizeH;
        vx[1] = (x + 1) * cellSizeW;
        vy[1] = y * cellSizeH + (cellSizeH / 2);
        vx[2] = x * cellSizeW + (cellSizeW / 2);
        vy[2] = (y + 1) * cellSizeH;
        vx[3] = x * cellSizeW;
        vy[3] = y * cellSizeH + (cellSizeH / 2);
        g.fillPolygon(vx, vy, 4);
    }

    public void drawEnemy(Graphics g, int x, int y) {
        g.setColor(Color.red);
        g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
    }
}
