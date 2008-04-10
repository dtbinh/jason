package jia;

import jason.environment.grid.Location;

import java.util.List;

import arch.LocalWorldModel;


public class Vec {
    
    public final int x,y;
    
    public Vec(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** create a vector based on a location in the model */
    public Vec(LocalWorldModel model, Location l) {
        this.x = l.x;
        this.y = model.getHeight()-l.y-1;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public Location getLocation(LocalWorldModel model) { 
        return new Location(x, model.getHeight()-y-1); 
    }
    
    public double magnitude() {
        return Math.sqrt(x*x + y*y);
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

    public static Vec max(List<Vec> vs) {
        Vec max = null;
        if (vs.size() >  0)
            max = vs.get(0);
        for (Vec v: vs) {
            if (max.magnitude() < v.magnitude())
                max = v;
        }
        return max;
    }

    public static void cluster(List<Vec> vs, int maxstddev) {
        Vec stddev = Vec.stddev(vs);
        // remove max if stddev is too big
        while (stddev.magnitude() > maxstddev) {
            vs.remove(Vec.max(vs));
            stddev = Vec.stddev(vs);
        }
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
