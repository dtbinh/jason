/* Initial beliefs and rules */

/* Initial goals */

!start.
!join.

/* Plans */

+!start 
  <- lookup_artifact("mypaper",_); 
	 adopt_role(writer,mypaper).
-!start
  <- .wait(100);
     !start.
	 
+!join <- .my_name(Me); join_workspace(ora4mas,"",user_id(Me)).
	 	 
// application domain goals
+!wsecs[scheme(S)]
   <- .print("writing sections for scheme ",S,"...").
 
+goal_state(Scheme,wsecs,_,_,statisfied)
    : .my_name(Me) & commitment(Me,mColaborator,Scheme)
   <- .print("sections are ok, leaving my mission....");
      leave_mission(mColaborator,Scheme).

// plans to react to normative events like obligation created

+obligation(Ag,Norm,committed(Ag,mColaborator,Scheme),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to commit to the scheme as a colaborator, so doing that...");
      commit_mission(mColaborator,Scheme).
	  
+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      !Goal[scheme(Scheme)];
      goal_achieved(Goal,Scheme).
	  
+obligation(Ag,Norm,What,DeadLine)  
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").

// signals
+norm_failure(N) <- .print("norm failure event: ", N).
   