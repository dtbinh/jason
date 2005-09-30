
import jason.stdlib.*;
import org.apache.log4j.*;

/** 
 *   This program creates a new agent for SACI infrastructure.
 *   This new agent is named "anotherAg" and enters in a
 *   MAS called "createAgDemo" that must be already running.
 */
public class CreateAgDemo {
  public static void main(String[] args) {
      // setup a logger
      PropertyConfigurator.configure(createAgent.class.getResource("/log4j.configuration"));

      // calls an Internal Action to create the agent
      new createAgent().createSaciAg(
         "anotherAg",     // agent name
         "createAgDemo",  // MAS name
         "ag1.asl",       // AgentSpeak source
         false);          // synchronous execution is false
  }
}