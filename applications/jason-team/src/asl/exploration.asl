/* -- plans for exploration phase -- */


/* -- initial beliefs -- */

/* -- initial goals -- */

//!create_team_group.

/*
   -- plans for new match 
   -- create the initial exploration groups and areas 
*/


/* plans for the team's groups creation */

/*
+!create_team_group
   : .my_name(gaucho1)
  <- .print("oooo creating new team group ------------------------------------------------- "); 
     !remove_org;
     jmoise.create_group(team).
+!create_team_group.
*/

/*+group(team,GId)                         // agent 1 is responsible for the creation of exploration groups 
   : .my_name(gaucho1)
  <- jmoise.create_group(exploration_grp,GId);
     jmoise.create_group(exploration_grp,GId);
     jmoise.create_group(exploration_grp,GId). */
	 

/* plans for agents with odd id */

+gsize(_,_)                             // new match has started 
   : .my_name(Me) &
     agent_id(Me,AgId) &
     AgId mod 2 == 1                    // I have an odd Id
  <- !create_exploration_gr.

+!create_exploration_gr
  <- .my_name(Me);

     // create the team  
	 .if( Me == gaucho1 & not group(team,_) ) {
        jmoise.create_group(team) 
	 };
	 
     // wait the team creation
	 ?group(team,TeamGroup);
	 
     .if( not group(exploration_grp,_)[owner(Me)]) {
        jmoise.create_group(exploration_grp,TeamGroup);
		.wait("+group(exploration_grp,G)[owner(Me)]")
     };
	 
     .print("ooo Recruiting scouters for my explorer group ",G);
  
     ?pos(MyX,MyY,_); // wait my pos
     
     // wait others pos
     .while( .count(ally_pos(_,_,_), N) & N < 5 ) {
        .print("ooo waiting others pos ");
        .wait("+ally_pos(_,_,_)", 500, _)
     };
     
     // find distance to even agents
     .findall(ag_d(D,AgName),
              ally_pos(AgName,X,Y) & agent_id(AgName,Id) & Id mod 2 == 0 & jia.path_length(MyX, MyY, X, Y, D),
              LOdd);
     .sort(LOdd, LSOdd);

     // test if I received the area of my group
     ?group_area(AreaId,G,A);
     .print("ooo Scouters candidates =", LSOdd," in area ",group_area(AreaId,G,A));
     
     // adopt role explorer in the group
     jmoise.adopt_role(explorer,G);
     !find_scouter(LSOdd, G).
     
+!find_scouter([],_)
  <- .print("ooo I did not find a scouter to work with me!").
+!find_scouter([ag_d(_,AgName)|_],GId)
  <- .print("ooo Ask ",AgName," to play scouter");
     .send(AgName, achieve, play_role(scouter,GId));
     .wait("+play(AgName,scouter,GId)",2000).  
-!find_scouter([_|LSOdd],GId) // in case the wait fails, try next agent
  <- .print("ooo find_scouter failure, try another agent.");
     !find_scouter(LSOdd,GId).  
     
// If if start playing explorer in a group that has no scheme, create the scheme
+play(Ag,explorer,G)
   : .my_name(Ag) &
     not scheme_group(_,G)
  <- jmoise.create_scheme(explore_sch, [G]).
     
// If I stop playing explorer, destroy the explore groups I've created
-play(Ag,explorer,_)
   : .my_name(Ag)
  <- .wait(4000);
     .for( group(exploration_grp,G)[owner(Me)] ) {
	    jmoise.remove_group(G);
		.wait(4000)
	 }.

	 
+group(exploration_grp,_)                // compute the area of the groups
   : .my_name(gaucho1) &
     group(team,TeamId) &
     .findall(GId, group(exploration_grp,GId)[super_gr(TeamId)], LG) &
	 LG = [G1,G2,G3]                     // there are three groups
  <- ?gsize(W,H);
	 X = math.round(((W*H)/3)/H);
	 +group_area(0, G1, area(0,   0,       X,   H-1));
	 +group_area(1, G2, area(X+1, 0,       W-1, H/2));
	 +group_area(2, G3, area(X+1, (H/2)+1, W-1, H-1)). 

+group_area(ID,G,A)[source(self)]
  <- .broadcast(tell, group_area(ID,G,A)).  

	 
/* -- plans for the goals of role explorer -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!goto_near_unvisited[scheme(Sch),mission(Mission)]
  <- .print("ooo I should find the nearest unvisited location and go there!");
     .my_name(Me); 
     ?play(Me,explorer,GroupId);    // get the group where I play explorer
     ?group_area(_,GroupId, Area);  // get the area of this group
     ?pos(MeX, MeY, _);             // get my location
     jia.near_least_visited(MeX, MeY, Area, TargetX, TargetY);
     -+target(TargetX, TargetY). 
	 
	 /* added by the pattern
     .wait("+pos(_,_,_)"); // wait next cycle
     !!goto_near_unvisited[scheme(Sch),mission(Mission)]
	 */
	 
{ end }

/* added by the pattern
-!goto_near_unvisited[scheme(Sch),mission(Mission)]
  <- .current_intention(I);
     .print("ooo Failure to goto_near_unvisited ",I);
     .wait("+pos(_,_,_)"); // wait next cycle
     !!goto_near_unvisited[scheme(Sch),mission(Mission)].
*/  



/* -- plans for the goals of role scouter -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!follow_leader[scheme(Sch),mission(Mission),group(Gr)]
   : play(Leader, explorer, Gr)
  <- .print("ooo I should follow the leader ",Leader);
     ?pos(MyX,MyY,_);
     ?ally_pos(Leader,LX,LY);
     ?ag_perception_ratio(AGPR);
     jia.dist(MyX, MyY, LX, LY, DistanceToLeader);
     
     // If I am far from him, go to him
     .if( DistanceToLeader > (AGPR * 2) -3) {
        .print("ooo Approaching leader.");
     	-+target(LX,LY)
     }{
        .print("ooo being in formation with leader.");
        .send(Leader,askOne,target(_,_),target(TX,TY));
        jia.scouter_pos(LX, LY, TX, TY, SX, SY);
     	-+target(SX,SY)
     }.
	 
{ end }	 

