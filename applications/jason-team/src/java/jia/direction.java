package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.Random;
import java.util.logging.Level;

import busca.Nodo;

import arch.CowboyArch;
import arch.LocalWorldModel;
import env.WorldModel;

/** 
 * Gives the direction (up, down, left, right) towards some location.
 * Uses A* for this task.
 *  
 * @author jomi
 */
public class direction extends DefaultInternalAction {
    
    WorldModel.Move[] actionsOrder = new WorldModel.Move[WorldModel.nbActions];
    Random     random = new Random();
    
    public direction() {
        for (int i=0; i<WorldModel.nbActions; i++) {
            actionsOrder[i] = Search.defaultActions[i];
        }
    }
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = "skip";

            LocalWorldModel model = ((CowboyArch)ts.getUserAgArch()).getModel();
    
            int iagx = (int)((NumberTerm)terms[0]).solve();
            int iagy = (int)((NumberTerm)terms[1]).solve();
            int itox = (int)((NumberTerm)terms[2]).solve();
            int itoy = (int)((NumberTerm)terms[3]).solve();

            if (model.inGrid(itox,itoy)) {
                // destination should be a free place
                while (!model.isFreeOfObstacle(itox,itoy) && itox > 0) itox--;
                while (!model.isFreeOfObstacle(itox,itoy) && itox < model.getWidth()) itox++;

                Location from = new Location(iagx, iagy);
                Location to   = new Location(itox, itoy);
                
                // randomly change the place of two actions in actionsOrder
                int i1 = random.nextInt(WorldModel.nbActions);
                int i2 = random.nextInt(WorldModel.nbActions);
                WorldModel.Move temp = actionsOrder[i2];
                actionsOrder[i2] = actionsOrder[i1];
                actionsOrder[i1] = temp;
                
                Search astar    = new Search(model, from, to, actionsOrder, true, false, true, false, ts.getUserAgArch());
                Nodo   solution = astar.search();

                if (solution == null) {
                    // Test impossible path
                    Search s = new Search(model, from, to, ts.getUserAgArch()); // search without agent/cows as obstacles
                    int fixtimes = 0;
                    while (s.search() == null && ts.getUserAgArch().isRunning() && fixtimes < 10) {
                        fixtimes++; // to avoid being in this loop forever 
                        // if search is null, it is impossible in the scenario to goto n, set it as obstacle  
                        ts.getLogger().info("[direction] No possible path to "+to+" setting as obstacle.");
                        model.add(WorldModel.OBSTACLE, to);
                        to = model.nearFree(to, null);
                        s = new Search(model, from, to, ts.getUserAgArch());
                    }
                    
                    // run A* again
                    astar    = new Search(model, from, to, actionsOrder, true, false, true, false, ts.getUserAgArch());
                    solution = astar.search();
                }

                if (solution != null) {
                    WorldModel.Move m = astar.firstAction(solution);
                    if (m != null)
                        sAction = m.toString();
                } else {
                    ts.getLogger().info("No route from "+from+" to "+to+"!"+"\n"+model);
                }
            }
            return un.unifies(terms[4], new Atom(sAction));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "direction error: "+e, e);    		
        }
        return false;
    }
}

