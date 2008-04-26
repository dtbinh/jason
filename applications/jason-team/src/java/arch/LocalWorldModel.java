package arch;

import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import env.WorldModel;


/**
 * Class used to model the scenario (for an agent view)
 * 
 * @author jomi
 */
public class LocalWorldModel extends WorldModel {

    int[][]                   visited; // count the visited locations
    int                       minVisited = 0; // min value for near least visited
    
    private Random			  random = new Random();

    Set<Location> cows = new HashSet<Location>();
    
    int[][]       cowsrep; // cows repulsion 
    int[][]       agsrep;  // agents repulsion
    int[][]       obsrep;  // obstacle repulsion
    
    //private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + LocalWorldModel.class.getName());

    public static LocalWorldModel create(int w, int h, int nbAg) {
    	return new LocalWorldModel(w,h,nbAg);
    }
    
    public LocalWorldModel(int w, int h) {
        this(w, h, 6);
    }
    
    public LocalWorldModel(int w, int h, int nbAg) {
        super(w, h, nbAg);
        
        visited = new int[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++)
            for (int j = 0; j < getHeight(); j++)
            	visited[i][j] = 0;

        cowsrep = new int[getWidth()][getHeight()];
        
        agsrep  = new int[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++)
            for (int j = 0; j < getHeight(); j++)
            	agsrep[i][j] = 0;

        obsrep  = new int[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++)
            for (int j = 0; j < getHeight(); j++)
            	obsrep[i][j] = 0;

    }
    
    @Override
    public void add(int value, int x, int y) {
    	super.add(value, x, y);
    	//if (value == WorldModel.AGENT || value == WorldModel.ENEMY) {
       	if (value == WorldModel.ENEMY) {
            increp(agsrep, x, y, 2, 2);
    	} else if (value == WorldModel.OBSTACLE) {
            increp(obsrep, x, y, 1, 1);
    	}
    }
    @Override
    public void remove(int value, int x, int y) {
    	super.remove(value, x, y);
    	//if (value == WorldModel.AGENT || value == WorldModel.ENEMY) {
       	if (value == WorldModel.ENEMY) {
            increp(agsrep, x, y, 2, -2);
    	}
    }


    public void clearCows() {
    	removeAll(WorldModel.COW);

    	for (int i = 0; i < getWidth(); i++)
            for (int j = 0; j < getHeight(); j++)
            	cowsrep[i][j] = 0;
    	
        cows.clear();
    }
    
    public void addCow(int x, int y) {
        add(WorldModel.COW, x, y);
        cows.add(new Location(x,y));        

        increp(cowsrep, x, y, 2, 1);
    }
    
    private void increp(int[][] m, int x, int y, int maxr, int value) {
    	for (int r = 1; r <= maxr; r++)
    		for (int c = x-r; c <= x+r; c++)
    			for (int l = y-r; l <= y+r; l++)
    				if (inGrid(c,l))
    					m[c][l] += value;    	
    }
    
    public void addCow(Location l) {
        addCow(l.x, l.y);
    }
    
    public Collection<Location> getCows() {
        return cows;
    }
    
    public int getCowsRep(int x, int y) {
    	return cowsrep[x][y];
    }
    public int getAgsRep(int x, int y) {
    	return agsrep[x][y];
    }
    public int getObsRep(int x, int y) {
    	return obsrep[x][y];
    }

    public Location nearFree(Location l) throws Exception {
        int w = 0;
        List<Location> options = new ArrayList<Location>();
        while (true) {
        	options.clear();
        	for (int y=l.y-w+1; y<l.y+w; y++) {
        		//System.out.println(" "+(l.x+w)+" "+y);
        		//System.out.println(" "+(l.x-w)+" "+y);
        		if (isFree(l.x-w,y)) 
        			options.add(new Location(l.x-w,y));
        		if (isFree(l.x+w,y)) 
        			options.add(new Location(l.x+w,y));
        	}
        	for (int x=l.x-w; x<=l.x+w;x++) {
        		//System.out.println(" "+x+" "+(l.y-w));
        		//System.out.println(" "+x+" "+(l.y+w));
        		if (isFree(x,l.y-w)) 
        			options.add(new Location(x,l.y-w));
        		if (isFree(x,l.y+w))
        			options.add(new Location(x,l.y+w));
        	}
        	//System.out.println(w + " " + options);
        	if (!options.isEmpty()) 
        		return options.get(random.nextInt(options.size()));
            w++;
        }
    }
    
