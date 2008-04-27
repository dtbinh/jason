/* -- plans for exploration phase -- */


/* -- initial beliefs -- */

/* -- initial goals -- */

//!test.
//+!test <- +gsize(16,8).

/*
   -- plans for new match 
   -- create the initial exploration groups and areas 
*/


/* plans for the team's groups creation */

+!create_team_group
  <- .print("oooo creating team group"); 
     .if( group(team,Old), {
        jmoise.remove_group(Old)
     });
     jmoise.create_group(team).
  
+group(team,GId)                         // agent 1 is responsible for the creation of exploration groups 
   : .my_name(gaucho1)
  <- jmoise.create_group(exploration_grp,GId);
     jmoise.create_group(exploration_grp,GId);
     jmoise.create_group(exploration_grp,GId).
+group(exploration_grp,_)                    // compute the area of the groups
   : .my_name(gaucho1) &
     .findall(GId, group(exploration_grp,GId), LG) &
	 LG = [G1,G2,G3]                     // there are three groups
  <- ?gsize(W,H);
	 X = math.round(((W*H)/3)/H);
	 +group_area(0, G1, area(0,   0,       X,   H-1));
	 +group_area(1, G2, area(X+1, 0,       W-1, H/2));
	 +group_area(2, G3, area(X+1, (H/2)+1, W-1, H-1)). 


+group_area(ID,G,A)[source(self)]
  <- .broadcast(tell, group_area(ID,G,A)).  


/* plans for agents with odd id */

+gsize(_,_)                             // new match has started 
   : .my_name(Me) &
     agent_id(Me,AgId) &
     AgId mod 2 == 1                    // I have an odd Id
  <- .if( .my_name(gaucho1), { 
        !create_team_group 
     });
  
     .print("ooo Recruiting scouters for my explorer group....");
  
     // wait my pos
     ?pos(MyX,MyY,_); 
     
     // wait others pos
     .while( .count(ally_pos(_,_,_), N) & N < 5, {
	    .print("ooo waiting others pos ");
        .wait("+ally_pos(_,_,_)", 500, nofail)
     });
     
     // find distance to even agents
     .findall(ag_d(D,AgName),
              ally_pos(AgName,X,Y) & agent_id(AgName,Id) & Id mod 2 == 0 & jia.path_length(MyX, MyY, X, Y, D),
              LOdd);
     .sort(LOdd, LSOdd);

     // test if I received the area of my group
     ?group_area(AgId div 2,G,A);
     .print("ooo Scouters candidates =", LSOdd," in area ",group_area(AgId div 2,G,A));
     
     // adopt role explorer in the group
     jmoise.adopt_role(explorer,G);
     !find_scouter(LSOdd, G).
     
+!find_scouter([],_)
  <- .print("ooo I did not find a scouter to work with me!").
+!find_scouter([ag_d(_,AgName)|_],GId)
  <- .print("ooo Ask ",AgName," to play scouter");
     .send(AgName, achieve, play_role(scouter,GId));
     .wait("+play(Ag,scouter,GId)",2000).  
-!find_scouter([_|LSOdd],GId) // in case the wait fails, try next agent
  <- .print("ooo find_scouter failure, try another agent.");
     !find_scouter(LSOdd,GId).  
     
// If if start playing explorer in a group that has no scheme, create the scheme
+play(Ag,explorer,G)
   : .my_name(Ag) &
     not scheme_group(_,G)
  <- jmoise.create_scheme(explore_sch, [G]).
     

/* -- plans for the goals of role explorer -- */

// TODO: make a pattern for organisational maintainance goal

+!goto_near_unvisited[scheme(Sch)]
  <- .print("ooo I should find the nearest unvisited location and go there!");
     .my_name(Me); 
     ?play(Me,explorer,GroupId);    // get the group where I play explorer
     ?group_area(_,GroupId, Area);  // get the area of this group
     ?pos(MeX, MeY, _);             // get my location
     jia.near_least_visited(MeX, MeY, Area, TargetX, TargetY);
     -+target(TargetX, TargetY);
     .wait("+pos(_,_,_)"); // wait next cycle
     !!goto_near_unvisited[scheme(Sch)].

-!goto_near_unvisited[scheme(Sch)]
  <- .current_intention(I);
     .print("ooo Failure to goto_near_unvisited ",I);
     .wait("+pos(_,_,_)"); // wait next cycle
     !!goto_near_unvisited[scheme(Sch)].
  

/* -- plans for the goals of role scouter -- */

+!follow_leader[scheme(Sch),group(Gr)]
   : play(Leader, explorer, Gr)
  <- .print("ooo I should follow the leader ",Leader);
     ?pos(MyX,MyY,_);
     ?ally_pos(Leader,LX,LY);
     ?ag_perception_ratio(AGPR);
     jia.dist(MyX, MyY, LX, LY, DistanceToLeader);
     
     // If I am far from him, go to him
     .if( DistanceToLeader > (AGPR * 2) -3, {
        .print("ooo Approaching leader.");
     	-+target(LX,LY)
     }, {
        .print("ooo being in formation with leader.");
        .send(Leader,askOne,target(X,Y),target(TX,TY));
        jia.scouter_pos(LX, LY, TX, TY, SX, SY);
     	-+target(SX,SY)
     });
     
     .wait("+pos(_,_,_)"); // wait next cycle
     !!follow_leader[scheme(Sch),group(Gr)].

-!follow_leader[scheme(Sch),group(Gr)]
  <- .current_intention(I);
     .print("ooo Failure to follow_leader ",I);
     .wait("+pos(_,_,_)"); // wait next cycle
     !!follow_leader[scheme(Sch),group(Gr)].
     