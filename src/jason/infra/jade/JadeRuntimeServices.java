package jason.infra.jade;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JadeRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger  = Logger.getLogger(JadeRuntimeServices.class.getName());
    
    private ContainerController cc;
    
    JadeRuntimeServices(ContainerController cc) {
        this.cc = cc;
    }
    
    public boolean createAgent(String agName, String agSource, String agClass, String archClass, ClassParameters bbPars, Settings stts) throws Exception {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating jade agent " + agName + "from source " + agSource + "(agClass=" + agClass + ", archClass=" + archClass + ", settings=" + stts);
            }
    
            AgentParameters ap = new AgentParameters();
            ap.setAgClass(agClass);
            ap.setArchClass(archClass);
            ap.setBB(bbPars);
            ap.asSource = new File(agSource);
            ap.setupDefault();        
            
            if (stts == null) stts = new Settings();
            
            cc.createNewAgent(agName, JadeAgArch.class.getName(), new Object[] { ap, false, false }).start();
            
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating agent " + agName, e);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAgentsName() {
        try {
            // TODO:
            logger.warning("getAgentsName is not implemented yet!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents' name", e);
        }
        return null;
    }

    public int getAgentsQty() {
        try {
            return getAgentsName().size();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents qty", e);
            return 0;
        }
    }

    public boolean killAgent(String agName) {
        try {
            AgentController ac = cc.getAgent(agName);
            if (ac == null) {
                logger.warning("Agent "+agName+" does not exist!");
            } else {
                ac.kill();
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error killing agent", e);
        }
        return false;
    }

   
    public void stopMAS() throws Exception {
        if (cc != null) {
            new Thread() { // this command should not block the agent!
                public void run() {  
                    try {
                        cc.getPlatformController().kill();
                    } catch (ControllerException e) {
                        e.printStackTrace();
                    }  
                }
            }.start();
        }
    }
}
