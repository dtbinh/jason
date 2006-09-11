package jia;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;


public class GridWorldView extends JFrame {

    protected int cellSizeW = 0;
    protected int cellSizeH = 0;

    protected GridCanvas     drawArea;
    protected GridWorldModel model;

    public GridWorldView(GridWorldModel model, String title, int width) {
        super(title);
        this.model = model;
        initComponents(width);
        model.setView(this);
    }

    public void initComponents(int width) {
        setSize(width, width);
        getContentPane().setLayout(new BorderLayout());
        drawArea = new GridCanvas();
        getContentPane().add(BorderLayout.CENTER, drawArea);        
    }
    
    @Override
    public void repaint() {
        cellSizeW = drawArea.getWidth() / model.getWidth();
        cellSizeH = drawArea.getHeight() / model.getHeight();
        super.repaint();
        drawArea.repaint();
    }

    public void update() {
        repaint();
    }
    

    public void update(int x, int y) {
        drawEmpty(drawArea.getGraphics(), x, y);
        draw(drawArea.getGraphics(), x, y);
    }

    public void drawObstacle(Graphics g, int x, int y) {
        g.setColor(Color.darkGray);
        g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        g.setColor(Color.black);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH+2, cellSizeW-4, cellSizeH-4);
    }

    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        g.setColor(c);
        g.fillOval(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 8, cellSizeH - 8);
        g.setColor(Color.black);
        g.drawString("" + id, x * cellSizeW + 6, y * cellSizeH + 15);
    }

    public void drawEmpty(Graphics g, int x, int y) {
        g.setColor(Color.white);
        g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        g.setColor(Color.lightGray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }


    /** method for unknown generic object */
    public void draw(Graphics g, int x, int y, int object) {
        g.setColor(Color.black);
        g.drawString("" + object, x * cellSizeW + 2, y * cellSizeH + 16);
    }

    private static int limit = (int)Math.pow(2,14);
    private void draw(Graphics g, int x, int y) {
        if ((model.data[x][y] & GridWorldModel.OBSTACLE) != 0) {
            drawObstacle(g, x, y);
        } else if ((model.data[x][y] & GridWorldModel.AGENT) != 0) {
            drawAgent(drawArea.getGraphics(), x, y, Color.blue, model.getAgAtPos(x, y)+1);
        } else {
            int vl = GridWorldModel.OBSTACLE*2;
            while (vl < limit) {
                if ((model.data[x][y] & vl) != 0) {
                    draw(g, x, y, vl);
                }
                vl *= 2;
            }
        }
    }
    
    class GridCanvas extends Canvas {
        
        public void paint(Graphics g) {
            int mwidth = model.getWidth();
            int mheight = model.getHeight();

            g.setColor(Color.lightGray);
            for (int l = 1; l <= mheight; l++) {
                g.drawLine(0, l * cellSizeH, mwidth * cellSizeW, l * cellSizeH);
            }
            for (int c = 1; c <= mwidth; c++) {
                g.drawLine(c * cellSizeW, 0, c * cellSizeW, mheight * cellSizeH);
            }

            for (int x = 0; x < mwidth; x++) {
                for (int y = 0; y < mheight; y++) {
                    draw(g,x,y);
                }
            }
        }
    }
}
