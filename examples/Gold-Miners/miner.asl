// miner agent

lastDir(null).
free.

+gsize(S,_,_) : true <- !sendInitPos(S).
+!sendInitPos(S) : pos(X,Y)
  <- .send(leader,tell,myInitPos(S,X,Y)).
+!sendInitPos(S) : not pos(_,_)
  <- .wait("+pos(X,Y)", 500);
     !!sendInitPos(S).

/* plans for wandering in my quadrant when I'm free */

+free : lastChecked(XC,YC)  <- !around(XC,YC).
+free : myQuad(X1,Y1,X2,Y2) <- !around(X1,Y1).
+free : free <- !waitForQuad.
@pwfq[atomic]
+!waitForQuad : free & myQuad(_,_,_,_) <- -+free.
+!waitForQuad : free     <- .wait("+myQuad(X1,Y1,X2,Y2)", 500); !!waitForQuad.
+!waitForQuad : not free <- .print("No longer free while waiting for myQuad.").

+around(X1,Y1) : myQuad(X1,Y1,X2,Y2) & free
  <- .print("in Q1 to ",X2,"x",Y1); 
     -around(X1,Y1); -+lastDir(null); !around(X2,Y1).

+around(X2,Y2) : myQuad(X1,Y1,X2,Y2) & free 
  <- .print("in Q4 to ",X1,"x",Y1); 
     -around(X2,Y2); -+lastDir(null); !around(X1,Y1).

+around(X2,Y) : myQuad(X1,Y1,X2,Y2) & free  
  <- !calcNewY(Y,Y2,YF);
     .print("in Q2 to ",X1,"x",YF);
     -around(X2,Y); -+lastDir(null); !around(X1,YF).

+around(X1,Y) : myQuad(X1,Y1,X2,Y2) & free  
  <- !calcNewY(Y,Y2,YF);
     .print("in Q3 to ", X2, "x", YF); 
     -around(X1,Y); -+lastDir(null); !around(X2,YF).

// the last "around" was not any Q above
+around(X,Y) : myQuad(X1,Y1,X2,Y2) & free & Y <= Y2 & Y >= Y1  
  <- .print("in no Q, going to X1");
     -around(X,Y); -+lastDir(null); !around(X1,Y).
+around(X,Y) : myQuad(X1,Y1,X2,Y2) & free & X <= X2 & X >= X1  
  <- .print("in no Q, going to Y1");
     -around(X,Y); -+lastDir(null); !around(X,Y1).

+around(X,Y) : myQuad(X1,Y1,X2,Y2)
  <- .print("It should never happen!!!!!! - go home");
     -around(X,Y); -+lastDir(null); !around(X1,Y1).

+!calcNewY(Y,Y2,Y2) : Y+2 > Y2.
+!calcNewY(Y,Y2,YF) <- YF = Y+2.


// BCG!
+!around(X,Y) 
  :  (pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y)) | lastDir(skip) 
  <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     !!around(X,Y).
+!around(X,Y) : true <- !!around(X,Y).

+!next_step(X,Y)
  :  pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     //.print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
     -+lastDir(D);
     do(D).
+!next_step(X,Y) : not pos(_,_) // i still do not know my position
  <- !next_step(X,Y).
-!next_step(X,Y) : true 
  <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
     -+lastDir(null);
     !next_step(X,Y).


/* Gold-searching Plans */

// I perceived unknown gold and I am free, handle it
@pcell[atomic] // atomic: so as not to handle another event until handle gold is carrying on
+cell(X,Y,gold) 
  :  not carrying_gold & free
  <- -free;
     +gold(X,Y);
     .print("Gold perceived: ",gold(X,Y));
     !init_handle(gold(X,Y)).
     
// if I see gold and I'm not free but also not carrying gold yet
// (I'm probably going towards one), abort handle(gold) and pick up
// this one which is nearer
@pcell2[atomic]
+cell(X,Y,gold)
  :  not gold(X,Y) & not carrying_gold & not free & 
     .desire(handle(gold(OldX,OldY))) & // I desire to handle another gold
     pos(AgX,AgY) & not gold(AgX, AgY) // I am not just above the gold 
  <- +gold(X,Y);
     .dropDesire(handle(gold(OldX,OldY)));
     .dropIntention(handle(gold(_,_)));
     .print("Giving up current gold ",gold(OldX,OldY)," to handle ",gold(X,Y)," which I am seeing!");
     .print("Announcing ",gold(OldX,OldY)," to others");
     .broadcast(tell,gold(OldX,OldY));
     !init_handle(gold(X,Y)).
     
