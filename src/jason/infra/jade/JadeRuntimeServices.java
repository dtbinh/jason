package jason.infra.jade;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JadeRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger  = Logger.getLogger(JadeRuntimeServices.class.getName());
    
    private ContainerController cc;
    
    private Agent jadeAgent;
    
    JadeRuntimeServices(ContainerController cc, Agent ag) {
        this.cc = cc;
        jadeAgent = ag;
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

    public AgArch clone(jason.asSemantics.Agent source, String archClassName, String agName) throws JasonException {
        throw new JasonException("clone for JADE is not implemented!");
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAgentsNames() {
        // TODO: make a cache list and update it when a new agent enters the system
        if (jadeAgent == null) return null;
        try {
            Set<String> ags = new HashSet<String>();
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("jason");
            sd.setName(JadeAgArch.dfName);
            template.addServices(sd);
            DFAgentDescription[] ans = DFService.search(jadeAgent, template);
            for (int i=0; i<ans.length; i++) {
                ags.add(ans[i].getName().getLocalName());                
            }
            /*
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults( new Long(-1) );
            AMSAgentDescription[] all = AMSService.search( jadeAgent, new AMSAgentDescription(), c);
            for (AMSAgentDescription ad: all) {
                AID agentID = ad.getName();
                if (    !agentID.getName().startsWith("ams@") && 
                        !agentID.getName().startsWith("df@") &&
                        !agentID.getName().startsWith(RunJadeMAS.environmentName) &&
                        !agentID.getName().startsWith(RunJadeMAS.controllerName)
                   ) {
                    ags.add(agentID.getLocalName());                
                }
            } 
            */       
            return ags;
            //logger.warning("getAgentsName is not implemented yet!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents' name", e);
        }        
        return null;
    }

    /** @deprecated use getAgentsNames() */
    public Set<String> getAgentsName() {
        return getAgentsNames();
    }

    public int getAgentsQty() {
        try {
            return getAgentsNames().size();
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
