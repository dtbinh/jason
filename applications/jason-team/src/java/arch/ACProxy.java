package arch;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**  
 * Handle the (XML) communication with contest simulator. 
 * 
 * @author Jomi
 */
public class ACProxy extends ACAgent {

    String         rid; // the response id of the current cycle
	ACArchitecture arq;
	
	private Logger logger = Logger.getLogger(ACProxy.class.getName());
	//private Transformer transformer;
	private DocumentBuilder documentbuilder;

	ConnectionMonitor monitor = new ConnectionMonitor();
	
	public ACProxy(ACArchitecture arq, String host, int port, String username, String password) {
		logger = Logger.getLogger(ACProxy.class.getName()+"."+arq.getAgName());
		//logger.setLevel(Level.FINE);
		
		if (host.startsWith("\"")) {
			host = host.substring(1,host.length()-1);
		}
		setPort(port);
		setHost(host);
		setUsername(username);
		setPassword(password);
		this.arq = arq;
		try {
			documentbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			//transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		monitor.start();
	}
	
	public void processLogIn() {
		logger.info("---#-#-#-#-#-#-- login ok.");
	}

	public void processSimulationStart(Element simulation, long currenttime) {
		try {
			//opponent = simulation.getAttribute("opponent");
			//arq.addBel(Literal.parseLiteral("opponent("+simulationID+","+opponent+")"));
            arq.setSimId(simulation.getAttribute("id"));

			int gsizex = Integer.parseInt(simulation.getAttribute("gsizex"));
			int gsizey = Integer.parseInt(simulation.getAttribute("gsizey"));
            arq.gsizePerceived(gsizex,gsizey, simulation.getAttribute("opponent"));

			int corralx0 = Integer.parseInt(simulation.getAttribute("corralx0"));
            int corralx1 = Integer.parseInt(simulation.getAttribute("corralx1"));
            int corraly0 = Integer.parseInt(simulation.getAttribute("corraly0"));
            int corraly1 = Integer.parseInt(simulation.getAttribute("corraly1"));
            arq.corralPerceived(new Location(corralx0, corraly0), new Location(corralx1, corraly1));

			int steps  = Integer.parseInt(simulation.getAttribute("steps"));
            arq.stepsPerceived(steps);
			
			logger.info("Start simulation processed ok!");

			rid = simulation.getAttribute("id");
			sendAction(null); // TODO: check is still needed. the start requires an answer!
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error processing start",e);
		}
	}

	public void processSimulationEnd(Element result, long currenttime) {
		try {
            String score = result.getAttribute("score") +"-"+ result.getAttribute("result");
			logger.info("End of simulation :"+score);
            arq.simulationEndPerceived(result.getAttribute("result"));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error processing end",e);
		}
	}

    
	public void processRequestAction(Element perception, long currenttime, long deadline) {
		try {
			List<Literal> percepts = new ArrayList<Literal>();
			
			rid = perception.getAttribute("id");
			int agx   = Integer.parseInt(perception.getAttribute("posx"));
			int agy   = Integer.parseInt(perception.getAttribute("posy"));
			int step  = Integer.parseInt(perception.getAttribute("step"));
            int score = Integer.parseInt(perception.getAttribute("score"));

            // update model
			arq.locationPerceived(agx, agy);
			// TODO: udpate my score (show in the interface)

            // add location in perception
			Literal lpos = new Literal("pos");
			lpos.addTerm(new NumberTermImpl(agx));
			lpos.addTerm(new NumberTermImpl(agy));
    		lpos.addTerm(new NumberTermImpl(step));
			percepts.add(lpos);

			// add in perception what is around
			NodeList nl = perception.getElementsByTagName("cell");
			for (int i=0; i < nl.getLength(); i++) {
				Element cell = (Element)nl.item(i);
				int cellx = Integer.parseInt(cell.getAttribute("x"));
                int celly = Integer.parseInt(cell.getAttribute("y"));
				int absx  = agx - cellx;
				int absy  = agy - celly;
				
				NodeList cnl = cell.getChildNodes();
				for (int j=0; j < cnl.getLength(); j++) {
					if (cnl.item(j).getNodeType() == Element.ELEMENT_NODE) {

						Element type = (Element)cnl.item(j);
						
						if (type.getNodeName().equals("agent")) {
							if (type.getAttribute("type").equals("ally")) {
								percepts.add(MinerArch.createCellPerception(cellx, celly, MinerArch.aALLY));
							} else if (type.getAttribute("type").equals("enemy")) {
								arq.enemyPerceived(absx, absy);
								percepts.add(MinerArch.createCellPerception(cellx, celly, MinerArch.aENEMY));
							}
                            
                        } else if (type.getNodeName().equals("cow")) {
                            int cowId = Integer.parseInt(type.getAttribute("ID"));
                            Literal lc = new Literal("cow");
                            lc.addTerm(new NumberTermImpl( cowId ));
                            percepts.add(MinerArch.createCellPerception(cellx, celly, lc));
                            arq.cowPerceived(absx, absy);
                            
                        } else if (type.getNodeName().equals("obstacle")) { 
							arq.obstaclePerceived(absx, absy, MinerArch.createCellPerception(cellx, celly, MinerArch.aOBSTACLE));
                        } else if (type.getNodeName().equals("corral") && type.getAttribute("type").equals("enemy")) { 
                            arq.obstaclePerceived(absx, absy, MinerArch.createCellPerception(cellx, celly, MinerArch.aOBSTACLE));
                            
                        } else if (type.getNodeName().equals("empty")) {
                            percepts.add(MinerArch.createCellPerception(cellx, celly, MinerArch.aEMPTY));
						}
					}
				}
			}
	
			//if (logger.isLoggable(Level.FINE)) 
			logger.info("Request action for "+lpos+" / "+rid + " percepts: "+percepts);
			
			arq.startNextStep(step,percepts);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error processing request",e);
		}
	}

	public void sendAction(String action) {
		try {
			logger.info("sending action "+action+" for step "+rid+" at "+arq.model.getAgPos(arq.getMyId()) );
			Document doc = documentbuilder.newDocument();
			Element el_response = doc.createElement("message");
			
			el_response.setAttribute("type","action");
			doc.appendChild(el_response);

			Element el_action = doc.createElement("action");
			if (action != null) {
				el_action.setAttribute("type", action);
			}
			el_action.setAttribute("id",rid);
			el_response.appendChild(el_action);

			sendDocument(doc);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error sending action.",e);
		}
	}

	@Override
	public void processPong(String pong) {
		monitor.processPong(pong);
	}

	/** checks the connection */
	class ConnectionMonitor extends Thread {
		long    sentTime = 0;
		int     count = 0;
		boolean ok = true;
		
		public void run() {
			int d = new Random().nextInt(10000);
            try {
                while (true) {
					sleep(20000+d);
					count++;
					ok = false;
					sentTime = System.currentTimeMillis();
					if (isConnected()) {
						sendPing("test:"+count);
						waitPong();
					}
					if (!ok) {
						logger.info("I likely loose my connection, reconnecting!");
						//reconnect();
						connect();
					}
			    }
            } catch (Exception e) {
                logger.log(Level.WARNING,"Error in communication ",e);
            }
		}
		
		synchronized void waitPong() throws Exception {
			wait(4000);
		}
		
		synchronized void processPong(String pong) {
			long time = System.currentTimeMillis() - sentTime;
			logger.info("Pong "+pong+" in "+time+" milisec");
			ok = true;
			notify();
		}
	}
}
