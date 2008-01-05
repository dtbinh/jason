// Code of dummies agents (Blue team)

// the following plans (+pos....) reacts to the step starting
// (since each new steps causes a new +pos perception)

/* -- Gold found! -- */

// in the positon of the agent
+pos(X,Y,_) 
   : cell(X,Y,gold) &
     carrying_gold(N) & N < 3 // container has space 
  <- do(pick);
     .print("picked gold!");
	 +back_pos(X,Y). // remembers a place to return 

// in a cell besides
+pos(X,Y,_) 
   : cell(GX,GY,gold) &
     carrying_gold(N) & N < 3 // container has space 
  <- jia.direction(X, Y, GX, GY, D);
     do(D).
  
/* -- has gold, carry it/them to depot -- */

// when arrive on depot
+pos(X,Y,_) 
   : carrying_gold(N) & N > 0 &
     depot(_,X,Y)
  <- .print("on depot");
     do(drop);
     do(up).

// when still not in depot
+pos(X,Y,_) 
   : carrying_gold(N) & N > 0 & 
     depot(_,DX,DY)
  <- jia.direction(X, Y, DX, DY, D); // uses A* to find a path to the depot
     //.print("from ",X,"x",Y," to ",DX,"x",DY," -> ",D);
     do(D).

/* -- returns to the back pos -- */

// in the back_pos
+pos(X,Y,_) 
   : back_pos(X,Y)
  <- -back_pos(X,Y);
     do(up).
+pos(X,Y,_) 
   : back_pos(BX,BY)
  <- jia.direction(X, Y, BX, BY, D);
     do(D).
	 
/* -- random move -- */	 
+pos(_,_,_) 
   <- .random(N);
      .nth(N*4,[up,down,left,right],A);
      do(A).
	  