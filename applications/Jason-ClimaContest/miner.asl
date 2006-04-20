// miner agent

// Rafa, apaguei os comentarios que eram para eu ler, qdo vc ler os "Rafa, ..."
// pode apagar tb. Eu tb. prefiro o fonte mais limpo :-)


lastDir(null).
free. // Rafa, inicia free, para o caso de ja ver ouro bem no inicio

//init.
// does not work, bug in .wait?
//+init : true <- .wait("+pos(X,Y)"); .send(leader,tell,myInitPos(X,Y)).

+pos(X,Y) : gsize(S,_,_) & not sentMyInitPos(S)  
  <- +sentMyInitPos(S); .send(leader,tell,myInitPos(S,X,Y)).

// security plan: if something else stop working, start again!
/* does not work! (problem with .desire?)
+pos(X,Y) : not .desire(handle(_)) & not .desire(around(_,_))
  <- .print("**** Reseting!"); !update(free).
+pos(X,Y) : true <- true.
*/

+myQuad(X1,Y1,X2,Y2)
  :  free
  <- .print(myQuad(X1,Y1,X2,Y2));
     !update(free).
+myQuad(X1,Y1,X2,Y2) 
  :  true
  <- .print(myQuad(X1,Y1,X2,Y2), " but I am not free").


/* plans for wandering in my quadrant when I'm free */

+free : nextPos(X,Y) <- !around(X,Y).
+free : myQuad(X1,Y1,X2,Y2) <- !around(X1,Y1).

+around(X1,Y1) : myQuad(X1,Y1,X2,Y2) & free
  <- .print("in Q1 to ",X2,"x",Y1); 
     -around(X1,Y1); !around(X2,Y1).

+around(X2,Y2) : myQuad(X1,Y1,X2,Y2) & free 
  <- .print("in Q4 to ",X1,"x",Y1); 
     -around(X2,Y2); !around(X1,Y1).

+around(X2,Y) : myQuad(X1,Y1,X2,Y2) & free  
  <- !calcNewY(Y,Y2,YF);
     .print("in Q2 to ",X1,"x",YF);
     -around(X2,Y); !around(X1,YF).

+around(X1,Y) : myQuad(X1,Y1,X2,Y2) & free  
  <- !calcNewY(Y,Y2,YF);
     .print("in Q3 to ", X2, "x", YF); 
     -around(X1,Y); !around(X2,YF).

// the last around was not any Q above
+around(X,Y) : myQuad(X1,Y1,X2,Y2) & free & Y <= Y2 & Y >= Y1  
  <- .print("in no Q, going to X1");
     -around(X,Y); !around(X1,Y).
+around(X,Y) : myQuad(X1,Y1,X2,Y2) & free & X <= X2 & X >= X1  
  <- .print("in no Q, going to Y1");
     -around(X,Y); !around(X,Y1).

+around(X,Y) : myQuad(X1,Y1,X2,Y2)
  <- .print("It should never happen!!!!!! - go home");
     -around(X,Y); !around(X1,Y1).

+!calcNewY(Y,Y2,Y2) : Y+3 > Y2 <- true.
+!calcNewY(Y,Y2,YF) : true <- YF = Y+3.


// BCG!
+!around(X,Y) : pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y) <- +around(X,Y).
+!around(X,Y) : lastDir(skip) <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     !!around(X,Y). 

+!next_step(X,Y)
  :  pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     .print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
     -lastDir(_); +lastDir(D);
     do(D).
-!next_step(X,Y) : true 
     <-.print("Failed next_step to ", X,"x",Y," fixing and trying again!");
     !fixLastDir;
     !next_step(X,Y).
+!fixLastDir : lastDir(_) <- -lastDir(_); !fixLastDir.
+!fixLastDir : true <- +lastDir(null).


/* Gold-searching Plans */

// I perceived unknown gold and I am free, handle it
@pcell[atomic] // atomic: to not handle another event until handle gold is carryin on
+cell(X,Y,gold) 
  :  not gold(X,Y) & free 
  <- -free;
     !update(nextPos(X,Y));
     +gold(X,Y);
     !!handle(gold(X,Y)). // must use !! to process handle as not atomic
     
// TODO: if i see gold and are free but not carrying gold (i'm probably going to it), 
// abort handle(gold) and catch this one that is near

