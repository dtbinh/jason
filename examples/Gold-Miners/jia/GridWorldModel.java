package jia;

import mining.WorldModel;


/**
 * Model for a grid world
 * 
 * @author jomi
 */
public class GridWorldModel {

    public static final int       CLEAN    = 0;
    public static final int       ROBOT    = 2;
    public static final int       OBSTACLE = 4;

    int                           width, height;
    public int[][]                data     = null; // !!
    protected Location[]          agPos;

    // singleton pattern
    protected static GridWorldModel model = null;
    synchronized public static GridWorldModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new GridWorldModel(w, h, nbAgs);
        }
        return (WorldModel)model;
    }

    public static GridWorldModel get() {
        return model;
    }

    public static void destroy() {
        model = null;
    }

    protected GridWorldModel(int w, int h, int nbAgs) {
        width = w;
        height = h;

        // int data
        data = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = CLEAN;
            }
        }

        agPos = new Location[nbAgs];
        for (int i = 0; i < agPos.length; i++) {
            agPos[i] = new Location(-1, -1);
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNbOfAgs() {
        return agPos.length;
    }

    public boolean inGrid(int x, int y) {
        return y >= 0 && y < height && x >= 0 && x < width;
    }

    public boolean hasObject(int obj, Location l) {
        return hasObject(obj, l.x, l.y);
    }
    public boolean hasObject(int obj, int x, int y) {
        return inGrid(x, y) && (data[x][y] & obj) != 0;
    }

    public void add(int value, Location l) {
        add(value, l.x, l.y);
    }

    public void add(int value, int x, int y) {
        data[x][y] |= value;
    }

    public void addWall(int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                add(OBSTACLE, x, y);
            }
        }
    }

    public void remove(int value, Location l) {
        remove(value, l.x, l.y);
    }

    public void remove(int value, int x, int y) {
        data[x][y] &= ~value;
    }

    public void setAgPos(int ag, Location l) {
        setAgPos(ag, l.x, l.y);
    }

    public void setAgPos(int ag, int x, int y) {
        Location oldLoc = getAgPos(ag);
        if (oldLoc != null) {
            remove(ROBOT, oldLoc.x, oldLoc.y);
        }
        agPos[ag] = new Location(x, y);
        add(ROBOT, x, y);
    }

    public Location getAgPos(int ag) {
        try {
            if (agPos[ag].x == -1)
                return null;
            else
                return agPos[ag];
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isFree(Location l) {
        return isFree(l.x, l.y);
    }

    public boolean isFree(int x, int y) {
        return inGrid(x, y) && (data[x][y] & OBSTACLE) == 0 && (data[x][y] & ROBOT) == 0;
    }

    public boolean isFreeOfObstacle(Location l) {
        return isFreeOfObstacle(l.x, l.y);
    }

    public boolean isFreeOfObstacle(int x, int y) {
        return inGrid(x, y) && (data[x][y] & OBSTACLE) == 0;
    }

}
