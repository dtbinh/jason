// miner agent

lastDir(null).

+pos(X,Y) : not myQuad(_,_,_,_) <- .send(miner1,tell,myInitPos(X,Y)).

+myQuad(X1,Y1,X2,Y2) : true
  <- .print(myQuad(X1,Y1,X2,Y2));
     +nextPos(X1,Y1);
     +free.
//     !around(X1,Y1).

//+around(X1,Y1): myQuad(X1,Y1,X2,Y2)
//  <- .print("Honey, I'm home!");
//     +free.

// GOLD-SEARCHING PLANS

//+free : true <- !!wander.
+free : true <- ?nextPos(X,Y); !around(X,Y).

//// BCG / RCG
////+!wander : true <- !next; ?wander.
//+!wander : true <- !next; !!wander.
////+!wander : true <- !wander.
//-!wander : true <- !!wander.
//// -free : true <- .dropGoal(wander, true).
//// -free : true <- .dropIntention(wander).

+around(X1,Y1) : myQuad(X1,Y1,X2,Y2) & free <- .print("Q1"); -around(X1,Y1); !around(X2,Y1).
+around(X2,Y) : myQuad(X1,Y1,X2,Y2) & free  <- .print("Q2"); -around(X2,Y); !around(X1,Y+3).
@pn3[atomic]// needed?
+around(X1,Y) : myQuad(X1,Y1,X2,Y2) & free & Y+3 > Y2 <- .print("Q3,Y>Y2"); -around(X1,Y); !around(X2,Y2).
@pn4[atomic]// needed?
+around(X1,Y) : myQuad(X1,Y1,X2,Y2) & free  <- .print("Q3"); -around(X1,Y); !around(X2,Y+3).
+around(X2,Y2) : myQuad(X1,Y1,X2,Y2) & free <- .print("Q4"); -around(X2,Y2); !around(X1,Y1).

//+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y1) <- .print("Q1"); -around(X1,Y1); !around(X2,Y1).
//+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y)  <- .print("Q2"); -around(X2,Y); !around(X1,Y+3).
//@pn3[atomic]// needed?
//+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y) & Y+3 > Y2 <- .print("Q3,Y>Y2"); -around(X1,Y); !around(X2,Y2).
//@pn4[atomic]// needed?
//+!next : myQuad(X1,Y1,X2,Y2) & around(X1,Y)  <- .print("Q3"); -around(X1,Y); !around(X2,Y+3).
//+!next : myQuad(X1,Y1,X2,Y2) & around(X2,Y2) <- .print("Q4"); -around(X2,Y2); !around(X1,Y1).
//+!next : true <- true.
////+!next : .desire(around(X,Y)) <- .print("DESIRE: ",X," ",Y); !next_step(X,Y).

// THIS DOESN'T WORK! Maybe because of !! or .desire and .intend don't work.
//+pos(X,Y) : .desire(around(XG,YG)) & lastDir(skip)
//  <- +around(XG,YG); .print("DESIRE: ",X," ",Y).
//+pos(X,Y) : .desire(around(XG,YG)) & jia.neighbour(X,Y,XG,YG)
//  <- +around(XG,YG); .print("DESIRE: ",X," ",Y).

// these are very old, not used for a long time...
// +!go_to(X,Y) : around(X,Y) <- .print("Here!",X,Y).
// +!go_to(X,Y) : true <- !next_step(X,Y); !!go_to(X,Y).

// BCG!
+!around(X,Y) : pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y) <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     .print("OK DOK, gonna be around soon.");
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
  :  pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     -lastDir(_); +lastDir(D);
     do(D).
// only for the eventuality of no "pos" info (needed???)
+!next_step(X,Y) : true <- true.


// GOLD-HANDLING PLANS

+cell(X,Y,gold) : true <- +gold(X,Y).

+gold(X1,Y1)[source(A)] : A \== self & free
  <- ?pos(X2,Y2);
     jia.dist(X1,Y1,X2,Y2,D);
     .send(miner1,tell,freeFor(gold(X1,Y1),D)).

+gold(X,Y)[source(self)] : not free
  <- .broadcast(tell,gold(X,Y)).

@pg3[atomic]
+gold(X,Y) : free
  <- // -free; // OK to use @ph1? Any chance of another gold event interfering?
//     .dropAllDesires;
//     .dropAllIntentions;
     .print("Oh well, at least I'm here!!!");
     !handle(gold(X,Y)).

+allocatedTo(Gold,Ag) : .myName(Ag)
  <- .print("I've been allocated to handle ",Gold);
     !handle(Gold).
+allocatedTo(Gold,Ag) : not .myName(Ag)
  <- -Gold.

// Hopefully going first to home if never got there because some gold was found
-gold(X,Y) : not gold(_,_) <- +free.
// Finished one gold, but others left
-gold(_,_) : gold(X,Y) <- !handle(gold(X,Y)).

@ph1[atomic]
+!handle(Gold) : free
  <- -free;
     ?pos(X,Y);
     +nextPos(X,Y);
     .dropAllDesires; // must be dropDesire(around) but causes conc. modif. exception...
     .dropAllIntentions; // ... this way can cause problems at least with leader (in the beginning)
     .print("Handling ",Gold," now.");
     !handle(Gold).
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

