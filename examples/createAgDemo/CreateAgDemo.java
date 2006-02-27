
import jason.infra.saci.*;

/** 
 *   This program creates a new agent for SACI infrastructure.
 *   This new agent is named "anotherAg" and enters in a
 *   MAS called "createAgDemo" that must be already running.
 */
public class CreateAgDemo {
  public static void main(String[] args) throws Exception {
      // calls an Internal Action to create the agent
      new SaciRuntimeServices("createAgDemo").createAgent(
         "anotherAg",     // agent name
         "ag1.asl",       // AgentSpeak source
	 null,            // default agent class
	 null,            // default architecture class
         null);           // default settings
  }
}