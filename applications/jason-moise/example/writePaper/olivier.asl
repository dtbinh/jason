/* 
   Beliefs
*/

refs([boissier04,sichman03]). // refs used in the paper

// I want to play "writer" in "wpgroups"
desired_role(wpgroup,writer).

// I want to commit to "mColaborator" and "mBib" missions
// in "writePaperSch" schemes
desired_mission(writePaperSch,mColaborator).
desired_mission(writePaperSch,mBib).


// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

// a generic plan for organisational goals (they have scheme(_) annotation)
+!X[scheme(Sch)] : true 
   <- .print("doing organisational goal ",X);
      jmoise.set_goal_state(Sch,X,satisfied).

// when I receive a tell message from S and
// S plays writer in a scheme, change the belief of
// used refs
+useRef(NewRef)[source(S)] 
   :  play(S, writer,Gr) & refs(R)
   <- .print("adding ref ",NewRef, " to ", R);
      -refs(R); +refs([NewRef|R]).
      