    public int countObjInArea(int obj, Location startPoint, int size) {
        int c = 0;
        for (int x = startPoint.x-size; x <= startPoint.x+size; x++) {
            for (int y = startPoint.y-size; y <= startPoint.y+size; y++) {
                if (hasObject(obj, x, y)) {
                    c++;
                }
            }
        }
        return c;
    }
    
    public int getVisited(Location l) {
    	return visited[l.x][l.y];
    }

    public void incVisited(Location l) {
    	incVisited(l.x,l.y);
    }
    public void incVisited(int x, int y) {
    	visited[x][y] += 2;
    	if (x > 0)                                 visited[x-1][y  ]++;
    	if (y > 0)                                 visited[x  ][y-1]++;
    	if (y > 0 && x > 0)                        visited[x-1][y-1]++;
    	if (y+1 < getHeight())                     visited[x  ][y+1]++;
    	if (x > 0 && y+1 < getHeight())            visited[x-1][y+1]++;
    	if (x+1 < getWidth())                      visited[x+1][y  ]++;
    	if (x+1 < getWidth() && y > 0)             visited[x+1][y-1]++;
    	if (x+1 < getWidth() && y+1 < getHeight()) visited[x+1][y+1]++;
    }
    
    /** returns the near location of x,y that was least visited */
    public Location getNearLeastVisited(Location agloc, Location tr, Location bl) {
        /*
    	logger.info("------");
        for (int i = 0; i < getWidth(); i++) {
        	String line = "";
            for (int j = 0; j < getHeight(); j++) {
            	line += visited[j][i] + " ";
            }
            logger.info(line);
        }
        */
    	
    	//int visitedTarget = 0;
    	while (true) {

        	int x = agloc.x;
        	int y = agloc.y;
    		int w = 1; 
        	int dx = 0;
        	int dy = 0;
        	int stage = 1;//(x % 2 == 0 ? 1 : 2);
        	Location better = null;
        	
	    	while (w < getWidth()) { //( (w/2+distanceToBorder) < getWidth()) {
	    		switch (stage) {
	    			case 1: if (dx < w) {
	    				    	dx++;
	    				    	break;
	    					} else {
	    						stage = 2;
	    					}
	    			case 2: if (dy < w) {
	    						dy++;
	    						break;
	    					} else {
	    						stage = 3;
	    					}
	    			case 3: if (dx > 0) {
								dx--;
								break;
							} else {
								stage = 4;
							}
	    			case 4: if (dy > 0) {
								dy--;
								break;
							} else {
								stage = 1;
								x--;
								y--;
								w += 2;
							}
	    		}
	    		
    			Location l = new Location(x+dx,y+dy);
	    		if (isFree(l) && !l.equals(agloc) && l.isInArea(tr, bl)) {
	    			if (visited[l.x][l.y] < minVisited) { // a place better then minVisited! go there
	    				return l;
	    			} if (visited[l.x][l.y] == minVisited) { // a place in the minVisited level
		    			if (better == null) {
		    				better = l;
		    			} else if (l.distance(agloc) < better.distance(agloc)) {
		    				better = l;
		    			} else if (l.distance(agloc) == better.distance(agloc) && random.nextBoolean()) { // to chose ramdomly equal options
		    				better = l;
		    			}
	    			}
	    		}
	    	} // end while
	    	
	    	if (better != null) {
				return better;
			}
	    	minVisited++;
    	}
    }
    

    /** removes enemies/gold around l */
    public void clearAgView(Location l) {
    	clearAgView(l.x, l.y);
    }

    private static final int cleanPerception = ~(ENEMY + COW);

    /** removes enemies/gold around x,y */
    public void clearAgView(int x, int y) {
        int r = agPerceptionRatio;
        for (int c=x-r; c<=x+r; c++) {
            for (int l=y-r; l<=y+r; l++) {
                if (inGrid(c, l)) {
                    data[c][l] &= cleanPerception;
                    if (view != null) view.update(c,l);                    
                }
            }
        }
    }

}
