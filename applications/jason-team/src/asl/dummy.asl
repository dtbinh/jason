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
   jia.random(RX,40)   & RX > 5 & X = (RX-20)+AgX & X > 0 &
   jia.random(RY,40,5) & RY > 5 & Y = (RY-20)+AgY &
   not jia.obstacle(X,Y).

// whether some location X,Y has an agent and I am near that location
agent_in_target :-
   pos(AgX,AgY,_) &
   target(TX,TY) &
   (cell(TX,TY,ally(_)) | cell(TX,TY,enemy(_)) | cell(TX,TY,cow(_))) &
   jia.dist(TX,TY,AgX,AgY,D) &
   D <= 2. // this number should be the same used by A* (DIST_FOR_AG_OBSTACLE constant)
   
/* -- initial goal */

!move.


/* -- reaction to cows -- */

+pos(_,_,_)                  // new cycle
   : cell(_,_,cow(_))        // I see cows
  <- !decide_target. 
     

/* -- what todo when arrive at location */

+!decide_target                  
   : jia.herd_position(X,Y) &              // compute new location
     (not target(_,_) | (target(TX,TY) & (TX \== X | TY \== Y))) // no target OR new target?     
  <- .print("COWS! going to ",X,",",Y," previous target ",TX,",",TY);
     -+target(X,Y).

+!decide_target                  // chose a new random pos
   : not cell(_,_,cow(_))
  <- ?random_pos(NX,NY);
     .print("New random target: ",NX,",",NY);
     -+target(NX,NY).

+!decide_target
  <- .print("No need for a new target, consider last herding location.");
     do(skip). // send an action so that the simulator does not wait for me.


/* -- plans to move to a destination represented in the belief target(X,Y) 
   -- (it is a kind of persistent goal)
*/

// if the target is changed, "restart" move
+target(NX,NY)
  <- .drop_desire(move);
     jia.set_target(NX,NY);
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
      (not target(_,_)   |  // I have no target OR
       target(X,Y)       |  // I am at target OR
       jia_obstacle(X,Y) |  // An obstacle was discovered in the target
       agent_in_target   |  // there is an agent in the target
       (target(BX,BY) & jia.direction(X, Y, BX, BY, skip))) // is impossible to go to target
   <- !decide_target.
   
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
     .abolish(target(_,_));
     !move.

/* -- tests -- */

+gsize(Weight,Height) <- .println("gsize  = ",Weight,",",Height).
+steps(MaxSteps)      <- .println("steps  = ",MaxSteps).
+corral(X1,Y1,X2,Y2)  <- .println("corral = ",X1,",",Y1," -- ",X2,",",Y2).

//+cell(X,Y,Type)       <- .println("cell   = ",X,",",Y," = ",Type).
+pratio(R)            <- .println("pratio = ",R).
+pos(X,Y,S)           <- .println("pos    = ",X,",",Y,"/",S).
