package jia;

import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;

import arch.LocalWorldModel;


public class Vec implements Cloneable {
    
    public final int x,y;
    public final double r,t;
    
    public Vec(int x, int y) {
        this.x = x;
        this.y = y;
        this.r = Math.sqrt(x*x + y*y);
        this.t = Math.atan2(y,x);
    }

    /** create a vector based on a location in the model */
    public Vec(LocalWorldModel model, Location l) {
        this.x = l.x;
        this.y = model.getHeight()-l.y-1;
        this.r = Math.sqrt(x*x + y*y);
        this.t = Math.atan2(y,x);
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public double magnitude() { return r; }
    public double angle() { return t; }
    
    public Location getLocation(LocalWorldModel model) { 
        return new Location(x, model.getHeight()-y-1); 
    }
    
    public Vec add(Vec v) {
        return new Vec(x + v.x, y + v.y);
    }
    public Vec sub(Vec v) {
        return new Vec(x - v.x, y - v.y);
    }
    public Vec product(double e) {
        return new Vec((int)(x * e), (int)(y *e));
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Vec) {
            Vec v = (Vec)o;
            return (x == v.x) && (y == v.y);
        }
        return false;
    }
    
    public Object clone() {
    	return this; // it is an immutable object, no need to create a new one
    }

    

	/**
	 * Provides info on which octant (0-7) the vector lies in.
	 * 0 indicates 0 radians +- PI/8 1-7 continue CCW.
	 * @return 0 - 7, depending on which direction the vector is pointing.
	 */
	public int octant() {
		double	temp = t + Math.PI/8;
		if (temp<0) 
			temp += Math.PI*2;
		return ((int)(temp/(Math.PI/4))%8);
	}

	/**
	 * Provides info on which quadrant (0-3) the vector lies in.
	 * 0 indicates 0 radians +- PI/4 1-3 continue CCW.
	 * @return 0 - 3, depending on which direction the vector is pointing.
	 */
	public int quadrant() {
		double temp = t + Math.PI/4;
		if (temp<0) temp += Math.PI*2;
		return ((int)(temp/(Math.PI/2))%4);
	}


    //
    // Useful static methods for list of vecs
    //
    
    public static Vec max(List<Vec> vs) {
        Vec max = null;
        for (Vec v: vs) {
            if (max == null || max.magnitude() < v.magnitude())
                max = v;
        }
        return max;
    }

    public static List<Vec> sub(List<Vec> vs, Vec ref) {
    	List<Vec> r = new ArrayList<Vec>(vs.size());
        for (Vec v: vs) {
        	r.add(v.sub(ref));
        }
        return r;
    }
    
    public static List<Vec> cluster(List<Vec> vs, int maxstddev) {
    	vs = new ArrayList<Vec>(vs);
        Vec stddev = Vec.stddev(vs);
        // remove max if stddev is too big
        while (stddev.magnitude() > maxstddev) {
        	Vec mean = Vec.mean(vs);
        	Vec max  = Vec.max(Vec.sub(vs, mean));
            vs.remove(max.add(mean));
            stddev = Vec.stddev(vs);
        }
        return vs;
    }

    public static Vec mean(List<Vec> vs) {
        if (vs.isEmpty())
            return new Vec(0,0);
        int x = 0, y = 0;
        for (Vec v: vs) {
            x += v.x;
            y += v.y;
        }
        return new Vec(x/vs.size(), y/vs.size());  
    }
    
    public static Vec stddev(List<Vec> vs) {
        if (vs.isEmpty())
            return new Vec(0,0);
        Vec mean = mean(vs);
        int x = 0, y = 0;
        for (Vec v: vs) {
            x += Math.pow(v.x - mean.x,2);
            y += Math.pow(v.y - mean.y,2);
        }
        x = x / vs.size();
        y = y / vs.size();
        
        return new Vec( (int)Math.sqrt(x), (int)Math.sqrt(y));
    }

    
    @Override
    public String toString() {
        return x + "," + y;
    }
}
