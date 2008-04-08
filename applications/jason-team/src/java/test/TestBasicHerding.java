package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;

import jia.Search;
import jia.Vec;
import jia.herd_position;

import org.junit.Before;
import org.junit.Test;

import arch.LocalWorldModel;
import busca.Nodo;
import env.WorldModel;

public class TestBasicHerding {

    Vec cow;
    Vec[] cows;
    Vec cowboy;
    LocalWorldModel model;
    
    @Before
    public void scenario() {
        model = new LocalWorldModel(50,50);
        model.setCorral(new Location(0,49), new Location(2,49));
        
        cow   = new Vec(6,7);
        cowboy = new Vec(3,5);
        
        cows = new Vec[4];
        cows[0] = cow;
        cows[1] = new Vec(5,30);
        cows[2] = new Vec(4,8);
        cows[3] = new Vec(5,10);

        for (int i=0; i<cows.length; i++)
            model.addCow(cows[i].getLocation(model));

    }
    
    @Test
    public void testVect() {
        //assertEquals(new Vec(6,7), cow.add(cowboy));
        //assertEquals(new Location(6,42), cow.add(cowboy).getLocation(model));
        assertEquals(new Location(3,44), cowboy.getLocation(model));
        assertEquals(new Location(1,49), model.getCorralCenter());
        assertEquals(new Vec(3,2), cow.sub(cowboy)); //new Vec(model, cowboy.getLocation(model), cow.add(cowboy).getLocation(model)));
    }

    /*
    @Test
    public void oneCow() throws Exception {
        // get the location the cow should go
        Location cowLocation = cow.getLocation(model);

        // search cow desired location
        Search s = new Search(model, cowLocation, model.getCorralCenter(), null, false, false, false, null);
        Nodo path = s.search();
        assertEquals(8, path.g()); //firstAction(path));
        
        // cow target in direction to corral
        Location cowTarget = WorldModel.getNewLocationForAction(cowLocation, s.firstAction(path));
        //assertEquals(new Location(5,43), cowTarget);
        Vec cowVecTarget = new Vec(model, cowTarget);
        //assertEquals(new Vec(5,6), cowVecTarget);
        
        // agent target to push cow into corral
        Vec agVecTarget = cow.sub(cowVecTarget).product(2).add(cow);
        Location agTarget = agVecTarget.getLocation(model);
        //assertEquals(new Location(8,40), agTarget);
        
        s = new Search(model, cowboy.getLocation(model), agTarget, null, true, true, true, null);
        path = s.search();
        System.out.println(path.g() + " " + path.montaCaminho());
        assertEquals(12, path.g());
    }
    */

    
    @Test
    public void moreCows() throws Exception {
        // get the location the cows should go
        List<Vec> cowsTarget = new ArrayList<Vec>(); 
        for (int i=0; i<cows.length; i++) {
            Search s = new Search(model, cows[i].getLocation(model), model.getCorralCenter(), null, false, false, false, null);
            Location cowTarget = WorldModel.getNewLocationForAction(cows[i].getLocation(model), s.firstAction(s.search()));
            cowsTarget.add(new Vec(model, cowTarget));
        }
        //System.out.println(cowsTarget);
        
        Vec stddev = Vec.stddev(cowsTarget);
        assertEquals(new Vec(0,9), stddev);
        
        // remove max if stddev is too big
        while (stddev.magnitude() > 3) {
            cowsTarget.remove(Vec.max(cowsTarget));
            stddev = Vec.stddev(cowsTarget);
        }
        assertTrue(stddev.magnitude() < 3);
        
        Vec mean   = Vec.mean(cowsTarget);
        assertEquals(new Vec(5,7), mean);
        double incvalue = (Vec.max(cowsTarget).sub(mean).magnitude()+2) / mean.magnitude();
        //System.out.println( incvalue);
        Vec agTarget = mean.product(incvalue+1);
        //System.out.println(agTarget);
        assertEquals(new Vec(7,10), agTarget);

        Location byIA =  new herd_position().getAgTarget(model);
        assertEquals(byIA, agTarget.getLocation(model));
        
        Search s = new Search(model, cowboy.getLocation(model), agTarget.getLocation(model), null, true, true, true, null);
        Nodo path = s.search();
        //System.out.println(path.g() + " " + path.montaCaminho());
        assertEquals(14, path.g());
        
    }
}
