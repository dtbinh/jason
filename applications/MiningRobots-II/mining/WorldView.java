package mining;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

import jia.Location;

public class WorldView extends JFrame {

    private int              cellSizeW = 20;
    private int              cellSizeH = 20;

    MyCanvas                 drawArea;
    WorldModel               model;

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
        super("Mining World");
        this.model = model;
        int s = 600;
        setSize(s, s);
        getContentPane().setLayout(new BorderLayout());
        drawArea = new MyCanvas();
        getContentPane().add(BorderLayout.CENTER, drawArea);
        setVisible(true);
    }

    public void repaint() {
        super.repaint();
        drawArea.repaint();
    }

    //int updateCount = 0;

    public void update() {
        //updateCount++;
        //if (updateCount == 4) { // only the fourth agent ask draw, we realy draw
        //    updateCount = 0;
            repaint();
        //}
    }

    class MyCanvas extends Canvas {
        public void paint(Graphics g) {
            int mwidth = model.getWidth();
            int mheight = model.getHeight();
            cellSizeW = getWidth() / mwidth;
            cellSizeH = getHeight() / mheight;

            g.setColor(Color.lightGray);
            for (int l = 1; l <= mheight; l++) {
                g.drawLine(0, l * cellSizeH, mwidth * cellSizeW, l * cellSizeH);
            }
            for (int c = 1; c <= mwidth; c++) {
                g.drawLine(c * cellSizeW, 0, c * cellSizeW, mheight * cellSizeH);
            }

            for (int x = 0; x < mwidth; x++) {
                for (int y = 0; y < mheight; y++) {
                    if ((model.data[x][y] & WorldModel.OBSTACLE) != 0) {
                        drawObstacle(g, x, y);
                    } else {
                        // if ((model.data[x][y] & WorldModel.CLEAN) != 0) {
                        // drawEmpty(g, x, y);
                        // }
                        if ((model.data[x][y] & WorldModel.DEPOT) != 0) {
                            drawDepot(g, x, y);
                        }
                        if ((model.data[x][y] & WorldModel.GOLD) != 0) {
                            drawGold(g, x, y);
                        }
                        if ((model.data[x][y] & WorldModel.ENEMY) != 0) {
                            drawEnemy(g, x, y);
                        }
                        if ((model.data[x][y] & WorldModel.ROBOT) != 0) {
                            drawAlly(g, x, y, Color.blue);
                        }
                    }
                }
            }

            for (int i = 0; i < model.getNbOfAgs(); i++) {
                Location pos = model.getAgPos(i);
                if (pos != null) {
                    if (model.isCarryingGold(i)) {
                        drawAlly(g, pos.x, pos.y, Color.yellow);
                    }
                    drawAllyId(g, pos.x, pos.y, (i + 1));
                }
            }
        }

        public void drawEmpty(Graphics g, int x, int y) {
            g.setColor(Color.white);
            g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 2, cellSizeH - 2);
        }

        public void drawObstacle(Graphics g, int x, int y) {
            g.setColor(Color.darkGray);
            g.fillRect(x*cellSizeW,   y*cellSizeH, cellSizeW, cellSizeH);
            g.setColor(Color.black);
            g.drawRect(x*cellSizeW, y*cellSizeH, cellSizeW, cellSizeH);
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
            g.drawRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 2, cellSizeH - 2);
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

        public void drawAlly(Graphics g, int x, int y, Color c) {
            g.setColor(c);
            g.fillOval(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 8, cellSizeH - 8);
        }

        public void drawAllyId(Graphics g, int x, int y, int id) {
            g.setColor(Color.black);
            g.drawString("" + id, x * cellSizeW + 7, y * cellSizeH + 16);
        }

        public void drawEnemy(Graphics g, int x, int y) {
            g.setColor(Color.red);
            g.fillOval(x * cellSizeW + 7, y * cellSizeH + 7, cellSizeW - 8, cellSizeH - 8);
        }
    }
}
