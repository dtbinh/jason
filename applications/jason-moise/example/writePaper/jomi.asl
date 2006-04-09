
// Organisational Events
// ------------------------

// when a wpgroup is created, adopts the role writer 
+group(wpgroup,Id) : true 
   <- jmoise.adoptRole(writer,Id).

// when I have an obligation to be a colaborator, 
// commit to it
+obligation(Sch, mColaborator) : true 
   <- jmoise.commitToMission(mColaborator, Sch).

// when the root goal of the scheme is satisfied, 
// remove my missions
+goalState(Sch, wpGoal, satisfied) : true
   <- jmoise.removeMission(Sch).


// Organisational Goals' plans
// ------------------------------

+!wsecs[scheme(Sch)] 
   :   commitment(Ag, mBib, Sch) 
   <- // send a message to the agent committed to mission mBib
      .send(Ag, tell, useRef(bordini05));
      .print("Writing sections!");
      jmoise.setGoalState(Sch, wsecs, satisfied).

// the plan to achieve the goal failed
-!wsecs[scheme(Sch)] : true 
   <- jmoise.setGoalState(Sch, wsecs, impossible).
      