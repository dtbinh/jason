/* 
   Perceptions
      Begin:
        gsize(Weight,Height)
        steps(MaxSteps)
        corral(UpperLeft.x,UpperLeft.y,DownRight.x,DownRight.y)
        
      Step:
        pos(X,Y,Step)
        cell(X,Y,Type) 
           Type in { cow(Id), ally(Name), enemy(Id) }
           // the Id of enemy is not working

      End:
        end_of_simulation(Result)

*/

/* -- initial beliefs -- */

agent_id(gaucho1,1).
agent_id(gaucho2,2).
agent_id(gaucho3,3).
agent_id(gaucho4,4).
agent_id(gaucho5,5).
agent_id(gaucho6,6).

ag_perception_ratio(8). // ratio of perception of the agent
cow_perception_ratio(4).

/* -- initial goals -- */

/* Testing alloc
alloc_target(a1,pos(10,10)).
alloc_target(a2,pos(20,20)).
alloc_target(a3,pos(30,30)).
!test.
+!test <- !alloc_all([a1,a2,a3],[pos(31,31),pos(21,21),pos(11,11)]).
*/

/* -- plans -- */

+?pos(X, Y, S)       <- .wait("+pos(X,Y,S)").
+?group_area(Id,G,A) <- .wait("+group_area(Id,G,A)").

+end_of_simulation(_Result)
  <- .abolish(area(_,_,_,_,_,_)).

+!restart 
  <- .print("*** restart ***"); 
     //.drop_all_desires;
     .abolish(target(_,_)).
     // TODO: what to do?
     //!decide_target.

/* -- includes -- */

{ include("goto.asl") }         // include plans for moving around
{ include("exploration.asl") }  // include plans for exploration
{ include("herding.asl") }      // include plans for herding
{ include("moise-common.asl") } // include common plans for MOISE+ agents

