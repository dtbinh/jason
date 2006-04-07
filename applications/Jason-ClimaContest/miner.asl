// miner agent

lastDir(null).

+gsize(_,_,_) : true
  <- !calcHome;
     ?myQuad(X,Y,X1,Y1);
     .print(myQuad(X,Y,X1,Y1));
     +dir(X,Y);
     !around(X,Y).

+around(X1,Y1): myQuad(X1,Y1,X2,Y2)
  <- .print("Honey, I'm home!");
     +free.

// TO FIX!
+!calcHome : .myName(miner1) & gsize(_,W,H) <- +myQuad(0,0,W div 2 - 1,H div 2 - 1).
+!calcHome : .myName(miner2) & gsize(_,W,H) <- +myQuad(W div 2,0,W-1,H div 2 - 1).
+!calcHome : .myName(miner3) & gsize(_,W,H) <- +myQuad(0,H div 2,W div 2 - 1,H-1).
+!calcHome : .myName(miner4) & gsize(_,W,H) <- +myQuad(W div 2,H div 2,W-1,H-1).


// GOLD-SEARCHING PLANS

+free : true <- !!wander.

// BCG / RCG
//+!wander : true <- !next; ?wander.
+!wander : true <- !next; !!wander.
//+!wander : true <- !wander.
-!wander : true <- !!wander.
// -free : true <- .dropGoal(wander, true).
// -free : true <- .dropIntention(wander).

+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y1)  <- .print("Q1"); -dir(A,B); -around(C,D); +dir(X2,Y1).
+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y)   <- .print("Q2"); !down; -dir(A,B); -around(C,D); +dir(X1,Y+3).
+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y)   <- .print("Q3"); !down; -dir(A,B); -around(C,D); +dir(X2,Y+3).
+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y2)  <- .print("Q4"); -dir(A,B); -around(C,D); +dir(X1,Y1).
+!next : dir(X,Y) <- !next_step(X,Y).

+!down : true <- do(down); do(down); do(down).

+pos(X,Y) : dir(XG,YG) & lastDir(skip)
  <- +around(XG,YG).
+pos(X,Y) : dir(XG,YG) & jia.neighbour(X,Y,XG,YG)
  <- +around(XG,YG).

// +!go_to(X,Y) : around(X,Y) <- .print("Here!",X,Y).
// +!go_to(X,Y) : true <- !next_step(X,Y); !!go_to(X,Y).

// BCG!
+!around(X,Y) : around(X,Y) <- true.
+!around(X,Y) : not around(X,Y) <-
     !next_step(X,Y);
     !!around(X,Y). // only used to go home, OK to be async.
// TO FIX
//     ?around(X,Y).
//-!around(X,Y) : true <- !around(X,Y).

// BCG!
+!pos(X,Y) : pos(X,Y) <- true.
+!pos(X,Y) : not pos(X,Y) <-
     !next_step(X,Y);
     !pos(X,Y).
// TO FIX
//     ?pos(X,Y).
//-!pos(X,Y) : true <- !pos(X,Y).


@pns[atomic]
+!next_step(X,Y)
  : pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     -lastDir(LD); +lastDir(D);
     do(D).
// only for the eventuality of no "pos" info (needed???)
+!next_step(X,Y) : true <- true.


// GOLD-HANDLING PLANS

+cell(X,Y,gold) : true <- +gold(X,Y).

//+gold(X,Y)[source(A)] : A \== self & free
//  <- !negotiate.
+gold(X,Y)[source(self)] : not free
  <- .broadcast(tell,gold(X,Y)).

@pg2[atomic]
+gold(X,Y) : free
  <- -free;
     .dropAllDesires;
     .dropAllIntentions;
     .print("Oh well, at least I'm here!!!");
     !handle(gold(X,Y)).

// Hopefully going first to home if never got there because some gold was found
-gold(X,Y) : not gold(_,_) <- +free.
// Finished one gold, but others left
-gold(_,_) : gold(X,Y) <- !handle(gold(X,Y)).

+!handle(gold(X,Y)) : true
  <- !pos(X,Y);
     !ensure(pick);
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop);
     -gold(X,Y).

// need to check if really carrying gold, otherwise drop goal, etc...
+!ensure(pick) : pos(X,Y) & gold(X,Y) // & not carryingGold
  <- do(pick).
//     !ensure(pick).
+!ensure(pick) : true <- true.
+!ensure(drop) : depot(_,X,Y) & pos(X,Y) // & carryingGold
  <- do(drop).
//     !ensure(drop).
+!ensure(drop) : true <- true.

