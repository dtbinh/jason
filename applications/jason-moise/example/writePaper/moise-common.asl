// Common plans for organised agents based on MOISE+ model.
//
// These plans use the beliefs:
//     . desiredRole(<GrSpec>,<Role>) and
//     . desiredMission(<SchSpec>,<Mission>).

/* 
   Organisational Events
   ---------------------
*/

/* Structural events */
// when a group is created and I desire to play in it,
// adopts a role  
+group(GrSpec,Id) 
   :  desiredRole(GrSpec,Role)
   <- jmoise.adoptRole(Role,Id).

   
/* Functional events */

// finish the scheme if it has no more players
// and it was created by me
+schPlayers(Sch,0) 
   :  .myName(Me) & scheme(_, Sch)[owner(Me)]
   <- jmoise.finishScheme(Sch).

   
/* Deontic events */

// when I have an obligation or permission to a mission 
// and I desire it, commit
+obligation(Sch, Mission) 
   :  scheme(SchSpec,Sch) & desiredMission(SchSpec, Mission)
   <- jmoise.commitToMission(Mission,Sch).
+permission(Sch, Mission)
   :  scheme(SchSpec,Sch) & desiredMission(SchSpec, Mission)
   <- jmoise.commitToMission(Mission,Sch).

// when the root goal of the scheme is satisfied, 
// remove my missions
+goalState(Sch, G[root], satisfied) 
   :  true
   <- jmoise.removeMission(Sch).
   
+error(M)[source(orgManager)] <- .print("Error in organisational action: ",M); -error(M).
   
