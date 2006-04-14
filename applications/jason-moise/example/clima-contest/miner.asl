
init.

// create a group for mining
+init : .myName(miner1) <- jmoise.createGroup(team).


/* 
   Organisational Events
   ---------------------
*/


/* Structural events */

// when a team is created, adopts the role miner
// miner1 is also the leader
+group(team,GId) : .myName(miner1) 
   <- jmoise.adoptRole(leader,GId);
      jmoise.adoptRole(miner,GId).
      
// when a team is created, adopts the role miner
+group(team,GId) : true 
   <- jmoise.adoptRole(miner,GId).

// if i am the leader, decide who is the courier
+play(A,leader,GId) : .myName(A)
   <- .send(miner2, achieve, adoptRole(courier, GId)).

// if the leader ask me to adopt a role, i do
+!adoptRole(R,GId)[source(A)]
   : true
   <- jmoise.adoptRole(R,GId).



/* Functional events */

// miner3 starts a meeting point scheme
+play(miner3,miner,GId) : .myName(miner3)
   <- .print("Creating a MP scheme");
      jmoise.startScheme(meeting_point).

// finish the scheme if it has no more players
+schPlayers(Sch,0) 
   : .myName(A) & scheme(_, Sch)[owner(A)]
   <- jmoise.finishScheme(Sch).
  

// when a meeting point scheme is created,
// add a responsible group for it if I created it
+scheme(meeting_point,SId)[owner(A)] 
   : group(team,GId) & .myName(A)
   <- jmoise.addResponsibleGroup(SId, GId).



/* Deontic events */
   
// when I have an obligation for a mission, commit to it
+obligation( Sch, Mission) : true 
   <- jmoise.commitToMission(Mission,Sch).

// when I have a permission to mMiner mission in the meeting point scheme,
// commit to it if I am miner3
+permission(Sch, mMiner) 
   :  scheme(meeting_point, Sch) & .myName(miner3)
   <- jmoise.commitToMission(mMiner,Sch).

// when the root goal of the scheme is satisfied, remove my missions
+goalState(Sch, G[root], satisfied) 
   :  true
   <- jmoise.removeMission(Sch).


   
/*
   Organisational Goals' plans
   ---------------------------
*/


+!mp(X,Y)[scheme(Sch)] : true 
   <- .print("***** FINISH! *****");
      jmoise.setGoalState(Sch,mp,satisfied).


// a generic plan for organisational goals (they have scheme(_) annotation)
+!goldAtDepot[scheme(Sch)] 
   :  .myName(miner2)
   <- .print("i do not satisfy gold at depot!").

+!proposeMP[scheme(Sch)] 
   :  true
   <- jmoise.setGoalArg(Sch,mp,"X", 3);
      jmoise.setGoalArg(Sch,mp,"Y", 5);
      jmoise.setGoalState(Sch,proposeMP,satisfied);
      .print("propose MP -> ok").
      
// a generic plan for organisational goals (they have scheme(_) annotation)
+!X[scheme(Sch)] : X \== agreeMP 
   <- .print("doing organisational goal ",X);
      jmoise.setGoalState(Sch,X,satisfied).

+!X[scheme(Sch)] : true
   <- .print("discarting organisational goal ",X).
 
   
