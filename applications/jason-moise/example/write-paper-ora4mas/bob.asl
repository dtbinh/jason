// Agent bob in project testJMoiseCartago.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true 
  <- .my_name(Me);
     join_workspace(ora4mas,"",user_id(Me)); 
	 .wait(300);
	 
     //make_artifact("mypaper", "ora4mas.nopl.GroupBoard", [mypaper, "wp-os.xml", wpgroup, false, false	]); 
     create_group(mypaper, "wp-os.xml", wpgroup, false, true);
	 .print("group created");
	 
	 //ora4mas.adopt_role(editor,mypaper);
	 adopt_role(editor,mypaper);
	 
	 // wait for alice
	 ?play(A,writer,mypaper);
	 
	 //ora4mas.adopt_role(writer,mypaper);
	 .print("roles adopted, writer is ",A);
	 
	 create_scheme(sch1, "wp-os.xml", writePaperSch, false, true);
	 .print("scheme created");
	 add_responsible_group(sch1,mypaper); 
	 .print("scheme is linked to responsible group");
	 
	 commit_mission(mManager, sch1).
	 //ora4mas.commit_mission(mColaborator, sch1);
	 //ora4mas.commit_mission(mBib, sch1).
	 
	 
//-!start[error(I),norm_failure(NF)] <- .print("starting fails due to the normative failure: ",NF).	 
-!start[error(I),error_msg(M)] <- .print("failure in starting! ",I,": ",M).

+?play(A,R,G) <- .wait({+play(A,R,G)}).

// signals
+norm_failure(N) <- .print("norm failure event: ", N).

// plans to react to normative events like obligation created

	  
+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      goal_achieved(Goal,Scheme).
	  
+obligation(Ag,Norm,What,DeadLine)  
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").

// for debug
+goal_state(Sch,Goal,CommittedAgs,AchievedBy,State)
   <- .print("goal changed:  ", goal_state(Sch,Goal,CommittedAgs,AchievedBy,State)).
   
