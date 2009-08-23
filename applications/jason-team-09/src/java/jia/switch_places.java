package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Level;

import busca.Nodo;

import arch.CowboyArch;
import arch.LocalWorldModel;

/**
 * 
 * Given the switch, determines the two places where an agent should stand in order to open the fence
 * 
 * @author ricardo.hahn
 *
 */

public class switch_places extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	CowboyArch arch       = (CowboyArch)ts.getUserAgArch();
            LocalWorldModel model = arch.getModel();
            if (model == null)
            	return false;
            
            int lx = (int)((NumberTerm)args[0]).solve();
            int ly = (int)((NumberTerm)args[1]).solve();
            Location switchPlace = new Location(lx, ly);
            Location agPlace = model.getAgPos(arch.getMyId());
            
            Location[] freeSwitch = new Location[2];
            int[] dist = {-1 , -1};
            
            Location[] d = {new Location(0,1), new Location(0,-1), new Location(1,0), new Location(-1,0) };
            
            for(int k =0; k < 4; ++k) {
            	Location candidate = new Location(switchPlace.x-d[k].x,switchPlace.y-d[k].y);
            	if(model.inGrid(candidate) && model.isFreeOfObstacle(candidate) && !model.hasFence(candidate.x, candidate.y)) {

            		Nodo solution = new Search(model, agPlace, candidate, ts.getUserAgArch()).search();

            		if(solution != null) {
            			int length = solution.getProfundidade();
                		ts.getLogger().info("oooo candidate "+candidate.x+" "+candidate.y+" length "+length);
            			if(dist[1]<0 || length<dist[1])
            			{
            				dist[1]=length;
            				freeSwitch[1]=candidate;
            				if(dist[0]<0 || dist[1] < dist[0])
            				{
            					dist[1]=dist[0];
            					dist[0]=length;
            					freeSwitch[1]=freeSwitch[0];
            					freeSwitch[0]=candidate;
            				}
            			}
            		}
            		
            	}
            	
            }
            if(dist[1]>=0)
            	return un.unifies(args[2], new NumberTermImpl(freeSwitch[0].x)) && 
            		un.unifies(args[3], new NumberTermImpl(freeSwitch[0].y)) &&
            		un.unifies(args[4], new NumberTermImpl(freeSwitch[1].x)) && 
            	    un.unifies(args[5], new NumberTermImpl(freeSwitch[1].y));
            } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "switch_places error: "+e, e);    		
        }
        return false;
    }

}

