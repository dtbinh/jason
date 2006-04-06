
// miner agent

lastDir(bla).

+gsize(_,_,_) : true
  <- !calcHome;
     ?myQuad(X,Y,X1,Y1);
     .print(myQuad(X,Y,X1,Y1));
     +go(X,Y);
     !go_to(X,Y).

+around(X1,Y1): myQuad(X1,Y1,X2,Y2)
  <- .print("Honey, I'm home!");
     +free.

// TO FIX!
+!calcHome : .myName(miner1) & gsize(_,W,H) <- +myQuad(0,0,W div 2 - 1,H div 2 - 1).
+!calcHome : .myName(miner2) & gsize(_,W,H) <- +myQuad(W div 2,0,W-1,H div 2 - 1).
+!calcHome : .myName(miner3) & gsize(_,W,H) <- +myQuad(0,H div 2,W div 2 - 1,H-1).
+!calcHome : .myName(miner4) & gsize(_,W,H) <- +myQuad(W div 2,H div 2,W-1,H-1).


+free : true <- !!wander.

// BCG / RCG
//+!wander : true <- !next; ?wander.
+!wander : true <- !next; !!wander.
//+!wander : true <- !wander.
-!wander : true <- !!wander.
// -free : true <- .dropGoal(wander, true).
-free : true <- .dropIntention(wander).

+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y1)  <- .print("Q1"); -go(A,B); -around(C,D); +go(X2,Y1).
+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y)   <- .print("Q2"); !down; -go(A,B); -around(C,D); +go(X1,Y+3).
+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y)   <- .print("Q3"); !down; -go(A,B); -around(C,D); +go(X2,Y+3).
+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y2)  <- .print("Q4"); -go(A,B); -around(C,D); +go(X1,Y1).
+!next : go(X,Y) <- !next_step(X,Y).

+!down : true <- do(down); do(down); do(down).

+pos(X,Y) : go(XG,YG) & lastDir(skip)
  <- +around(XG,YG).
+pos(X,Y) : go(XG,YG) & jia.neighbour(X,Y,XG,YG)
  <- +around(XG,YG).
// Comment out plan below???
//+pos(_,_) : go(XG,YG) & around(_,_) 
//  <- -around(A,B).

//+!next : go(X,Y) & cell(X,Y,obstacle) <- -go(_,_); ?pos(A,B); +go(A,B). // as close as it can get from target position

//+!next_step(X,Y) 
//  : pos(X,Y)
//  <- true.

@pns[atomic]
+!next_step(X,Y)
  : pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     -lastDir(KK); +lastDir(D);
     do(D).
+!next_step(X,Y) : true <- true.


+!go_to(X,Y) : around(X,Y) <- .print("Here!",X,Y).
+!go_to(X,Y) : true <- !next_step(X,Y); !!go_to(X,Y).


//+cell(X,Y,gold) : true <- +gold(X,Y).

+gold(X,Y)[source(A)] : A \== self & free
  <- !negotiate.
+gold(X,Y)[source(self)] : not free
  <- .broadcast(tell,gold(X,Y)).
+gold(X,Y) : free <- -free; !handle(gold(X,Y)).

-gold(X,Y) : not gold(_,_) <- +free.
-gold(_,_) : gold(X,Y) <- !handle(gold(X,Y)).

+!handle(gold(X,Y)) : true
  <- .dropIntention(go(_,_));
     !go(X,Y);
     !ensure(pick);
     ?depot(DX,DY);
     !go(DX,DY);
     !ensure(drop);
     -gold(X,Y).
  
+!ensure(pick) : pos(X,Y) & cell(X,Y,gold)
  <- do(pick);
     !ensure(pick).
+!ensure(pick) : true <- true.
+!ensure(drop) : pos(X,Y) & not cell(X,Y,gold)
  <- do(drop);
     !ensure(drop).
+!ensure(drop) : true <- true.
     

// In the future, change !go to !pos (declarative version)
// BCG!
/*
+!pos(X,Y) : pos(X,Y) <- true.
+!pos(X,Y) : not pos(X,Y) <-
     jia.getDirection(AgX, AgY, X, Y, D);
     do(D);
     ?pos(X,Y).
-!pos(X,Y) : true <- !pos(X,Y).
*/