// I am not free, just add gold belief and announce to others
+cell(X,Y,gold) 
  :  not gold(X,Y) & not committed(gold(X,Y))
  <- +gold(X,Y);
     .print("Announcing ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 
     
// someone else sent me gold location
+gold(X1,Y1)[source(A)]
  :  not gold(X1,Y1) & A \== self & not allocatedTo(gold(X1,Y1),_) & not carrying_gold & free & pos(X2,Y2)
  <- jia.dist(X1,Y1,X2,Y2,D);
     .send(leader,tell,bidFor(gold(X1,Y1),D)).
// bid high as I'm not free
+gold(X1,Y1)[source(A)]
  :  A \== self
  <- .send(leader,tell,bidFor(gold(X1,Y1),1000)).

@palloc1[atomic]
+allocatedTo(Gold,Ag)[source(leader)] 
  :  .myName(Ag) & free // I am still free
  <- -free;
     .print("Gold ",Gold," allocated to ",Ag);
     !init_handle(Gold).

@palloc2[atomic]
+allocatedTo(Gold,Ag)[source(leader)] 
  :  .myName(Ag) & not free // I am  no longer free
  <- .print("I can not handle ",Gold," anymore!");
     .print("(Re)announcing ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 
     
     
// someone else picked up the gold I was going after
// remove from bels and goals
@ppgd[atomic]
+picked(G)[source(A)] 
  :  .desire(handle(G)) | .desire(init_handle(G))
  <- .print(A," has taken ",G," that I am pursuing! Dropping my intention.");
     -gold(X,Y);
     .dropDesire(handle(G)); // Rafa, do we need to drop the desire?
     .dropIntention(handle(G));
     !!choose_gold.

// someone else picked up a gold I know about, remove from my bels
+picked(gold(X,Y)) : true 
  <- -gold(X,Y).


@pih1[atomic]
+!init_handle(Gold) : .desire(around(_,_)) 
  <- .print("Dropping around(_,_) desires and intentions to handle ",Gold);
     .dropDesire(around(_,_));
     .dropIntention(around(_,_));
     .print("Going for ",Gold);
     !updatePos;
     !!handle(Gold). // must use !! to process handle as not atomic
@pih2[atomic]
+!init_handle(Gold) : true 
  <- .print("Going for ",Gold);
     !updatePos;
     !!handle(Gold). // must use !! to process handle as not atomic

+!updatePos : free & pos(X,Y)
  <- -+lastChecked(X,Y).
// do we need another alternative? I couldn't think of another. If free
// but no desire, probably was still going home which works
+!updatePos.

+!handle(gold(X,Y)) 
  :  not free & .myName(Me)
  <- .print("Handling ",gold(X,Y)," now.");
     .broadcast(tell, committedTo(gold(X,Y)));
     !pos(X,Y);
     !ensure(pick,gold(X,Y));
     // broadcast that I got the gold(X,Y), to avoid someone else to pursue this gold
     .broadcast(tell,picked(gold(X,Y)));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     -gold(X,Y); 
     .print("Finish handling ",gold(X,Y));
     !!choose_gold.

// if ensure(pick/drop) failed, pursue another gold
-!handle(G) : G
  <- .print("failed to catch gold ",G);
     -G;
     !!choose_gold.
-!handle(G) : true
  <- .print("failed to handle ",G,", it isn't in the BB anyway");
     !!choose_gold.

// Hopefully going first to home if never got there because some gold was found
+!choose_gold 
  :  not gold(_,_)
  <- -+free.

// Finished one gold, but others left
// find the closest gold among the known options, 
// which isn't committed by someone else
+!choose_gold 
  :  gold(_,_)
  <- .findall(gold(X,Y),gold(X,Y),LG);
     !calcGoldDistance(LG,LD);
     .length(LD,LLD); LLD > 0;
     .print("Uncommitted gold distances: ",LD,LLD);
     .sort(LD,[d(D,NewG)|_]);
     .print("Next gold is ",NewG);
     !!handle(NewG).
-!choose_gold : true <- -+free.

+!calcGoldDistance([],[]).
+!calcGoldDistance([gold(GX,GY)|R],[d(D,gold(GX,GY))|RD]) 
  :  pos(IX,IY) & not committedTo(gold(GX,GY))
  <- jia.dist(IX,IY,GX,GY,D);
     !calcGoldDistance(R,RD).
+!calcGoldDistance([_|R],RD) 
  :  true
  <- !calcGoldDistance(R,RD).


// BCG!
// !pos is used when it is algways possible to go 
// so this plans should not be used: +!pos(X,Y) : lastDir(skip) <-
// .print("It is not possible to go to ",X,"x",Y).
// in the future
//+lastDir(skip) <- .dropGoal(pos) 
+!pos(X,Y) : pos(X,Y) <- .print("I've reached ",X,"x",Y).
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).


+!ensure(pick,G) : pos(X,Y) & cell(X,Y,gold)
  <- do(pick); ?carrying_gold. 
// fail if no gold there or not carrying_gold after pick! 
// handle(G) will "catch" this failure.

+!ensure(drop, _) : pos(X,Y) & depot(_,X,Y) 
  <- do(drop). //TODO: not ?carrying_gold. 


/* end of a simulation */

@end[atomic]
+endOfSimulation(S,_) : true 
  <- .dropAllDesires; 
     .dropAllIntentions;
     -myQuad(_,_,_,_);
     .abolish(gold(_,_));
     .abolish(committedTo(_));
     .abolish(picked(_));
     .abolish(lastChecked(_,_));
     !repostGsize;
     -+free;
     .print("-- END ",S," --").

+!repostGsize : gsize(S,W,H) <- -+gsize(S,W,H)[source(percept)].
+!repostGsize.

