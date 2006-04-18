// miner agent

// Rafa, apaguei os comentarios que eram para eu ler, qdo vc ler os "Rafa, ..."
// pode apagar tb. Eu tb. prefiro o fonte mais limpo :-)


lastDir(null).
free. // Rafa, inicia free, para o caso de ja ver ouro bem no inicio
//init.

// does not work, bug in .wait?
//+init : true <- .wait("+pos(X,Y)"); .send(miner1,tell,myInitPos(X,Y)).

+pos(X,Y) : not sentMyInitPos <- +sentMyInitPos; .send(miner1,tell,myInitPos(X,Y)).

// security plan: if something else stop working, start again!
/* does not work!
+pos(X,Y) : not .desire(handle(_)) & not .desire(around(_,_))
  <- .print("**** Reseting!"); !update(free).
+pos(X,Y) : true <- true.
*/

+myQuad(X1,Y1,X2,Y2)
  :  free
  <- .print(myQuad(X1,Y1,X2,Y2));
     -free; +free.
+myQuad(X1,Y1,X2,Y2) 
  :  true
  <- .print(myQuad(X1,Y1,X2,Y2)).


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
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     !!around(X,Y). 

+!next_step(X,Y)
  :  pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     -lastDir(_); +lastDir(D);
     do(D).
+!next_step(X,Y) : true <- true.



/* Gold-searching Plans */

// I perceived unkwon gold
+cell(X,Y,gold) : not gold(X,Y) <- +gold(X,Y).

// someone else send me gold location
+gold(X1,Y1)[source(A)] : A \== self & free
  <- ?pos(X2,Y2);
     jia.dist(X1,Y1,X2,Y2,D);
     .send(miner1,tell,freeFor(gold(X1,Y1),D)).
+gold(X1,Y1)[source(A)] : A \== self & not free
  <- .send(miner1,tell,freeFor(gold(X1,Y1),1000)).

// i've perceived gold, but i'm alredy carrying gold, so announce to others
+gold(X,Y)[source(self)] 
  :  not free //.intend(handle(gold(_,_))) // is processing  another gold
  <- .print("announcing gold ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 


//@pg3[atomic]
  // Rafa, [jomi said] removed atomic, since the agent should perceive gold, 
  // answer to other, ... while handling gold
+gold(X,Y) : free
  <- // -free; // OK to use @ph1? Any chance of another gold event interfering?
    //     .dropAllDesires;
    //     .dropAllIntentions;
     //.print("Oh well, at least I'm here!!!",gold(X,Y));
     !handle(gold(X,Y)).

// Rafa, parece que os dois planos abaixo nao sao usados, o leader manda
// direto !handle(Gold), ok?
/*
+allocatedTo(Gold,Ag) : .myName(Ag) & free // i am still free
  <- .print("I've been allocated to handle ",Gold);
     !handle(Gold).
+allocatedTo(Gold,Ag) : true //not .myName(Ag)
  <- -Gold.
*/

// someone else catch a gold I saw, remove from my bels
// TODO: if my intend handle this gold, drop this intention and chose another gold
+pick(gold(X,Y)) : gold(X,Y) <- .print(aaaaa);-gold(X,Y).
+pick(_) : true <- .print(bbbbb);.

// TODO: use the SGA (sequential goal) here, the should not deal with to golds!
// it is possible with communicated golds
//@ph1[atomic]
+!handle(Gold) : free
  <- -free; 
     ?pos(X,Y);
     !update(nextPos(X,Y));
     .print("Dropping desires and intentions to handle ",Gold);
     .dropAllDesires;    // must be dropDesire(around) but causes conc. modif. exception...
     .dropAllIntentions; // ... this way can cause problems at least with leader (in the beginning)
     !handle(Gold).
     
+!handle(gold(X,Y)) 
  :  gold(X,Y) // the gold is still there
  <- .print("Handling ",gold(X,Y)," now.");
     !pos(X,Y);
     !ensure(pick);
     // broadcast that a got gold(X,Y) to avoid someone else to pursue this gold
     .broadcast(tell,pick(gold(X,Y)));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop);
     -gold(X,Y); 
     .print("Finish to handle gold ",gold(X,Y));
     !choose_gold.

// TODO: if ensure(pick) failed, pursue another gold
-!handle(G) : true
  <- .print("failed to catch gold ",G);
     -G;
     !choose_gold.


// Hopefully going first to home if never got there because some gold was found
+!choose_gold 
  :  not gold(_,_)
  <- +free.

// Finished one gold, but others left
// TODO: only pursue this gold in case no other is doing it (committed to this gold)
+!choose_gold 
  :  gold(X,Y)
  <- .findall(gold(X,Y),gold(X,Y),LG);
     !calcGoldDistance(LG,LD); .print("Gold distances: ",LD);
     .sort(LD,[d(_,G)|_]);
     .print("Next gold is ",G);
     !!handle(G).

+!calcGoldDistance([],[]) : true <- true.
+!calcGoldDistance([gold(GX,GY)|R],[d(D,gold(GX,GY))|RD]) 
  :  pos(IX,IY) 
  <- jia.dist(IX,IY,GX,GY,D);
     !calcGoldDistance(R,RD).


// BCG!
+!pos(X,Y) : lastDir(skip) <- .print("It is not possible to go to ",X,Y). // in future +lastDir(skip) <- .dropGoal(pos)
+!pos(X,Y) : pos(X,Y) <- .print("I am at ",X,"x",Y).
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).


// need to check if really carrying gold, otherwise drop goal, etc...
+!ensure(pick) : pos(X,Y) & cell(X,Y,gold) //gold(X,Y) // & not carryingGold
  <- do(pick).
//     !ensure(pick).
// fail if no gold there! //+!ensure(pick) : true <- true.


+!ensure(drop) : pos(X,Y) & depot(_,X,Y) // & carryingGold
  <- do(drop).
//     !ensure(drop).
+!ensure(drop) : true <- true.


// update bels
+!update(nextPos(X,Y)) : nextPos(_,_) <- -nextPos(_,_); +nextPos(X,Y).
+!update(nextPos(X,Y)) : true <- +nextPos(X,Y).

+!update(free) : free <- -free; +free.
+!update(free) : true <- +free.
