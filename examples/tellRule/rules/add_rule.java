// Internal action code for project tell-rule.mas2j

package rules;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

public class add_rule extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("tell-rule.mas2j."+add_rule.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	if (args[0].isList()) {
        		for (Term t: (ListTerm)args[0]) {
    	            addRule( ((StringTerm)t).getString(), ts );        			
        		}
        	} else {
	            addRule( ((StringTerm)args[0]).getString(), ts );
        	}
    		return true;
        } catch (Exception e) {
            logger.warning("Error in internal action 'add_rule'! "+e);
        }
        return false;
    }
    
    private void addRule(String sRule, TransitionSystem ts) {
        int p = sRule.indexOf(":-");
        if (p > 0) {
        	//logger.info("Adding rule: "+sRule);
            Literal        head = Literal.parseLiteral(sRule.substring(0,p));
            LogicalFormula body = LogExpr.parseExpr(sRule.substring(p+2));
            ts.getAg().getBB().add(new Rule(head,body));
        } else {
            logger.info(sRule+" is not a rule!");            	
        }    	
    }
}

