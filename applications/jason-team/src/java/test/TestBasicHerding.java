package test;

import static org.junit.Assert.assertEquals;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jia.Search;
import jia.Vec;
import jia.herd_position;
import jia.scouter_pos;
import jia.herd_position.Formation;

import org.junit.Before;
import org.junit.Test;

import arch.LocalWorldModel;
import busca.Nodo;
import env.WorldModel;

public class TestBasicHerding {

    Vec[] cows;
    Vec cowboy;
    LocalWorldModel model;
    
    @Before
    public void scenario() {
        model = new LocalWorldModel(50,50);
        model.setCorral(new Location(0,49), new Location(2,49));
        model.wall(7, 44, 7, 49);
    }
    
    public void scenario1() {
        cowboy = new Vec(3,5);
        model.add(WorldModel.AGENT, cowboy.getLocation(model));

        cows = new Vec[5];
        cows[0] = new Vec(6,7);
        cows[1] = new Vec(5,30);
        cows[2] = new Vec(4,8);
        cows[3] = new Vec(5,10);
        cows[4] = new Vec(0,1);

        for (int i=0; i<cows.length; i++)
            model.addCow(cows[i].getLocation(model));

    }
    
    public void scenario2() {
        cowboy = new Vec(11,3);
        model.add(WorldModel.AGENT, cowboy.getLocation(model));
        
        cows = new Vec[9];
        cows[0] = new Vec(8,0);
        cows[1] = new Vec(9,0);
        cows[2] = new Vec(10,0);
        cows[3] = new Vec(8,1);
        cows[4] = new Vec(9,1);
        cows[5] = new Vec(10,1);
        cows[6] = new Vec(8,2);
        cows[7] = new Vec(9,2);
        cows[8] = new Vec(10,2);

        for (int i=0; i<cows.length; i++)
            model.addCow(cows[i].getLocation(model));
        
    }
    
    @Test
    public void testVec() {
        scenario1();
        //assertEquals(new Vec(6,7), cow.add(cowboy));
        //assertEquals(new Location(6,42), cow.add(cowboy).getLocation(model));
        assertEquals(new Location(3,44), cowboy.getLocation(model));
        assertEquals(new Location(1,49), model.getCorralCenter());
        assertEquals(new Vec(3,2), new Vec(6,7).sub(cowboy)); //new Vec(model, cowboy.getLocation(model), cow.add(cowboy).getLocation(model)));
        Vec v = new Vec(3,2);
        assertEquals(v, v.newAngle(v.angle()));
    }
    
    @Test 
    public void testVecSort() {
    	scenario1();
    	
        List<Vec> cowsl = new ArrayList<Vec>(); 
        for (int i=0; i<cows.length; i++) {
            cowsl.add(cows[i]);
        }
        Collections.sort(cowsl);
        assertEquals(new Vec(0,1), cowsl.get(0));
        assertEquals(new Vec(4,8), cowsl.get(1));
        assertEquals(new Vec(6,7), cowsl.get(2));
        assertEquals(new Vec(5,10), cowsl.get(3));
        assertEquals(new Vec(5,30), cowsl.get(4));
    }

    @Test
    public void testCowsRepMat() throws Exception {
        scenario1();
        assertEquals(2, model.getCowsRep(5,38));
        assertEquals(1, model.getCowsRep(5,37));
        assertEquals(0, model.getCowsRep(5,36));
        assertEquals(5, model.getCowsRep(5,40));
    }

    @Test
    public void testAgsRepMat() throws Exception {
        scenario1();
        //assertEquals(4, model.getAgsRep(3,43));
        //assertEquals(2, model.getAgsRep(5,42));
        //assertEquals(0, model.getAgsRep(6,42));
        
        model.add(WorldModel.ENEMY, 4, 44);
        assertEquals(4, model.getAgsRep(3,43));
        assertEquals(2, model.getAgsRep(5,42));
        assertEquals(2, model.getAgsRep(6,42));

        model.remove(WorldModel.ENEMY, 4, 44);
        /*assertEquals(4, model.getAgsRep(3,43));
        assertEquals(2, model.getAgsRep(5,42));
        assertEquals(0, model.getAgsRep(6,42));

        model.remove(WorldModel.AGENT, cowboy.getLocation(model));
        */
        assertEquals(0, model.getAgsRep(3,43));
        assertEquals(0, model.getAgsRep(5,42));
        assertEquals(0, model.getAgsRep(6,42));
        
        assertEquals(2, model.getObsRep(6, 44));
        assertEquals(1, model.getObsRep(7, 43));
        assertEquals(0, model.getObsRep(5, 44));
    }

    @Test
    public void testAStar1() throws Exception {
        scenario1();
        Search s = new Search(model, cowboy.getLocation(model), new Location(8,37), null, true, true, true, false, null);
        Nodo path = s.search();
        assertEquals(13, s.normalPath(path).size());
    }

