
!init.

// create a group for mining
//+!init : .myName(miner1) <- jmoise.createGroup(team).

+!init : .my_name(Me) & jmoise.link(communication,Me,miner2) <- .print(haslink); !status; .wait(1000); !init.
+!init <- .print(hasNOlink); !status; .wait(1000); !init.

+!status
  :  .my_name(Me)
  <- .findall(in(G,R),play(Me,R,G),LR);
     .print("My roles:",LR);
	 .findall(link(T,Ag), jmoise.link(T,Me,Ag), LL);
	 .print("My links:",LL).
	 
	 
/* 
   Beliefs
*/

desired_role(team,miner).

// I want to commit to "mManager" mission in "writePaperSch" schemes
desired_mission(meeting_point,mMiner).

// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/* 
   Organisational Events
   ---------------------
*/


/* Structural events */

// if i am the leader, decide who is the courier
+play(A,leader,GId) : .myName(A)
   <- .send(miner2, achieve, adoptRole(courier, GId)).

// if the leader ask me to adopt a role, i do
+!adoptRole(R,GId)[source(A)]
   : true
   <- jmoise.adoptRole(R,GId).



/* Functional events */

   
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
   <- jmoise.set_goal_arg(Sch,mp,"X", 3);
      jmoise.set_aoal_arg(Sch,mp,"Y", 5);
      jmoise.set_goal_state(Sch,proposeMP,satisfied);
      .print("propose MP -> ok").
      
// a generic plan for organisational goals (they have scheme(_) annotation)
+!X[scheme(Sch)] : X \== agreeMP 
   <- .print("doing organisational goal ",X);
      jmoise.set_goal_state(Sch,X,satisfied).

+!X[scheme(Sch)] : true
   <- .print("discarting organisational goal ",X).
 
   
