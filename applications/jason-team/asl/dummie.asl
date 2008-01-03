// Code of dummies agents

{ include("moving.asl") } // plans for movements in the scenario

free.

@l1[atomic]
+pos(X,Y,_) 
   : free &
     cell(X,Y,gold) &
     container_has_space  
  <- .print("gold found");
     -free;
     do(pick); 
	 do(pick); // pick twice to ensure pick
	 ?carrying_gold(N);
	 N > 0;
     .print("picked gold!"); 
	 ?depot(_,DX,DY);
     .print("going to depot!"); 
	 !pos(DX,DY).

@l2[atomic]
+pos(X,Y,_) 
   : depot(_,X,Y)
  <- .print("on depot");
     do(drop); do(drop);
	 .print("dropped");
	 do(up);
	 +free.

// random move (if free)	 
+pos(_,_,_) 
    : free 
   <- .random(N);
      .nth(N*4,[up,down,left,right],A);
	  do(A).

