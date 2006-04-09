refs([boissier04,sichman03]).

// Organisational Events
// ------------------------

// when a wpgroup is created, adopts the role writer 
+group(wpgroup,Id) : true
   <- jmoise.adoptRole(writer,Id).

// when I have an obligation or permission to a mission,
// commit to it
+obligation( Sch, Mission) : true 
   <- jmoise.commitToMission(Mission,Sch).
+permission( Sch, Mission) : true 
   <- jmoise.commitToMission(Mission,Sch).

// when the root goal of the scheme is satisfied, 
// remove my missions
+goalState(Sch, wpGoal, satisfied) : true
   <- jmoise.removeMission(Sch).


// Organisational Goals' plans
// ------------------------------

// a generic plan for organisational goals (they have scheme(_) annotation)
+!X[scheme(Sch)] : true 
   <- .print("doing organisational goal ",X);
      jmoise.setGoalState(Sch,X,satisfied).

// Other events

+useRef(NewRef)[source(S)] 
   :  play(S, writer,Gr) & refs(R)
   <- .print("adding ref ",NewRef, " to ", R);
      -refs(R); +refs([NewRef|R]).
      
