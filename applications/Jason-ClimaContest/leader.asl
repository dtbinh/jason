// leader agent

lastDir(null).


+gsize(_,W,H) : true
  <- +quad(1, 0, 0, W div 2 - 1, H div 2 - 1);
     +quad(2, W div 2, 0, W-1, H div 2 - 1);
     +quad(3, 0, H div 2, W div 2 - 1, H - 1);
     +quad(4, W div 2, H div 2, W - 1, H - 1).

+myInitPos(X,Y)[source(A)]
  :  myInitPos(X1,Y1)[source(miner1)] & myInitPos(X2,Y2)[source(miner2)] &
     myInitPos(X3,Y3)[source(miner3)] & myInitPos(X4,Y4)[source(miner4)]
  <- .print("InitPos ",A," is ",X," ",Y);
     !assignAllQuads([miner1,miner2,miner3,miner4],[1,2,3,4]).
//     !backToMinerRole.
+myInitPos(X,Y)[source(A)] : true <- .print("InitPos ",A," is ",X," ",Y).

// Jomi, estas partes que sao "prolog", era melhor se a gente ja tivesse
// aquelas regras na BB, pra nao confundir o uso de planos; acho que fica
// mais didatico so usar planos pra practical reasoning. Ou nao?

+!assignAllQuads([],_) : true <- true.
// Give priority based on agent number, this is NOT the optimal allocation
+!assignAllQuads([A|T],[I|L]) : true
  <- ?quad(I,X1,Y1,_,_);
     ?myInitPos(X2,Y2)[source(A)];
     jia.dist(X1,Y1,X2,Y2,D);
     // NB: Fixed Jason bug with unif of returning goals (see ClrInt in TS, no use of compose anymore)
     !assignQuad(A,q(I,D),q(AQ,_),L,[],NL); // Using Q instead of AQ here makes last call, with L=[] already, not to
                                            // unify directly with the first assignQuad plan below. Is this a bug
					    // with Jason's handling of logical variagbles and unification?
     .print(A, "'s Quadrant is: ",AQ);
     ?quad(AQ,X3,Y3,X4,Y4);
     .send(A,tell,myQuad(X3,Y3,X4,Y4)); // .send works for the agent itself
     !assignAllQuads(T,NL).

// Already checked all quadrants available for agent A
+!assignQuad(A,Q,Q,[],L,L) : true <- true.
//
+!assignQuad(A,q(ID,D),Q,[I|T],L,FL) : true
  <- ?quad(I,X1,Y1,_,_);
     ?myInitPos(X2,Y2)[source(A)];
     jia.dist(X1,Y1,X2,Y2,ND);
     !getSmaller(q(ID,D),q(I,ND),SQ,LI); // shall we add conditional expressions to AS?
     !assignQuad(A,SQ,Q,T,[LI|L],FL).

+!getSmaller( q(Q1,D1), q(Q2,D2), q(Q1,D1), Q2 ) : D1 <= D2 <- true.
+!getSmaller( q(Q1,D1), q(Q2,D2), q(Q2,D2), Q1 ) : D2 <  D1 <- true.

// Jomi, da pra gerar esse evento na arch?
+endOfSimStep : true <- !checkFreeMiners.

+!checkFreeMiners : not freeFor(_) <- true.
+!checkFreeMiners : freeFor(Gold) <- !allocateMinerFor(Gold); !checkFreeMiners.

+!allocateMinerFor(Gold) : true
  <- .findall(freeFor(Gold,Dist)[source(A)],A,[HA|TAs]);
     !calcClosest(TAs,Gold,c(HA,Dist),CA);
     .send(CA,achieve,handle(Gold)).

+!calcClosest([],_,c(A,_),A) : true <- true.
+!calcClosest([A|T],Gold,c(CA,CD),FA) : freeFor(Gold,D)[source(A)]
  <- !getClosest(c(CA,CD),c(A,D),C);
     !calcClosest(T,Gold,C,FA).

+!getClosest( c(A1,D1), c(A2,D2), c(A1,D1) ) : A1 >= A2 <- true.
+!getClosest( c(A1,D1), c(A2,D2), c(A2,D2) ) : A2 >  A1 <- true.

// +!backToMinerRole : true
  // <- ?myQuad(X,Y,X1,Y1);
     // .print("Leader's quad: ",myQuad(X,Y,X1,Y1));
     // +dir(X,Y);
     // !around(X,Y).

// +pos(X,Y) : not myQuad(_,_,_,_) <- +myInitPos(X,Y)[source(miner1)].
// more efficient but then we can "include miner.asl" here, in the future.
// FROM HERE, SAME AS MINER.ASL     

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

