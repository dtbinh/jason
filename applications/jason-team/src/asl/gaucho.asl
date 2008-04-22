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

/* -- create the initial exploration groups -- */

+gsize(Weight,Height)
   : .my_name(gaucho1) 
  <- jmoise.create_group(team).
+group(team,GId)
   : .my_name(gaucho1) 
  <- jmoise.create_group(exploration,GId);
     jmoise.create_group(exploration,GId);
     jmoise.create_group(exploration,GId).

	  
// include common plans for MOISE+ agents
{ include("moise-common.asl") }

