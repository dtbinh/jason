// leader agent

lastDir(null).

+pos(X,Y) : not myQuad(_,_,_,_) <- +myInitPos(X,Y)[source(miner1)].

+gsize(_,W,H) : true
  <- +quad(1, 0, 0, W div 2 - 1, H div 2 - 1);
     +quad(2, W div 2, 0, W-1, H div 2 - 1);
     +quad(3, 0, H div 2, W div 2 - 1, H - 1);
     +quad(4, W div 2, H div 2, W - 1, H - 1).

+myInitPos(X,Y)[source(A)]
  :  myInitPos(X1,Y1)[source(B)] & myInitPos(X2,Y2)[source(C)] & myIniPos(X3,Y3)[source(D)] &
     A \== B & B \== C & C \== D & D \== A & C \== A & D \== B
  <- !assignAllQuads([miner1,miner2,miner3,miner4]);
     !backToMinerRole.

+!assignAllQuads([]) : true <- true.
+!assignAllQuads([A|T]) : true
  <- ?gsize(_,W,H);
     !assignQuad(A,d(0,q(0,W+H+1)),Q);
     .print(A, "'s Quadrant is: ",Q);
     Q = q(I,_);
     ?quad(I,X1,Y1,X2,Y2);
     -quad(I,X1,Y1,X2,Y2);
     .send(A,tell,myQuad(X1,Y1,X2,Y2));
     !assignAllQuads(T).

// Already checked all (at most) 4 quadrants available for agent A
+!assignQuad(A,d(5,Q),Q) : true <- true.
// 
+!assignQuad(A,d(I,q(ID,D)),Q) : NI = I+1 & quad(NI,X1,Y1,_,_)
  <- ?myInitPos(X2,Y2)[source(A)];
     jia.dist(X1,Y1,X2,Y2,ND);
     !getSmaller(q(ID,D),q(NI,ND),SQ); // shall we add conditional expressions in AS?
     !assignQuad(A,d(NI,SQ),Q).
// Quadrant 'I' already assigned to another agent
+!assignQuad(A,d(I,D),Q) : true
  <- NI = I+1;
     !assignQuad(A,d(NI,D),Q).

+!getSmaller( q(Q1,D1), q(Q2,D2), q(Q1,D1) ) : D1 <= D2 <- true.
+!getSmaller( q(Q1,D1), q(Q2,D2), q(Q2,D2) ) : D2 <  D1 <- true.

+!backToMinerRole : true
  <- ?myQuad(X,Y,X1,Y1);
     .print(myQuad(X,Y,X1,Y1));
     +dir(X,Y);
     !around(X,Y).

+around(X1,Y1): myQuad(X1,Y1,X2,Y2)
  <- .print("Honey, I'm home!");
     +free.

// GOLD-SEARCHING PLANS

+free : true <- !!wander.

// BCG / RCG
//+!wander : true <- !next; ?wander.
+!wander : true <- !next; !!wander.
//+!wander : true <- !wander.
-!wander : true <- !!wander.
// -free : true <- .dropGoal(wander, true).
// -free : true <- .dropIntention(wander).

+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y1)  <- .print("Q1"); -dir(_,_); -around(X1,Y1); +dir(X2,Y1).
+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y)   <- .print("Q2"); !down; -dir(_,_); -around(X2,Y); +dir(X1,Y+3).
+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y)   <- .print("Q3"); !down; -dir(_,_); -around(X1,Y); +dir(X2,Y+3).
+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y2)  <- .print("Q4"); -dir(A,B); -around(X2,Y2); +dir(X1,Y1).
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
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     !!around(X,Y). // only used to go home, OK to be async.
// TO FIX
//     ?around(X,Y).
//-!around(X,Y) : true <- !around(X,Y).

// BCG!
+!pos(X,Y) : pos(X,Y) <- true.
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).
// TO FIX
//     ?pos(X,Y).
//-!pos(X,Y) : true <- !pos(X,Y).


@pns[atomic]
+!next_step(X,Y)
  : pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     -lastDir(_); +lastDir(D);
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

