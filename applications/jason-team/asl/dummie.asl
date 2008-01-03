// Code of dummies agents

{ include("moving.asl") } // plans for movements in the scenario


free. // free means not carrying gold

// the following plans (+pos....) reacts to the next step
// (since each new steps causes a new +pos perception)

/* -- Gold found! -- */

@l2[atomic]  // this plan is atomic: no other intention is executed while this one is not finished
+pos(X,Y,_) 
   : cell(X,Y,gold) &
     container_has_space  
  <- .drop_all_desires; // just in case I was doing other thing....
     !fetch_gold;
	 !carry_to_depot.

/* -- Random move (if free) -- */	 
+pos(_,_,_) 
    : free 
   <- .random(N);
      .nth(N*4,[up,down,left,right],A);
	  do(A).

	  
/* -- plans for handling gold -- */

+!fetch_gold
  <- ?carrying_gold(N1);
     .print("gold found while I am carrying ",N1," golds.");
     -free;
     do(pick); // pick twice to ensure pick
	 do(pick); 
    .print("picked gold!"). 
	 	 
+!carry_to_depot
   : carrying_gold(N2) & N2 > 0
  <- ?depot(_,DX,DY);
     .print("going to depot!"); 
	 !pos(DX,DY);
     .print("on depot");
     do(drop); do(drop);
	 .print("dropped");
	 do(up); // leave depot
	 +free.

// restart is perceived when the agent is blocked in the same position for 5 steps 
+restart
   : carrying_gold(0)
  <- .print("** Restarting (search gold) **");
     .drop_all_desires;
     +free.
+restart
  <- .print("** Restarting (go to depot) **");
     .drop_all_desires;
     !carry_to_depot.
	 
