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

/* -- initial goals -- */

!test.
+!test <- +gsize(16,8).

/* -- create the initial exploration groups and areas -- */

+gsize(_Weight,_Height)                  // new match've started
   : .my_name(gaucho1) 
  <- .if( group(team,Old), {
        jmoise.remove_group(Old)
      });
	 .abolish(area(_,_,_,_,_)); 
     jmoise.create_group(team).
+gsize(_Weight,_Height)
  <- .abolish(area(_,_,_,_,_)).
  
+group(team,GId)
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
	 +area(G1, 0,   0,       X,   H-1);
	 +area(G2, X+1, 0,       W-1, H/2);
	 +area(G3, X+1, (H/2)+1, W-1, H-1).
+area(G,A,B,C,D)[source(self)]
  <- .broadcast(tell, area(G,A,B,C,D)).  

// include common plans for MOISE+ agents
{ include("moise-common.asl") }

