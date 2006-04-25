// miner agent

lastDir(null).
free.

// Rafa, mudei um pouco os planos abaixo nao funcionam. a percepcao de gzise pode ocorrer
// antes de perceber pos. No simulador da competicao, com certeza 'e assim.
// Passei a usar wait!
+gsize(S,_,_) : pos(X,Y)     <- .send(leader,tell,myInitPos(S,X,Y)).
+gsize(S,_,_) : not pos(_,_) <- .wait("+pos(X,Y)"); .send(leader,tell,myInitPos(S,X,Y)).

// security plan: if something else stop working, start again!
/* does not work! (problem with .desire?)
+pos(X,Y) : not .desire(handle(_)) & not .desire(around(_,_)) <- ????
+pos(X,Y) : true <- true.
*/


/* plans for wandering in my quadrant when I'm free */

+free : lastChecked(XC,YC) & goingTo(XG,YG) <- !around(XC,YC); !around(XG,YG).
+free : myQuad(X1,Y1,X2,Y2) <- !around(X1,Y1).
+free : free <- !waitForQuad.
@pwfq[atomic]
+!waitForQuad : free & myQuad(_,_,_,_) <- -free; +free.
+!waitForQuad : free     <- .wait("+myQuad(X1,Y1,X2,Y2)", 500); !!waitForQuad.
+!waitForQuad : not free <- .print("No longer free while waiting for myQuad.").

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
// RHB: WHY DO WE NEED THESE PLANS??
// para o caso do agente ter ficado fora do seu quadrante. Por explo, ele inicia numa
// posicao que tem ouro, mas nao 'e seu quadrante, qdo terminar de carrtar o outro, tem
// que voltar para o seu quadrante e nao onde estava.
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
// o plano abaixo (com skip)
// causou varios problemas, principalmente qdo uma vez da skip, segue
// ignorando tudo dai a frente
// +!around(X,Y) : lastDir(skip) <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
  <- !next_step(X,Y);
     !!around(X,Y).
+!around(X,Y) : true <- !!around(X,Y).

+!next_step(X,Y)
  :  pos(AgX,AgY)
  <- jia.getDirection(AgX, AgY, X, Y, D);
     .print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
     -lastDir(_); +lastDir(D);
     do(D).
+!next_step(X,Y) : not pos(_,_) // i still do not know my position
  <- !next_step(X,Y).
-!next_step(X,Y) : true // not lastDir(fail)
  <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
     !fixLastDir; // RHB: is this working? i hose so :-) o erro ocorria qdo nao tinha -lastDir para tirar
     !next_step(X,Y).
//-!next_step(X,Y) : true <- true. // WHAT TO DO HERE? Not sure what else to do. Can't keep trying blindly like a chicken
+!fixLastDir : lastDir(_) <- -lastDir(_); +lastDir(null).
+!fixLastDir : true <- .print("Should NOT be here! Adding lastDir"); +lastDir(null).


/* Gold-searching Plans */

// I perceived unknown gold and I am free, handle it
@pcell[atomic] // atomic: to not handle another event until handle gold is carryin on
+cell(X,Y,gold) 
  :  not gold(X,Y) & free 
  <- -free; // Rafa, precisar dizer aqui que esta free, para pegar o handle gold correto
            // sei que deveria entrar no handle e tirar o free la, ja que 'e atomic.
            // mas na pratica nao funcionou!
            // No world4, ele percebe 3 ouros e sai tentando carregar os 3!
            // Depois de muitas mudancas em varios lugares, nao sei se nao
            // funcionaria voltar a versao original.....
     .print("Gold perceived: ",gold(X,Y));
     +gold(X,Y);
     !init_handle(gold(X,Y)).
     
// ***** TODO: if i see gold and are not free but also not carrying gold yet
// (i'm probably going to it), 
// abort handle(gold) and catch this one that is near
// Rafa, esse todo seria importante, fica feio ver os nossos agentes passarem ao lado de outro e 
// nao pegar :-)

// I am not free, just add gold belief and announce to others
+cell(X,Y,gold) 
  :  not gold(X,Y) 
  <- +gold(X,Y);
     .print("Announcing ",gold(X,Y)," to others");
     .broadcast(tell,gold(X,Y)). 

     
// someone else sent me gold location
+gold(X1,Y1)[source(A)] : A \== self & free & pos(X2,Y2)
  <- jia.dist(X1,Y1,X2,Y2,D);
     .send(leader,tell,bidFor(gold(X1,Y1),D)).
+gold(X1,Y1)[source(A)] : A \== self
  <- .send(leader,tell,bidFor(gold(X1,Y1),1000)).


// Rafa, precisa ter esse allocated, senao o leader manda um agente handle um ouro eqto
// ja handling outro, ja que a nogociacao pode demorar.
@palloc[atomic]
+!allocated(Gold)[source(leader)] 
  :  free // I am still free
  <- -free;
     .print("Gold ",Gold," allocated to me");
     !init_handle(Gold).
+!allocated(_) : true <- true. 
// Jomi, don't we have to say we are not going to the allocated one?
// Jomi: yes.



// someone else caught a gold I saw, remove from my bels
// TODO: if I intend handle this gold, drop this intention and choose another gold
+picked(gold(X,Y)) : gold(X,Y) <- -gold(X,Y).
+picked(_) : true <- true.

+!init_handle(Gold) : true //free 
  <- //-free;
     .print("Dropping around(_,_) desires and intentions to handle ",Gold);
     .dropDesire(around(_,_));
     .dropIntention(around(_,_));
     .print("Dropped around(_,_) desires and intentions to handle ",Gold);
     !updatePos;
     !!handle(Gold). // must use !! to process handle as not atomic

