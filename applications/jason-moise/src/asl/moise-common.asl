// Common plans for organised agents based on MOISE+ model.
//
// These plans use the beliefs:
//     . desired_role(<GrSpec>,<Role>) and
//     . desired_mission(<SchSpec>,<Mission>).

/* 
   Organisational Events
   ---------------------
*/

/* Structural events */
// when a group is created and I desire to play in it,
// adopts a role  
+group(GrSpec,Id) 
   :  desired_role(GrSpec,Role)
   <- jmoise.adopt_role(Role,Id).

   
/* Functional events */

// finish the scheme if it has no more players
// and it was created by me
+sch_players(Sch,0) 
   :  .my_name(Me) & scheme(_, Sch)[owner(Me)]
   <- jmoise.remove_scheme(Sch).

   
/* Deontic events */

// when I have an obligation or permission to a mission 
// and I desire it, commit
+obligation(Sch, Mission) 
   :  scheme(SchSpec,Sch) & desired_mission(SchSpec, Mission)
   <- jmoise.commit_mission(Mission,Sch).
+obligation(Sch, Mission) 
   :  not scheme(SchSpec,Sch)
   <- .println("I do not understand why I have an obligation for a scheme I do not know! Scheme:",Sch," Mission:", Mission).
+permission(Sch, Mission)
   :  scheme(SchSpec,Sch) & desired_mission(SchSpec, Mission)
   <- jmoise.commit_mission(Mission,Sch).
+permission(Sch, Mission) 
   :  not scheme(SchSpec,Sch)
   <- .println("I do not understand why I have a permission for a scheme I do not know! Scheme:",Sch," Mission:", Mission).

// when the root goal of the scheme is satisfied, 
// remove my missions
+goal_state(Sch, G[root], satisfied) 
   :  true
   <- jmoise.remove_mission(Sch).
   
+error(M)[source(orgManager)] <- .print("Error in organisational action: ",M); -error(M).

