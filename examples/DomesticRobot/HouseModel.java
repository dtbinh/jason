import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {
        
    public static final int GSize = 7;
    public static final int NSips = 10;

    public static final int FRIDGE = 16;
    public static final int OWNER  = 32;

    boolean fridgeOpen   = false; // whether the fridge is open
    boolean carryingBeer = false; // whether the robot is carrying berr
    int beer   = 0; // how many sip the owner did
    int avBeer = 2; // how many beers are available

    Location lFridge = new Location(0,0);
    Location lOwner  = new Location(GSize-1,GSize-1);

    public HouseModel() {
        super(GSize, GSize, 1); // only one agent moves

        // initial location of robot
        setAgPos(0, GSize/2, GSize/2);
        
        // initial location of fridge and owner
        add(FRIDGE, lFridge);
        add(OWNER, lOwner);
    }

    void openFridge() {
        if (!fridgeOpen) {
            fridgeOpen = true;                 
        }
    }

    void closeFridge() {
        if (fridgeOpen) {
            fridgeOpen = false;                 
        }
    }  

    void moveTowards(Location dest) {
        Location r1 = getAgPos(0);
        if (r1.x < dest.x)
            r1.x++;
        else if (r1.x > dest.x)
            r1.x--;
        if (r1.y < dest.y)
            r1.y++;
        else if (r1.y > dest.y)
            r1.y--;
        setAgPos(0, r1);
    }
    
    void getBeer() {
        if (fridgeOpen && avBeer > 0 && !carryingBeer) {
            avBeer--;
            carryingBeer = true;
        }
    }
    
    void handInBeer() {
        beer = NSips;
        carryingBeer = false;
    }
    
    void sipBeer() {
        beer--;
    }
 
    void addBeer(int n) {
        avBeer = avBeer + n;
    }
}
