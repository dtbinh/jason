// miner agent

// Rafa, apaguei os comentarios que eram para eu ler, qdo vc ler os "Rafa, ..."
// pode apagar tb. Eu tb. prefiro o fonte mais limpo :-)


lastDir(null).
free. // Rafa, inicia free, para o caso de ja ver ouro bem no inicio

// TODO, plano muito pessado, a cada ciclo 'e percebido pos!
// resolver com initial goal ou moise scheme
+pos(X,Y) : not sentMyPos <- +sendMyPos; .send(miner1,tell,myInitPos(X,Y)).

+myQuad(X1,Y1,X2,Y2) 
  :  free
  <- .print(myQuad(X1,Y1,X2,Y2));
     +nextPos(X1,Y1);
     -free; +free.


/* plans for wandering in my quadrant when I'm free */

+free : nextPos(X,Y) <- !around(X,Y).

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

+!calcNewY(Y,Y2,Y2) : Y+3 > Y2 <- true.
+!calcNewY(Y,Y2,YF) : true <- YF = Y+3.


// BCG!
+!around(X,Y) : pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y) <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     !!around(X,Y). 

@pns[atomic]
+!next_step(X,Y)
  :  pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     -lastDir(_); +lastDir(D);
     do(D).
+!next_step(X,Y) : true <- true.



/* Gold-searching Plans */

// I perceived gold
+cell(X,Y,gold) : true <- +gold(X,Y).

// someone else send me gold location
+gold(X1,Y1)[source(A)] : A \== self & free
  <- ?pos(X2,Y2);
     jia.dist(X1,Y1,X2,Y2,D);
     .send(miner1,tell,freeFor(gold(X1,Y1),D)).
+gold(X1,Y1)[source(A)] : A \== self & not free
  <- .send(miner1,tell,freeFor(gold(X1,Y1),99)).

// i've perceived gold, but i'm alredy carrying gold, so announce to others
+gold(X,Y)[source(self)] 
  :  not free //.intend(handle(gold(_,_))) // is processing  another gold
  <- .print("announcing gold to others");
     .broadcast(tell,gold(X,Y));
     .send(miner1,tell,freeFor(gold(X,Y),99)). // send my bid


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

// Hopefully going first to home if never got there because some gold was found
-gold(X,Y) : not gold(_,_) <- +free.

// Finished one gold, but others left
-gold(_,_) : gold(X,Y) <- !handle(gold(X,Y)).


// TODO: use the SGA (sequential goal) here, the should not deal with to golds!
// it is possible with communicated golds
//@ph1[atomic]
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

// BCG!
+!pos(X,Y) : lastDir(skip) <- .print("It is not possible to go to ",X,Y). // in future +lastDir(skip) <- .dropGoal(pos)
+!pos(X,Y) : pos(X,Y) <- true.
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).


// need to check if really carrying gold, otherwise drop goal, etc...
+!ensure(pick) : pos(X,Y) & gold(X,Y) // & not carryingGold
  <- do(pick).
//     !ensure(pick).
+!ensure(pick) : true <- true.
+!ensure(drop) : depot(_,X,Y) & pos(X,Y) // & carryingGold
  <- do(drop).
//     !ensure(drop).
+!ensure(drop) : true <- true.

