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
           Type in { cow(Id), ally(Name), enemy, empty }

      End:
        end_of_simulation(Result)

*/

// the following plans (+pos....) react to the starting step
// (since each new step causes a new +pos perception)

/* -- useful rules */ 

// find a free random location	  
random_pos(X,Y) :- 
   pos(AgX,AgY,_) &
   jia.random(RX,40)   & X = (RX-20)+AgX & X > 0 &
   jia.random(RY,40,5) & Y = (RY-20)+AgY &
   not jia.obstacle(X,Y) &
   jia.set_target(X,Y).  
   

/* -- go to the back pos -- */

// at the back_pos
+pos(X,Y,_) 
   : back_pos(X,Y) | // I am at back pos, find another 
     (back_pos(BX,BY) & jia.direction(X, Y, BX, BY, skip)) // impossible to go to back_pos, find another
  <- !define_new_pos.
+pos(X,Y,_) 
   : back_pos(BX,BY) & jia.direction(X, Y, BX, BY, D) // one step towards back_pos
  <- do(D).
	 
/* -- random move -- */	 
+pos(_,_,_) 
   <- !define_new_pos.
	  
+!define_new_pos
   <- ?pos(X,Y,_);
      ?random_pos(NX,NY);
     //.print("New point ",NX,",",NY);
     -+back_pos(NX,NY);
     jia.direction(X, Y, NX, NY, D);
     do(D).


+restart <- .drop_all_desires; !define_new_pos.

/* -- tests -- */

+gsize(Weight,Height) <- .println("gsize  = ",Weight,",",Height).
+steps(MaxSteps)      <- .println("steps  = ",MaxSteps).
+corral(X1,Y1,X2,Y2)  <- .println("corral = ",X1,",",Y1," -- ",X2,",",Y2).

+cell(X,Y,Type)       <- .println("cell   = ",X,",",Y," = ",Type).
+pratio(R)            <- .println("pratio = ",R).