    @Test
    public void moveCows2() throws Exception {
        scenario1();
        
        List<Vec> cowsl = new ArrayList<Vec>(); 
        for (int i=0; i<cows.length; i++) {
            cowsl.add(cows[i]);
        }

        // find center/clusterise
        cowsl = herd_position.cluster(cowsl, WorldModel.cowPerceptionRatio);
        //Vec stddev = Vec.stddev(cowsl, Vec.mean(cowsl));
        assertEquals(3, cowsl.size());
        
        Vec mean   = Vec.mean(cowsl);
        assertEquals(new Vec(5,8), mean);
        
        //Search s = new Search(model, mean.getLocation(model), model.getCorralCenter(), null, false, false, false, true, null);
        //List<Nodo> np = s.normalPath(s.search());

        int stepsFromCenter = (int)Math.round(Vec.max(cowsl).sub(mean).magnitude())+1;
        assertEquals(3, stepsFromCenter);
        
        herd_position hp = new herd_position();
        hp.setModel(model);
        
        Location byIA =  hp.getAgTarget(Formation.one, cowboy.getLocation(model));
        assertEquals(new Location(6,38), byIA);
        
        byIA =  hp.getAgTarget(Formation.six, cowboy.getLocation(model));
        assertEquals(new Location(6,39), byIA);
        
        // add an agent in 6,39
        model.add(WorldModel.AGENT, 6,39);
        byIA =  hp.getAgTarget(Formation.six, cowboy.getLocation(model));
        assertEquals(new Location(5,38), byIA);        

        // add an agent in 5,38
        model.add(WorldModel.AGENT, 5,38);
        byIA =  hp.getAgTarget(Formation.six,cowboy.getLocation(model));
        assertEquals(new Location(7,42), byIA);        

        // add an agent in 7,42
        model.add(WorldModel.AGENT, 7,42);
        byIA =  hp.getAgTarget(Formation.six,cowboy.getLocation(model));
        assertEquals(new Location(4,38), byIA);        

        // add an agent in 4,38
        model.add(WorldModel.AGENT, 4,38);
        byIA =  hp.getAgTarget(Formation.six,cowboy.getLocation(model));
        assertEquals(null, byIA);        

        // add an agent in 5,37
        //model.add(WorldModel.AGENT, 5,37);
        //byIA =  new herd_position().getAgTarget(model, Formation.six,cowboy.getLocation(model));
        //assertEquals(new Location(5,37), byIA);        
    }

    @Test 
    public void moveCows3() throws Exception {
        scenario2();
        model.add(WorldModel.ENEMY, 11,48);

        List<Vec> cowsl = new ArrayList<Vec>(); 
        for (int i=0; i<cows.length; i++) {
            cowsl.add(cows[i]);
        }

        cowsl = herd_position.cluster(cowsl, WorldModel.cowPerceptionRatio);
        assertEquals(9, cowsl.size());
        
        herd_position hp = new herd_position();
        hp.setModel(model);
        
        Location byIA =  hp.getAgTarget(Formation.one, cowboy.getLocation(model));
        assertEquals(new Location(11,49), byIA);
    }

    @Test 
    public void scouterPos() throws Exception {
        scenario1();
        Location byIA =  new scouter_pos().getScouterTarget(model, new Location(2,46), new Location(6,42));
        assertEquals(new Location(5,49), byIA);

        byIA =  new scouter_pos().getScouterTarget(model, new Location(2,46), new Location(3,42));
        assertEquals(new Location(6,47), byIA);

        byIA =  new scouter_pos().getScouterTarget(model, new Location(9,46), new Location(9,42));
        assertEquals(new Location(22,46), byIA);
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

    
    /*
    @Test
    public void moveCows1() throws Exception {
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
    */

    /*
    @Test
    public void moveCows2() throws Exception {
        scenario1();
        
        List<Vec> cowsTarget = new ArrayList<Vec>(); 
        for (int i=0; i<cows.length; i++) {
            cowsTarget.add(cows[i]);
        }

        // find center/clusterise
        cowsTarget = Vec.cluster(cowsTarget, 2);
        Vec stddev = Vec.stddev(cowsTarget);
        assertTrue(stddev.magnitude() < 3);
        
        Vec mean   = Vec.mean(cowsTarget);
        assertEquals(new Vec(5,8), mean);
        
        int stepsFromCenter = (int)Vec.max(cowsTarget).sub(mean).magnitude()+1;
        assertEquals(3, stepsFromCenter);
        
        // run A* to see the cluster target in n steps
        Search s = new Search(model, mean.getLocation(model), model.getCorralCenter(), null, false, false, false, null);
        List<Nodo> np = s.normalPath(s.search());
        int n = Math.min(stepsFromCenter, np.size());
        assertEquals(3, n);

        Vec ctarget = new Vec(model, s.getNodeLocation(np.get(n)));
        assertEquals(new Vec(3,5), ctarget);
        
        Vec agTarget = mean.sub(ctarget).product(1.0).add(mean);
        assertEquals(new Vec(7,11), agTarget);
        
        Location byIA =  new herd_position().getAgTarget(model);
        assertEquals(byIA, agTarget.getLocation(model));
    }
	*/
    

}
