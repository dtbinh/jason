/* 
   Beliefs
*/

// I want to play "writer" in "wpgroups"
desiredRole(wpgroup,writer).

// I want to commit to "mColaborator" mission in "writePaperSch" schemes
desiredMission(writePaperSch,mColaborator).

// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

+!wsecs[scheme(Sch)] 
   :   commitment(Ag, mBib, Sch) 
   <- // send a message to the agent committed to mission mBib
      .send(Ag, tell, useRef(bordini05));
      .print("Writing sections!");
      jmoise.setGoalState(Sch, wsecs, satisfied).

// the plan to achieve the goal failed
-!wsecs[scheme(Sch)] : true 
   <- jmoise.setGoalState(Sch, wsecs, impossible).
      