/* 
   Perceptions
      Begin:
        gsize(Weight,Height)
        steps(MaxSteps)
        corral(UpperLeft.x,UpperLeft.y,DownRight.x,DownRight.y)
        
      Each step:
        pos(X,Y,Step)
        cow(Id,X,Y)
        apply_pos(Name,X,Y)

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
+?gsize(W,H)         <- .wait("+gsize(W,H)").
+?ally_pos(Name,X,Y) : .my_name(Name) <- ?pos(X,Y,_).

+end_of_simulation(_Result)
  <- .abolish(group_area(_,_,_));
     !remove_org.

+!restart.
 
/*  <- .print("*** restart ***"). 
     //.drop_all_desires;
     //.abolish(target(_,_)).
     // TODO: what to do?
     //!decide_target.
*/

/* -- plans for the goals of all roles -- */


+!share_seen_cows <- .print("ooo start sharing cows."); .suspend.

// simple implementation of share_cows (see TODO above)
+cow(Id,X,Y)[source(percept)]
   : .desire(share_seen_cows) & .my_name(Me) & play(Me,_,Gr)
  <- jmoise.broadcast(Gr, tell, cow(Id,X,Y)).
-cow(Id,X,Y)[source(percept)]
   : .desire(share_seen_cows) & .my_name(Me) & play(Me,_,Gr)
  <- jmoise.broadcast(Gr, untell, cow(Id,X,Y)).


/* -- general organisational plans -- */

// remove all groups and schemes (only agent1 does that)
+!remove_org
   : .my_name(gaucho1)
  <- .if( group(team,Old) ) {
        jmoise.remove_group(Old)
     };
     
     .for( scheme(_,SchId)) {
        jmoise.remove_scheme(SchId)
     }.
+!remove_org.

// finish the scheme if it has no more players
// and it was created by me
+sch_players(Sch,0) 
   :  .my_name(Me) & scheme(_, Sch)[owner(Me)]
   <- jmoise.remove_scheme(Sch).

  
// get the list G of participants of the group where I play R
+?my_group_players(G,R) 
  <- .my_name(Me);
     ?play(Me,R,Gid);
     .findall(P, play(P,_,Gid), G).

+!play_role(Role,Group)[source(Ag)]
  <- .print("ooo Adopting role ",Role," in group ",Group,", asked by ",Ag);
     jmoise.adopt_role(Role, Group).

// when I have an obligation or permission to a mission, commit to it
+obligation(Sch, Mission) 
  <- jmoise.commit_mission(Mission,Sch).
+permission(Sch, Mission)
  <- jmoise.commit_mission(Mission,Sch).

// when I am not obligated to a mission anymore, uncommit
-obligation(Sch, Mission)
  <- jmoise.remove_mission(Mission,Sch).
-permission(Sch, Mission)
  <- jmoise.remove_mission(Mission,Sch).

// when I am not committed to a mission anymore, remove all goals based on that mission
-commitment(Me,Mission,Sch)
   : .my_name(Me)
  <- .drop_desire(_[scheme(Sch),mission(Mission)]).

// if some scheme is finished, drop all intentions related to it.
-scheme(_Spec,Id)
  <- .drop_desire(_[scheme(Id)]).

+error(M)[source(orgManager)] 
  <- .print("Error in organisational action: ",M); -error(M)[source(orgManager)].


/* -- includes -- */

{ include("goto.asl") }         // include plans for moving around
{ include("exploration.asl") }  // include plans for exploration
{ include("herding.asl") }      // include plans for herding
// { include("moise-common.asl") } // include common plans for MOISE+ agents

