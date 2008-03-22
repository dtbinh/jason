// Code of dummy agents 

/* 
   Perceptions
      Begin:
        gsize(Weight,Height)
        steps(MaxSteps)
        corral(UpperLeft.x,UpperLeft.y,DownRight.x,DownRight.y)
        pratio(Int) // ratio of perception of the agent
        
      Step:
        pos(X,Y,Step)
        cell(X,Y,Type) 
           Type in { cow(Id), ally(Name), enemy(Id) }
           // the Id of enemy is not working

      End:
        end_of_simulation(Result)

*/

/* -- useful rules */ 

// find a free random location	  
random_pos(X,Y) :- 
   pos(AgX,AgY,_) &
   jia.random(RX,40)   & X = (RX-20)+AgX & X > 0 &
   jia.random(RY,40,5) & Y = (RY-20)+AgY &
   not jia.obstacle(X,Y).  
   
/* -- initial goal */

!move.


/* -- plans to move to a destination represented in the belief target(X,Y) 
   -- (it is a kind of persistent goal)
*/

// if the target is changed, "restart" move
+target(_,_)
  <- .drop_desire(move);
     !!move.

// I still do not know my location
+!move
    : not pos(_,_,_)
  <- .print("waiting my location....");
     .wait("+pos(_,_,_)");
     !move.

// find a new destination
+!move 
    : pos(X,Y,_) &
      (not target(_,_) |  // I have no target OR
       target(X,Y)     |  // I am at target OR
       (target(BX,BY) & jia.direction(X, Y, BX, BY, skip))) // is impossible to go to target
   <- ?random_pos(NX,NY);
      jia.set_target(NX,NY);
      -+target(NX,NY).
   
// does one step towards target  
+!move 
    : pos(X,Y,_) & 
      target(BX,BY) & 
      jia.direction(X, Y, BX, BY, D) // jia.direction finds one action D (using A*) towards the target
   <- do(D);  // this action will "block" the intention until it is sent to the simulator (in the end of the cycle)
      !!move. // continue moving

// in case of failure, move
-!move
   <- .current_intention(I); .println("failure in move, intention: ",I);
      !move.


+restart 
  <- .print("*** restart ***"); 
     .drop_all_desires; 
     !move.

/* -- tests -- */

+gsize(Weight,Height) <- .println("gsize  = ",Weight,",",Height).
+steps(MaxSteps)      <- .println("steps  = ",MaxSteps).
+corral(X1,Y1,X2,Y2)  <- .println("corral = ",X1,",",Y1," -- ",X2,",",Y2).

//+cell(X,Y,Type)       <- .println("cell   = ",X,",",Y," = ",Type).
+pratio(R)            <- .println("pratio = ",R).
+pos(X,Y,S)           <- .println("pos    = ",X,",",Y,"/",S).
