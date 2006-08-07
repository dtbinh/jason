/* 
   Beliefs
*/

refs([boissier04,sichman03]). // refs used in the paper

// I want to play "writer" in "wpgroups"
desiredRole(wpgroup,writer).

// I want to commit to "mColaborator" and "mBib" missions
// in "writePaperSch" schemes
desiredMission(writePaperSch,mColaborator).
desiredMission(writePaperSch,mBib).


// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

// a generic plan for organisational goals (they have scheme(_) annotation)
+!X[scheme(Sch)] : true 
   <- .print("doing organisational goal ",X);
      jmoise.setGoalState(Sch,X,satisfied).

// when I receive a tell message from S and
// S plays writer in a scheme, change the belief of
// used refs
+useRef(NewRef)[source(S)] 
   :  play(S, writer,Gr) & refs(R)
   <- .print("adding ref ",NewRef, " to ", R);
      -refs(R); +refs([NewRef|R]).
      
