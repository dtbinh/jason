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

+end_of_simulation(_Result)
  <- .abolish(group_area(_,_,_)).

+!restart.
 
/*  <- .print("*** restart ***"). 
     //.drop_all_desires;
     //.abolish(target(_,_)).
     // TODO: what to do?
     //!decide_target.
*/

/* -- plans for the goals of all roles -- */

// get the list G of participants of the group where I play R
+?my_group_players(G,R) 
  <- .my_name(Me);
     play(Me,R,Gid);
     .findall(P, play(P,_,Gid), G).

+!play_role(Role,Group)[source(Ag)]
   : .my_name(Me) & not play(Me,_,_) // I can not play more than one role
  <- .print("ooo Adopting role ",Role,", asked by ",Ag);
     jmoise.adopt_role(Role, Group).
+!play_role(Role,Group)[source(Ag)]
  <- .print("ooo Can NOT adopt role ",Role,", asked by ",Ag).

/*

TODO: use a list given by BUF

+!share_seen_cows[scheme(Sch)]
  <- .print("ooo I should share cows!");
     ?cows_to_inform(C);
	 jmoise.broadcast(Sch, tell, C);
	 // TODO: limpar -+cows_to_inform([])
     .wait("+pos(_,_,_)"); // wait next cycle
     !!share_seen_cows[scheme(Sch)].

+?cows_to_inform([]).

+cell(X,Y,cow(Id))
  <- +cow(Id,X,Y); //Jomi, tu nao vai gostar disso :D
     ?cows_to_inform(C);
	 -+cows_to_inform([cow(Id,X,Y)|C]).

*/

+!share_seen_cows.

// simple implementation of share_cows (see TODO above)
+cow(Id,X,Y)[source(percept)]
   : .my_name(Me) & play(Me,_,Gr)
  <- jmoise.broadcast(Gr, tell, cow(Id,X,Y)).
-cow(Id,X,Y)[source(percept)]
   : .my_name(Me) & play(Me,_,Gr)
  <- jmoise.broadcast(Gr, untell, cow(Id,X,Y)).



/* -- general organisational plans -- */

// when I have an obligation or permission to a mission, commit to it
+obligation(Sch, Mission) 
  <- jmoise.commit_mission(Mission,Sch).
+permission(Sch, Mission)
  <- jmoise.commit_mission(Mission,Sch).

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

