/* 
   Beliefs
*/

// I want to play "editor" in "wpgroups"
// (this belief is used by the moise common plans included below) 
desiredRole(wpgroup,editor).

// I want to commit to "mManager" mission in "writePaperSch" schemes
desiredMission(writePaperSch,mManager).


/*
   Initial goals
*/

!createGroup. // initial goal

// create a group to write a paper
+!createGroup : true 
   <- .send(orgManager, ask, createGroup(wpgroup), GId);
      .print("Created group: ",GId).


/* 
   Organisational Events
   ---------------------
*/

/* Structural events */

// when I start playing the role "editor",
// create a writePaper scheme
+play(Me,editor,GId) 
   :  .myName(Me) 
   <- jmoise.startScheme(writePaperSch).

      
/* Functional events */

// when a writePaper scheme is created,
// add a responsible group for it
+scheme(writePaperSch,SId) 
   : group(wpgroup,GId)
   <- jmoise.addResponsibleGroup(SId, GId).

// when a scheme has finished, start another
-scheme(writePaperSch,SId) : true
   <- .send(orgManager, ask, startScheme(writePaperSch), SchId);
      .print("The new scheme id is ",SchId).

// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

+!wtitle[scheme(Sch)] : true 
   <- .print("Writing title!");
      jmoise.setGoalState(Sch,wtitle,satisfied).

+!wabs[scheme(Sch)] : true 
   <- .print("Writing abstract!");
      jmoise.setGoalState(Sch,wabs,satisfied).

+!wsectitles[scheme(Sch)] : true 
   <- .print("Writing section titles!");
      jmoise.setGoalState(Sch,wsectitles,satisfied).

+!fdv[scheme(Sch)] : true 
   <- .print("Writing the first draft version!");
      jmoise.setGoalState(Sch,fdv,satisfied).

+!wconc[scheme(Sch)] : true 
   <- .print("Writing conclusion!");
      jmoise.setGoalState(Sch,wconc,satisfied).

+!wpGoal[scheme(Sch)] : true 
   <- .print("***** FINISH! *****");
      jmoise.setGoalState(Sch,wpGoal,satisfied).
