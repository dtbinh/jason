//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.infra.jade;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jason.control.ExecutionControl;
import jason.control.ExecutionControlInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

/**
 * Concrete execution control implementation for Jade infrastructure.
 */
@SuppressWarnings("serial")
public class JadeExecutionControl extends JadeAg implements ExecutionControlInfraTier {

    public static String controllerOntology = "AS-ExecControl";

    private ExecutionControl userControl;

    ExecutorService executor; // the thread pool used to execute actions
    
    @Override
    public void setup()  {
        logger = Logger.getLogger(JadeExecutionControl.class.getName());
        
        // create the user environment
        try {
            Object[] args = getArguments();
            if (args != null && args.length > 0) {
                if (args[0] instanceof ClassParameters) { // it is an mas2j parameter
                    ClassParameters ecp = (ClassParameters)args[0];
                    userControl = (ExecutionControl) Class.forName(ecp.className).newInstance();
                    userControl.setExecutionControlInfraTier(this);
                    userControl.init(ecp.getParametersArray());
                } else {
                    userControl = (ExecutionControl) Class.forName(args[0].toString()).newInstance();
                    userControl.setExecutionControlInfraTier(this);
                    if (args.length > 1) {
                        logger.warning("Execution control arguments is not implemented yet (ask it to us if you need)!");
                    }
                }
            } else {
                logger.warning("Using default execution control.");
                userControl = new ExecutionControl();
                userControl.setExecutionControlInfraTier(this);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in setup Jade Environment", e);
        }

        executor = Executors.newFixedThreadPool(5);
        
        try {
            // message handler for "informCycleFinished"
            addBehaviour(new CyclicBehaviour() {
                ACLMessage m;
                public void action() {
                    synchronized (syncReceive) {
                        m = receive();
                    }
                    if (m == null) {
                        block(1000);
                    } else {
                        if (!isAskAnswer(m)) {
                            try {
                                @SuppressWarnings("unused")
                                Document o = (Document)m.getContentObject();
                                logger.warning("Received agState too late! in-reply-to:"+m.getInReplyTo());
                            } catch (Exception _) {
                                try {
                                    final String content = m.getContent();
                                    final int p = content.indexOf(",");
                                    if (p > 0) {
                                        final boolean breakpoint = Boolean.parseBoolean(content.substring(0,p));
                                        final int cycle = Integer.parseInt(content.substring(p+1));
                                        executor.execute(new Runnable() {
                                            public void run() {
                                                userControl.receiveFinishedCycle(m.getSender().getLocalName(), breakpoint, cycle);
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, "Error in processing "+m, e);
                                }
                            }
                        }
                    }
                }
            });
            
            informAllAgsToPerformCycle(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agent", e);
        }
    }

    @Override
    protected void takeDown() {
        if (userControl != null) userControl.stop();
    }
    
    public ExecutionControl getUserControl() {
        return userControl;
    }
    
    public void informAgToPerformCycle(String agName, int cycle) {
        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.setOntology(controllerOntology);
        m.addReceiver(new AID(agName, AID.ISLOCALNAME));
        m.setContent("performCycle "+cycle);
        send(m);
    }

    public void informAllAgsToPerformCycle(final int cycle) {
        try {
            ACLMessage m = new ACLMessage(ACLMessage.INFORM);
            m.setOntology(controllerOntology);
            addAllAgsAsReceivers(m);
            m.setContent("performCycle "+cycle);
            send(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in informAllAgsToPerformCycle", e);
        }
    }

    public Document getAgState(String agName) {
        if (agName == null) return null;
        try {
            ACLMessage m = new ACLMessage(ACLMessage.QUERY_REF);
            m.setOntology(controllerOntology);
            m.addReceiver(new AID(agName, AID.ISLOCALNAME));
            m.setContent("agState");
            ACLMessage r = ask(m);
            if (r == null) {
                System.err.println("No agent state received! (possibly timeout in ask)");
            } else {
                return (Document) r.getContentObject();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getAgState", e);
        }
        return null;
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new JadeRuntimeServices(getContainerController(), this);
    }
}
