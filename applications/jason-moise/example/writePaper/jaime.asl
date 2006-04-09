init.

// create a group to write a paper
+init : true 
   <- .send(orgManager, ask, createGroup(wpgroup), GId);
      .print("Created group ",GId).


// Organisational Events
// -----------------------

// when a wpgroup is created, 
// adopts the role editor and creates the scheme writePaper
+group(wpgroup,GId) : true 
   <- jmoise.adoptRole(editor,GId);
      jmoise.startScheme(writePaperSch).

// when a writePaper scheme is created,
// add a responsible group for it
+scheme(writePaperSch,SId) 
   : group(wpgroup,GId)
   <- jmoise.addResponsibleGroup(SId, GId).

// when I have an obligation or permission to a mission, 
// commit to it
+obligation(Sch, Mission) : true 
   <- jmoise.commitToMission(Mission,Sch).
+permission(Sch, Mission) : true 
   <- jmoise.commitToMission(Mission,Sch).

// when the root goal of the scheme is satisfied, 
// remove my missions
+goalState(Sch, wpGoal, satisfied) : true
   <- jmoise.removeMission(Sch).

// finish the scheme if it has no more players
+schPlayers(Sch,0) : true
   <- jmoise.finishScheme(Sch).

// when a scheme has finished, start another
-scheme(writePaperSch,SId) : true
   <- .send(orgManager, ask, 
            startScheme(writePaperSch), SchId);
      .print("The new scheme id is ",SchId).


// Organisational Goals
// ------------------------

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
