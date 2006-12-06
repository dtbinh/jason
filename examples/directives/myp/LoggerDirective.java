
package myp;

import jason.asSyntax.*;
import jason.asSyntax.directives.*;
import jason.asSyntax.BodyLiteral.BodyType;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pattern that add .print in the begin and end of plans.
 
 * @author jomi
 */
public class LoggerDirective implements Directive {

    static Logger logger = Logger.getLogger(LoggerDirective.class.getName());

    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            // add .print(te) in the begin and end of the plan
            for (Plan p: innerPlans) {
                Literal print1 = Literal.parseLiteral(".print(\"Entering \","+p.getTriggerEvent().getLiteral()+")");
                BodyLiteral b1 = new BodyLiteral(BodyType.internalAction, print1);
                p.getBody().add(0,b1);

                Literal print2 = Literal.parseLiteral(".print(\"Leaving \","+p.getTriggerEvent().getLiteral()+")");
                BodyLiteral b2 = new BodyLiteral(BodyType.internalAction, print2);
                p.getBody().add(b2);
                
                pl.add(p);
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}

