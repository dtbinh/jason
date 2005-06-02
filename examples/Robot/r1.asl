// mars robot 1

// Beliefs
// --------------

pos(r2,2,2).
checking(slots).

// Plans
// --------------

+pos(r1,X,Y) : checking(slots) & not(garbage(r1))
   <- next(slot).

+garbage(r1) : checking(slots)
   <- !stop(check);
      !take(garb,r2);
      !continue(check).

+!stop(check) : true
   <- ?pos(r1,X,Y); 
      +pos(back,X,Y);
      -checking(slots).

+!take(S,L) : true
   <- !ensure_pick(S); 
      !go(L);
      drop(S).

+!ensure_pick(S) : garbage(r1)
   <- pick(garb);
      !ensure_pick(S).

+!ensure_pick(S) : true <- true.

+!continue(check) : true
   <- !go(back);
      -pos(back,X,Y);
      +checking(slots);
      next(slot).

+!go(L) : pos(L,X,Y) & pos(r1,X,Y)
   <- true.

+!go(L) : true
   <- ?pos(L,X,Y);
      moveTowards(X,Y);
      !go(L).
