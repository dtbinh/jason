// mars robot 1

/* Initial goal */

!check(slots). 

/* Plans */

+!check(slots) : not garbage(r1)
   <- next(slot);
      !!check(slots).
+!check(slots). 


+garbage(r1) : not .desire(carry_to(r2))
   <- !carry_to(r2).
   
+!carry_to(R)   
   <- // remember where to go back
      ?pos(r1,X,Y); 
	  -+pos(back,X,Y);
	  
	  // carry garbage to r2
      !take(garb,R);
	  
	  // goes back and continue to check
      !go(back); 
	  !!check(slots).

+!take(S,L) : true
   <- !ensure_pick(S); 
      !go(L);
      drop(S).

+!ensure_pick(S) : garbage(r1)
   <- pick(garb);
      !ensure_pick(S).
+!ensure_pick(_).

+!go(L) : pos(L,X,Y) & pos(r1,X,Y).
+!go(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !go(L).
