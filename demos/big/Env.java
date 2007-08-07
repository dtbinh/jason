// Environment code for project big.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

public class Env extends jason.environment.Environment {

    private Logger logger = Logger.getLogger("big.mas2j."+Env.class.getName());

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info("executing: "+action);
        return true;
    }
}

