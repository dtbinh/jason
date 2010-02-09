// Agent alice in project testJMoiseCartago.mas2j

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
+!wsecs <- .print("writing sections...").
+!wrefs <- .print("organising bibliography...").


// plans to react to normative events like obligation created

+obligation(Ag,Norm,committed(Ag,Mission,Scheme),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission);
      commit_mission(Mission,Scheme).
	  
+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      !Goal;
      goal_achieved(Goal,Scheme).
	  
+obligation(Ag,Norm,What,DeadLine)  
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").

