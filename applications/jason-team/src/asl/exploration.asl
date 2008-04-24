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
  <- .print("oooo creating team group"); 
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


/* plans for agents with even id */

+gsize(_,_)
   : .my_name(Me) &
     agent_id(Me,AgId) &
     AgId mod 2 == 0                    // I have an even Id
  <- // wait my pos
     .if(not pos(MyX, MyY,_), { MyX = 333; MyY = 444 }); //.wait("+pos(MyX,MyY,_)") 
     .print("oooo ",MyX, MyY); //?pos(MyX, MyY,_);
     
     // wait others pos
     .if(not cell(_,_,ally(_)), { .wait("+cell(_,_,ally(_))") });
     .wait(200);
     
     // find distance to odd agents
     .findall(ag_d(D,AgName),
              cell(X,Y,ally(AgName)) & .print(AgName) & agent_id(AgName,Id) & .print(AgName," ooo ",Id) & Id mod 2 == 1 & jia.dist(MyX, MyY, X, Y, D),
              LOdd);
     .sort(LOdd, LSOdd);
     // test if I received the area of my group
     .if( not group_area(AgId div 2,G,A), { .wait("+group_area(AgId div 2,G,A)") });
     .print("oooo Ags=", LSOdd," in area ",group_area(AgId div 2,G,A));
     
     // adopt role explorer in the group
     jmoise.adopt_role(explorer,G);
     !find_scouter(LSOdd, G).
     
+!find_scouter([],_)
  <- .print("oooo I do not find a scouter to work with me!").
+!find_scouter([ag_d(_,AgName)|_],GId)
  <- .send(AgName, achieve, play_role(scouter,GId));
     .wait("+play(Ag,scouter,GId)",1000).  
-!find_scouter([_|LSOdd],GId) // in case the wait fails, try next agent
  <- !find_scouter(LSOdd,GId).  
     

/* plans for agents the others */

+!play_role(Role,Group)
  <- jmoise.adopt_role(Role,Group).