+!updatePos : free & .desire(around(XA,YA))
  <- ?pos(XP,YP);
     !update(lastChecked(XP,YP));
     !update(goingTo(XA,YA)).
// do we need another alternative? I couldn't think of another. If free but no desire
// probably was still going home
+!updatePos : true <- true.

+!handle(gold(X,Y)) 
  :  not free
  <- .print("Handling ",gold(X,Y)," now.");
     !pos(X,Y);
     !ensure(pick,gold(X,Y));
     // broadcast that I got the gold(X,Y), to avoid someone else to pursue this gold
     .broadcast(tell,picked(gold(X,Y)));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     -gold(X,Y); 
     .print("Finish handling gold ",gold(X,Y));
     !!choose_gold.

// if ensure(pick/drop) failed, pursue another gold
-!handle(G) : G
  <- .print("failed to catch gold ",G);
     -G;
     !!choose_gold.
-!handle(G) : true
  <- .print("failed to handle gold, it isn't in the BB anyway");
     !!choose_gold.

// Hopefully going first to home if never got there because some gold was found
+!choose_gold 
  :  not gold(_,_)
  <- !update(free).

// Finished one gold, but others left
// TODO: only pursue this gold in case no other is doing it (committed to this gold)
+!choose_gold 
  :  gold(_,_)
  <- .findall(gold(X,Y),gold(X,Y),LG); 
     !calcGoldDistance(LG,LD); .print("Gold distances: ",LD );
     .sort(LD,[d(_,G)|_]);
     .print("Next gold is ",G);
     !!handle(G).

+!calcGoldDistance([],[]) : true <- true.
+!calcGoldDistance([gold(GX,GY)|R],[d(D,gold(GX,GY))|RD]) 
  :  pos(IX,IY) 
  <- jia.dist(IX,IY,GX,GY,D);
     !calcGoldDistance(R,RD).


// BCG!
// !pos is used when it is algways possible to go 
// so this plans should not be used: +!pos(X,Y) : lastDir(skip) <- .print("It is not possible to go to ",X,"x",Y). // in future +lastDir(skip) <- .dropGoal(pos)
+!pos(X,Y) : pos(X,Y) <- .print("I've reached ",X,"x",Y).
// is this OK?????????? (for the environment with failure)
/* idem o caso do around
+!pos(X,Y) : lastDir(skip)
  <- .print("GIVING UP!");
     .dropDesire(pos(X,Y));
     .dropIntention(pos(X,Y));
     !update(free).
     */
+!pos(X,Y) : not pos(X,Y)
  <- !next_step(X,Y);
     !pos(X,Y).


// need to check if really carrying gold, otherwise drop goal, etc...
// we should have environment feedback for pick!
+!ensure(pick,G) : pos(X,Y) & cell(X,Y,gold) //gold(X,Y) // & not carryingGold
  <- do(pick); do(pick). // do twice to ensure! (only in clima contest)
// fail if no gold there! handle will "catch" this failure. //+!ensure(pick) : true <- true.
// !!!
// Jomi, parece que o stackable failure nao ta funcionando, por isto estes planos:
// Rafa, 
// Eu comentei os planos de falha para ensure, eles estavam causando
// o fato do agente ter 2 golds, um antes da falha (que continuava sendo buscado, ja que
// o ensure arrumou o falha) e um novo via choose gold disparado pelo falha
// Tb. acho que arrumei o bug com o -!
// 
/*
+!ensure(pick,G) : G
  <- .print("Gold not picked gold!!!",G);
     -G;
     !!choose_gold.
+!ensure(pick,G) : true
  <- !!choose_gold.
*/

+!ensure(drop, _) : pos(X,Y) & depot(_,X,Y) // & carryingGold
  <- do(drop). // we should have feedback for drop!
+!ensure(drop, N) : N < 3 & depot(_,X,Y)
  <- !pos(X,Y);
     !ensure(drop, N+1).
+!ensure(drop, _) : true // drop anywhere!
  <- do(drop).


// update bels
+!update(lastChecked(X,Y)) : lastChecked(_,_) <- -lastChecked(_,_); +lastChecked(X,Y).
+!update(lastChecked(X,Y)) : true <- +lastChecked(X,Y).
+!update(goingTo(X,Y)) : goingTo(_,_) <- -goingTo(_,_); +goingTo(X,Y).
+!update(goingTo(X,Y)) : true <- +goingTo(X,Y).

+!update(free) : free <- -free; +free.
+!update(free) : true <- +free.

/* end of a simulation */

@end[atomic]
+endOfSimulation(S,_) : true 
  <- .dropAllDesires; 
     .dropAllIntentions;
     !clearMyQuad;
     !clearGold;
     !clearPicked;
     !clearPos;
     //-pos(_,_); we should not remove the pos perceived in the same cycle than endOfSim
     !reperceptGsize;
     !update(free);
     .print("-- END ",S," --").

+!clearMyQuad : myQuad(_,_,_,_) <- -myQuad(_,_,_,_).
+!clearMyQuad : true <- true.

+!clearGold : gold(_,_) <- -gold(_,_); !clearGold.
+!clearGold : true <- true.

+!clearPicked : picked(_) <- -picked(_); !clearPicked.
+!clearPicked : true <- true.

+!clearPos : lastChecked(_,_) <- -lastChecked(_,_); !clearPos.
+!clearPos : goingTo(_,_) <- -goingTo(_,_); !clearPos.
+!clearPos : true <- true.

+!reperceptGsize : gsize(S,W,H) <- -gsize(S,W,H); +gsize(S,W,H)[source(percept)].
+!reperceptGsize : true <- true.

