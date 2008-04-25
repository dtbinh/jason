/* -- plans for exploration phase -- */

/* -- initial goals -- */

//!test.
//+!test <- +gsize(16,8).

/*
   -- plans for new match 
   -- create the initial exploration groups and areas 
*/


/* plans for agent 1 */

+gsize(_Weight,_Height)                  // new match've started
   : .my_name(gaucho1)                   // agent 1 is responsible for the team creation 
  <- //.print("oooo creating team group"); 
     .if( group(team,Old), {
        jmoise.remove_group(Old)
     });
     jmoise.create_group(team).
  
+group(team,GId)                         // agent 1 is responsible for the creation of exploration groups 
   : .my_name(gaucho1)
  <- jmoise.create_group(exploration,GId);
     jmoise.create_group(exploration,GId);
     jmoise.create_group(exploration,GId).
+group(exploration,_)                    // compute the area of the groups
   : .my_name(gaucho1) &
     .findall(GId, group(exploration,GId), LG) &
	 LG = [G1,G2,G3] // there are three groups
  <- ?gsize(W,H);
	 X = math.round(((W*H)/3)/H);
	 +group_area(0, G1, area(0,   0,       X,   H-1));
	 +group_area(1, G2, area(X+1, 0,       W-1, H/2));
	 +group_area(2, G3, area(X+1, (H/2)+1, W-1, H-1)).

+group_area(ID,G,A)[source(self)]
  <- .broadcast(tell, group_area(ID,G,A)).  


/* plans for agents with odd id */

+gsize(_,_)
   : .my_name(Me) &
     agent_id(Me,AgId) &
     AgId mod 2 == 1                    // I have an odd Id
  <- .print("ooo Recruiting scouters for my explorer group....");
  
     // wait my pos
     ?pos(MyX,MyY,_); 
     
     // wait others pos
     .while( .count(cell(_,_,ally(_)), N) & N < 5, {
	    .print("ooo waiting others pos ");
        .wait("+cell(_,_,ally(_))", 500, nofail)
     });
     
     // find distance to even agents
     .findall(ag_d(D,AgName),
              cell(X,Y,ally(AgName)) & agent_id(AgName,Id) & Id mod 2 == 0 & jia.dist(MyX, MyY, X, Y, D),
              LOdd);
     .sort(LOdd, LSOdd);

     // test if I received the area of my group
     ?group_area(AgId div 2,G,A);
     .print("ooo Scouters candidates =", LSOdd," in area ",group_area(AgId div 2,G,A));
     
     // adopt role explorer in the group
     jmoise.adopt_role(explorer,G);
     !find_scouter(LSOdd, G).
     
+!find_scouter([],_)
  <- .print("ooo I do not find a scouter to work with me!").
+!find_scouter([ag_d(_,AgName)|_],GId)
  <- .print("ooo Ask ",AgName," to play scouter");
     .send(AgName, achieve, play_role(scouter,GId));
     .wait("+play(Ag,scouter,GId)",2000).  
-!find_scouter([_|LSOdd],GId) // in case the wait fails, try next agent
  <- .print("ooo find_scouter failure, try another agent.");
     !find_scouter(LSOdd,GId).  
     

/* plans for agents the others */

+!play_role(Role,Group)
  <- jmoise.adopt_role(Role,Group).
