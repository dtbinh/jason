import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.Random;

/** class that implements the Model of the Game of Life application */
public class LifeModel extends GridWorldModel {
    
    // the grid size
    public static final int GSize = 30;
    public static final int LIFE  = 16; // represent a cell with life

    //private Logger logger = Logger.getLogger(LifeModel.class.getName());
    
    Random random = new Random();
    
    public LifeModel(int density) {
        super(GSize, GSize, GSize*GSize);

        // initial agents' state (alive or dead)
        try {
            for (int i=0; i<GSize; i++) {
                for (int j=0; j<GSize; j++) {
                    int ag = getAgId(i,j);
                    setAgPos(ag, i, j);
                    if (random.nextInt(100) < density) {
                        alive(ag);
                    }
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public Location getAgPos(int ag) {
        return new Location(ag / GSize, ag % GSize);
    }
    
    int getAgId(int x, int y) {
        return x*GSize + y;
    }
    
    void alive(int ag) {
        add(LIFE, getAgPos(ag));
    }
    
    boolean isAlive(int ag) {
        return hasObject(LIFE, getAgPos(ag));
    }
    
    boolean isAlive(int x, int y) {
        return hasObject(LIFE, x, y);
    }

    void dead(int ag) {
        remove(LIFE, getAgPos(ag));
    }  
}
