package ora4mas;

import java.util.logging.Level;
import java.util.logging.Logger;

import c4jason.CartagoEnvironment;
import cartago.CartagoNode;
import cartago.CartagoService;
import cartago.ICartagoContext;
import cartago.security.UserIdCredential;

public class CartagoOrgEnvironment extends CartagoEnvironment {
    private Logger logger = Logger.getLogger("ora4mas."+CartagoOrgEnvironment.class.getName());

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        int port = CartagoNode.DEFAULT_PORT;
        try {
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // install cartago node and create the ora4mas workspace
            CartagoService.installNode(port);
            ICartagoContext defaultContext = CartagoService.joinWorkspace("default",null,null, new UserIdCredential("jason_environemnt"), null);
            logger.info("creating workspace "+args[0]);
            defaultContext.createWorkspace(args[0]); //ORA4MASConstants.ORA4MAS_WSNAME);
            Thread.sleep(500); // give some time for cartago to finish the process
        } catch (Exception ex){
            logger.log(Level.SEVERE, "Infrastructure already installed on port "+port+" on localhost.", ex);
        }
        super.init(args);        
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
