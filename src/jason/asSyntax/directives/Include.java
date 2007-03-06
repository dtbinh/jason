package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.parser.as2j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implementation of the <code>include</code> directive. */
public class Include implements Directive {

    static Logger logger = Logger.getLogger(Include.class.getName());

    public Agent process(Pred directive, Agent ag) {
        return processInclude(((StringTerm)directive.getTerm(0)).getString());
    }

    Agent processInclude(String asFileName) {
        try {
        	Agent ag = new Agent();
            as2j parser = new as2j(new FileInputStream(asFileName));
            parser.agent(ag);
            logger.fine("as2j: AgentSpeak program '"+asFileName+"' parsed successfully!");
            return ag;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"as2j: error parsing \"" + asFileName + "\"", e);
        }
        return null;
    }
}