// I am not free, just add gold belief and announce to others
+cell(X,Y,gold) 
  :  not gold(X,Y) 
  <- +gold(X,Y);
     .print("announcing gold ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 

     
// someone else send me gold location
+gold(X1,Y1)[source(A)] : A \== self & free & pos(X2,Y2)
  <- jia.dist(X1,Y1,X2,Y2,D);
     .send(leader,tell,freeFor(gold(X1,Y1),D)).
+gold(X1,Y1)[source(A)] : A \== self
  <- .send(leader,tell,freeFor(gold(X1,Y1),1000)).

+!allocated(Gold)[source(leader)] 
  :  free // I am still free
  <- -free;
     !update(nextPos(X,Y));
     !handle(Gold).
+!allocated(_) : true <- true.
  
/* this is now called by +cell, to void handling many golds when many perceived
//@pg3[atomic]
  // Rafa, [jomi said] removed atomic, since the agent should perceive gold, 
  // answer to other, ... while handling gold
+gold(X,Y) : free
  <- // -free; // OK to use @ph1? Any chance of another gold event interfering?
     //     .dropAllDesires;
     //     .dropAllIntentions;
     //.print("Oh well, at least I'm here!!!",gold(X,Y));
     !handle(gold(X,Y)).
*/

// someone else catch a gold I saw, remove from my bels
// TODO: if my intend handle this gold, drop this intention and choose another gold
+pick(gold(X,Y)) : gold(X,Y) <- -gold(X,Y).
+pick(_) : true <- true.

// TODO: use the SGA (sequential goal) here, it should not deal with two golds!
// it is possible with communicated golds
//@ph1[atomic]
/* moved to +cell(gold)
+!handle(Gold) : free
  <- -free; 
     ?pos(X,Y);
     !update(nextPos(X,Y));
     .print("Dropping desires and intentions to handle ",Gold);
     .dropAllDesires;    // must be dropDesire(around) but causes conc. modif. exception...
     .dropAllIntentions; // ... this way can cause problems at least with leader (in the beginning)
     !handle(Gold).
*/

+!handle(gold(X,Y)) 
  :  true
  <- .print("Handling ",gold(X,Y)," now.");
     //.print("Dropping desires and intentions to handle ",Gold);
     .dropAllDesires;    // must be dropDesire(around) but causes conc. modif. exception...
     .dropAllIntentions; // ... this way can cause problems at least with leader (in the beginning)
     !pos(X,Y);
     !ensure(pick);
     // broadcast that I got the gold(X,Y), to avoid someone else to pursue this gold
     .broadcast(tell,pick(gold(X,Y)));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     -gold(X,Y); 
     .print("Finish to handle gold ",gold(X,Y));
     !!choose_gold.

// if ensure(pick/drop) failed, pursue another gold
-!handle(G) : G
  <- .print("failed to catch gold ",G);
     -G;
     !!choose_gold.
-!handle(G) : true
  <- .print("failed to handle gold, it isn't in BB anyway");
     !!choose_gold.

// Hopefully going first to home if never got there because some gold was found
+!choose_gold 
  :  not gold(_,_)
  <- !update(free).

// Finished one gold, but others left
// TODO: only pursue this gold in case no other is doing it (committed to this gold)
+!choose_gold 
  :  gold(X,Y) // todo: do not work with gold(_,_)!!!!!! see email that describe bug to find "maior" in a list
               // todo: write an IA meanwhile....
  <- .findall(gold(X,Y),gold(X,Y),LG); .print("Gold distances: ",LG);
     !calcGoldDistance(LG,LD); 
     .sort(LD,[d(_,G)|_]);
     .print("Next gold is ",G);
     !handle(G).

+!calcGoldDistance([],[]) : true <- true.
+!calcGoldDistance([gold(GX,GY)|R],[d(D,gold(GX,GY))|RD]) 
  :  pos(IX,IY) 
  <- jia.dist(IX,IY,GX,GY,D);
     !calcGoldDistance(R,RD).


// BCG!
// !pos is used when it is algways possible to go 
// so this plans should not be used: +!pos(X,Y) : lastDir(skip) <- .print("It is not possible to go to ",X,"x",Y). // in future +lastDir(skip) <- .dropGoal(pos)
+!pos(X,Y) : pos(X,Y) <- .print("I reach ",X,"x",Y).
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).


// need to check if really carrying gold, otherwise drop goal, etc...
// we should have environment feedback for pick!
+!ensure(pick) : pos(X,Y) & cell(X,Y,gold) //gold(X,Y) // & not carryingGold
  <- do(pick).
// fail if no gold there! handle will "catch" this failure. //+!ensure(pick) : true <- true.

+!ensure(drop, _) : pos(X,Y) & depot(_,X,Y) // & carryingGold
  <- do(drop). // we should have feedback for drop!
+!ensure(drop, N) : N < 3 & depot(_,X,Y)
  <- !pos(X,Y);
     !ensure(drop, N+1).
+!ensure(drop, _) : true // drop anywhere!
  <- do(drop).


// update bels
+!update(nextPos(X,Y)) : nextPos(_,_) <- -nextPos(_,_); +nextPos(X,Y).
+!update(nextPos(X,Y)) : true <- +nextPos(X,Y).

+!update(free) : free <- -free; +free.
+!update(free) : true <- +free.


/* end of a simulation */

@end[atomic]
+endOfSimulation(S,_) : true 
  <- .print("-- END ",S," --");
     .dropAllDesires; 
     .dropAllIntentions;
     !clearMyQuad;
     !clearGold;
     !clearNextPos;
     !fixLastDir;
     -pos(_,_);
     !clearMyInitPos;
     !!update(free).

+!clearMyQuad : myQuad(_,_,_,_) <- -myQuad(_,_,_,_).
+!clearMyQuad : true <- true.

+!clearMyInitPos : sentMyInitPos(_) <- -sentMyInitPos(_).
+!clearMyInitPos : true <- true.

+!clearGold : gold(_,_) <- -gold(_,_); !clearGold.
+!clearGold : true <- true.

+!clearNextPos : nextPos(_,_) <- -nextPos(_,_); !clearNextPos.
+!clearNextPos : true <- true.

