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

agent_id(gaucho1,0).
agent_id(gaucho2,1).
agent_id(gaucho3,2).
agent_id(gaucho4,3).
agent_id(gaucho5,4).
agent_id(gaucho6,5).

ag_perception_ratio(8). // ratio of perception of the agent
cow_perception_ratio(4).

/* -- initial goals -- */

/* -- plans -- */

+end_of_simulation(_Result)
  <- .abolish(area(_,_,_,_,_,_)).

+!restart 
  <- //.print("*** restart ***"); 
     .drop_all_desires;
     .abolish(target(_,_)).
     // TODO: what to do?
     //!decide_target.

/* -- includes -- */

{ include("goto.asl") }         // include plans for movimentation
{ include("exploration.asl") }  // include plans for exploration
{ include("herding.asl") }      // include plans for herding
{ include("moise-common.asl") } // include common plans for MOISE+ agents

