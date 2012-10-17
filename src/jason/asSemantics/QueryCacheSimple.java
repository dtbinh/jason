package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.profiling.QueryProfiling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QueryCacheSimple {

    //private Agent ag; 
    private QueryProfiling prof;
    
    private Map<Literal, List<Unifier>> cache = null;
    
    /*
    private int nbUpdateCycles = 0;
    private int nbUpdates = 0;
    private int nbReasoningCycles = 0;
    private int uses = 0;
    private int nbQueries = 0;
    private int nbUniqueQueries = 0;
    private int nbQueriesNoCache = 0;
    private long timeForQueries = 0;
    private long timeForUpdates = 0;
    
    private float p = 0;
    private Set<String> uniqueQueries = new HashSet<String>();
    private Set<String> lastUniqueQueries = null;
    
    private static int nbAgs = -1;
    */
    
    protected Logger logger = null;
    
    public QueryCacheSimple(Agent ag, QueryProfiling p) {
        //this.ag   = ag;
        this.prof = p;
        logger    = Logger.getLogger(QueryCacheSimple.class.getName()+"-"+ag.getTS().getUserAgArch().getAgName());
        cache     = new HashMap<Literal,List<Unifier>>();
    }
    
    public void reset() {
        cache.clear();
    }
    
    public Iterator<Unifier> getCache(final Literal f) {
        List<Unifier> l = cache.get(f);
        if (l == null)
            return null;
        if (prof != null)
            prof.incHits();
        return l.iterator();
    }

    public void queryFinished(Literal f, List<Unifier> r) {
        //System.out.println("finished "+f+" with "+r);
        cache.put(f, r);
    }

    /*
    public void newReasoningCycle(int n) {
        nbReasoningCycles = n;
    }

    public void newUpdateCycle(int n, int u, long time) {
        nbUpdateCycles++; // = n;  
        nbUpdates += u;
        
        timeForUpdates += time;
        
        //System.out.println("counting "+uniqueQueries+" cache="+cache);

        int uqSize = uniqueQueries.size();
        //System.out.println(lastUniqueQueries+" intersect "+uniqueQueries);
        if (lastUniqueQueries != null) {
            if (uqSize != 0) {
                lastUniqueQueries.retainAll(uniqueQueries);
                p += (float)lastUniqueQueries.size()/uqSize;
                //System.out.println("*******     "+lastUniqueQueries.size()+"/"+uqSize+"="+(float)lastUniqueQueries.size()/uqSize+ " : "+p);
                //if (lastUniqueQueries.size() > uqSize) {
                //    System.out.println("      ***** "+lastUniqueQueries+" intersect "+uniqueQueries);
                //}
            }
        }
        nbUniqueQueries += uqSize; 
        
        lastUniqueQueries = uniqueQueries; 
        cache.clear();
        uniqueQueries = new HashSet<String>();
        
        if (nbAgs < 0)
            try {
                nbAgs = ag.getTS().getUserAgArch().getRuntimeServices().getAgentsQty();
            } catch (Exception e) {
                logger.fine("Error getting number of agents: "+e);
            }
    }
*/
    
    /*
    public static int nbStops = 0;
    public static float nT = 0;
    public static float pT = 0;
    public static float uT = 0;
    public static float cqryT = 0;
    public static float cupdT = 0;
    //private static int nbCyclesT = 0;
    private static float usesT = 0;
    private static float nbQueriesT = 0;
    private static float nbUniqueQueriesT = 0;
    private static int nbAgsT = 0;
    private static int nbupdateCyclesT = 0;
    
    public int getNbUses() {
        return uses;
    }
    
    public float getP() {
        return p;
    }

    public void newQueryStared(Literal l) {
        uniqueQueries.add(l.toString());
        nbQueries++;        
    }
    
    public void addQueryTime(long ns) {
        nbQueriesNoCache++;
        timeForQueries += ns;
    }
    
    public void stop() {
        float N = (float)nbQueries/nbUpdateCycles;
        float K = (float)nbUniqueQueries/nbUpdateCycles;
        float n = N/K;
        float u = (float)nbUpdates/nbUpdateCycles;
        float cqry = (float)timeForQueries/nbQueriesNoCache;
        float cupd = (float)timeForUpdates/nbUpdateCycles;
        
        p = p / nbUpdateCycles;
        
        if (K>0) {
            nbAgsT++;
            nbQueriesT         += N;
            nbUniqueQueriesT   += K;
            nT                 += n;
            pT                 += p;
            uT                 += u;
            cqryT              += cqry;
            cupdT              += cupd;
            usesT              += ((float)uses/nbUpdateCycles);    
            nbupdateCyclesT    += nbUpdateCycles;
        }
        
        logger.info("Number of update cycles               : "+nbUpdateCycles);
        logger.info("Number of reasoning cycles            : "+nbReasoningCycles);
        logger.info("Queries by cycle                  (N) : "+ N);
        logger.info("Number of unique queries by cycle (K) : "+ K);
        logger.info("N/K                               (n) : "+ n);
        logger.info("% queries from last cycle         (p) : "+ p);
        logger.info("Query cost                     (Cqry) : "+ cqry+ " ns");
        logger.info("Number of updates by cycle        (U) : "+ u);
        logger.info("Update cost                    (Cupd) : "+ cupd+ " ns");
        logger.info("Query cache reused by cycle           : "+ (float)uses/nbUpdateCycles);

        nbStops++;
        if (nbStops == nbAgs) {
            logger.info("* Number of reasoning cycles            : "+ nbupdateCyclesT/nbAgsT);
            logger.info("* Queries by cycle                  (N) : "+ nbQueriesT/nbAgsT);
            logger.info("* Number of unique queries by cycle (K) : "+ nbUniqueQueriesT/nbAgsT);
            logger.info("* N/K                               (n) : "+ nT/nbAgsT);            
            logger.info("* % queries from last cycle         (p) : "+ pT/nbAgsT);
            logger.info("* Query cost                     (Cqry) : "+ (cqry/nbAgsT)+ " ns");
            logger.info("* Number of updates by cycle        (U) : "+ uT/nbAgsT);
            logger.info("* Update cost                    (Cupd) : "+ (cupdT/nbAgsT)+ " ns");
            logger.info("* Query cache reused by cycle           : "+ usesT/nbAgsT);
        }
        
    }    */
}
