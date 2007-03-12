import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {
    
    // constants for the grid objects.
    // each different object is represented by having a single bit 
    // set (a bit mask is used in the model), so any power of two
    // represents different objects. Other numbers represent combinations 
    // of objects which are all located in the same cell of the grid.
    public static final int FRIDGE = 16;
    public static final int OWNER  = 32;

    // the grid size
    public static final int GSize = 7;
    
    boolean fridgeOpen   = false; // whether the fridge is open
    boolean carryingBeer = false; // whether the robot is carrying beer
    int sipCount       = 0; // how many sip the owner did
    int availableBeers = 2; // how many beers are available

    Location lFridge = new Location(0,0);
    Location lOwner  = new Location(GSize-1,GSize-1);

    public HouseModel() {
        // create a 7x7 grid with one mobile agent
        super(GSize, GSize, 1);

        // initial location of robot (column 3, line 3)
        // ag code 0 means the robot
        try {
			setAgPos(0, GSize/2, GSize/2);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // initial location of fridge and owner
        add(FRIDGE, lFridge);
        add(OWNER, lOwner);
    }

    boolean openFridge() {
        if (!fridgeOpen) {
            fridgeOpen = true;
            return true;
        } else {
            return false;
        }
    }

    boolean closeFridge() {
        if (fridgeOpen) {
            fridgeOpen = false; 
            return true;
        } else {
            return false;
        }                
    }  

    boolean moveTowards(Location dest) throws Exception {
        Location r1 = getAgPos(0);
        if (r1.x < dest.x)        r1.x++;
        else if (r1.x > dest.x)   r1.x--;
        if (r1.y < dest.y)        r1.y++;
        else if (r1.y > dest.y)   r1.y--;
        setAgPos(0, r1); // move the robot in the grid
        return true;
    }
    
    boolean getBeer() {
        if (fridgeOpen && availableBeers > 0 && !carryingBeer) {
            availableBeers--;
            carryingBeer = true;
            return true;
        } else {
            return false;
        }
    }
    
    boolean addBeer(int n) {
        availableBeers += n;
        return true;
    }
    
    boolean handInBeer() {
        if (carryingBeer) {
            sipCount = 10;
            carryingBeer = false;
            return true;
        } else {
            return false;
        }
    }
    
    boolean sipBeer() {
        if (sipCount > 0) {
            sipCount--;
            return true;
        } else {
            return false;
        }
    }
}
