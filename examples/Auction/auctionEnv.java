

import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.environment.Environment;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class auctionEnv extends Environment {
    
    public static final Integer NoBid = new Integer(-1);
    public static final byte LastAuc  = 8;
	public static final int  nrTrials = 100;
    public static final byte NAg      = 3;
    
	public static final Literal winner1   = Literal.parseLiteral("winner(ag1)");
    public static final Literal winner2   = Literal.parseLiteral("winner(ag2)");
	public static final Literal winner3   = Literal.parseLiteral("winner(ag3)");

	// old style env
    //public static final Term winner1   = Term.parse("winner(ag1)");
    //public static final Term winner2   = Term.parse("winner(ag2)");
    //public static final Term winner3   = Term.parse("winner(ag3)");
	
	int nauc;
	int trial = 1;
    Map bid = new HashMap();
	Literal auction, winner;
    
	static Logger logger = Logger.getLogger(auctionEnv.class);
		
    public auctionEnv() {
		init();
    }
    
    public void init() {
        nauc=1;
        bid.put("ag1",NoBid);
        bid.put("ag2",NoBid);
        bid.put("ag3",NoBid);
        
        // Add initial percepts below
        auction = Literal.parseLiteral("auction("+nauc+")");
		clearPercepts();
        addPercept(auction);

		// old style
		//auction = Term.parse("auction("+nauc+")");
		//getPercepts().clear();
		//getPercepts().add(auction);
    }
	
    /**
     * Implementation of the agents' basic actions
     */
    public boolean executeAction(String ag, Term action) {
        if (action.getFunctor().equals("place_bid")) {
            Integer x = new Integer(action.getTerm(1).toString());
            bid.put(ag,x);
        }
        
        if ( ((Integer)bid.get("ag1")).intValue()!=NoBid.intValue() &&
             ((Integer)bid.get("ag2")).intValue()!=NoBid.intValue() &&
             ((Integer)bid.get("ag3")).intValue()!=NoBid.intValue() &&
             nauc<=LastAuc) {
        	
            if (nauc>1) {
                removePercept(winner);
				
				// old style: getPercepts().remove(winner);
            }

			removePercept(auction);
			// old style: getPercepts().remove(auction);
			
            if ( ((Integer)bid.get("ag1")).intValue() >=
                 ((Integer)bid.get("ag2")).intValue() &&
                 ((Integer)bid.get("ag1")).intValue() >=
                 ((Integer)bid.get("ag3")).intValue() ) {
                winner = winner1;
            }
            else if ( ((Integer)bid.get("ag2")).intValue() >=
                      ((Integer)bid.get("ag1")).intValue() &&
                      ((Integer)bid.get("ag2")).intValue() >=
                      ((Integer)bid.get("ag3")).intValue() ) {
                winner = winner2;
            }
            else if ( ((Integer)bid.get("ag3")).intValue() >=
                      ((Integer)bid.get("ag1")).intValue() &&
                      ((Integer)bid.get("ag3")).intValue() >=
                      ((Integer)bid.get("ag2")).intValue() ) {
                winner = winner3;
            }
            
			addPercept(winner);
			// old style: getPercepts().add(winner);
			
            bid.put("ag1",NoBid);
            bid.put("ag2",NoBid);
            bid.put("ag3",NoBid);
            nauc++;
            logger.info("Winner of auction "+auction+": "+winner);
            auction = Literal.parseLiteral("auction("+nauc+")");

			// old style: auction = Term.parse("auction("+nauc+")");
            
			if (nauc<=LastAuc) {
				addPercept(auction);
				
				// old style: getPercepts().add(auction);				
			} else if (trial < nrTrials) {
				trial++;
				logger.info("----------------------- Trial "+trial);
				init();
				addPercept(Literal.parseLiteral("trial("+trial+")"));
				
				// old style: getPercepts().add(Term.parse("trial("+trial+")"));
            } else {
				System.exit(0);
            }
        }
        return true;
    }
}
