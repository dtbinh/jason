
package myp;

import jason.asSemantics.Agent;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.BodyLiteral.BodyType;
import jason.asSyntax.directives.Directive;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pattern that add .print in the begin and end of plans.
 
 * @author jomi
 */
public class LoggerDirective implements Directive {

    static Logger logger = Logger.getLogger(LoggerDirective.class.getName());

    public Agent process(Pred directive, Agent ag) {
        try {
        	Agent newAg = new Agent();
            // add .print(te) in the begin and end of the plan
            for (Plan p: ag.getPL()) {
                Literal print1 = Literal.parseLiteral(".print(\"Entering \","+p.getTriggerEvent().getLiteral()+")");
                BodyLiteral b1 = new BodyLiteral(BodyType.internalAction, print1);
                p.getBody().add(0,b1);

                Literal print2 = Literal.parseLiteral(".print(\"Leaving \","+p.getTriggerEvent().getLiteral()+")");
                BodyLiteral b2 = new BodyLiteral(BodyType.internalAction, print2);
                p.getBody().add(b2);
                
                newAg.getPL().add(p);
            }
            return newAg;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}

