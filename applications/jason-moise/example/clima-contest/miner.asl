
init.

// create a group for mining
+init : .myName(miner1) <- jmoise.createGroup(team).


/* 
   Organisational Events
   ---------------------
*/

// when a team is created, adopts the role miner
// miner1 is also the leader
+group(team,GId) : .myName(miner1) 
   <- jmoise.adoptRole(miner,GId);
      jmoise.adoptRole(leader,GId).

// when a team is created, adopts the role miner
// miner2 is also the courier
+group(team,GId) : .myName(miner2) 
   <- jmoise.adoptRole(miner,GId);
      jmoise.adoptRole(courier,GId).

// when a team is created, adopts the role miner
// miner3 also starts a meeting poinr scheme
+group(team,GId) : .myName(miner3) 
   <- jmoise.adoptRole(miner,GId);
      jmoise.startScheme(meeting_point).

// when a team is created, adopts the role miner
+group(team,GId) : true 
   <- jmoise.adoptRole(miner,GId).

// finish the scheme if it has no more players
+schPlayers(Sch,0) 
   : .myName(A) & scheme(_, Sch)[owner(A)]
   <- jmoise.finishScheme(Sch).
  

// when a meeting point scheme is created,
// add a responsible group for it if I created it
+scheme(meeting_point,SId)[owner(A)] 
   : group(team,GId) & .myName(A)
   <- jmoise.addResponsibleGroup(SId, GId).
   
// when I have an obligation for a mission, commit to it
+obligation( Sch, Mission) : true 
   <- jmoise.commitToMission(Mission,Sch).

// when I have a permission to mMiner mission in the meeting point scheme,
// commit to it if I am miner3
+permission(Sch, mMiner) 
   :  scheme(meeting_point, Sch) & .myName(miner3) 
   <- jmoise.commitToMission(mMiner,Sch).

// when the root goal of the scheme is satisfied, remove my missions
+goalState(Sch, mp, satisfied) : true
   <- jmoise.removeMission(Sch).
   
/*
   Organisational Goals' plans
   ---------------------------
*/

// a generic plan for organisational goals (they have scheme(_) annotation)
+!minerCarryGoldDepot[scheme(Sch)] : true
   <- .print("gold at depot!!! ");
      jmoise.setGoalState(Sch,minerCarryGoldDepot,satisfied);
      jmoise.setGoalState(Sch,goldAtDepot,satisfied).

+!mp[scheme(Sch)] : true 
   <- .print("***** FINISH! *****");
      jmoise.setGoalState(Sch,mp,satisfied).

      
// a generic plan for organisational goals (they have scheme(_) annotation)
+!X[scheme(Sch)] : X \== agreeMP & X \== goldAtDepot
   <- .print("doing organisational goal ",X);
      jmoise.setGoalState(Sch,X,satisfied).

+!X[scheme(Sch)] : true
   <- .print("discarting organisational goal ",X).
 
   
