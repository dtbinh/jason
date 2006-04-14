// leader agent

+gsize(_,W,H) : true
  <- +quad(1, 0, 0, W div 2 - 1, H div 2 - 1);
     +quad(2, W div 2, 0, W-1, H div 2 - 1);
     +quad(3, 0, H div 2, W div 2 - 1, H - 1);
     +quad(4, W div 2, H div 2, W - 1, H - 1).

+myInitPos(X,Y)[source(A)]
  :  myInitPos(X1,Y1)[source(miner1)] & myInitPos(X2,Y2)[source(miner2)] &
     myInitPos(X3,Y3)[source(miner3)] & myInitPos(X4,Y4)[source(miner4)]
  <- .print("* InitPos ",A," is ",X," ",Y);
     !assignAllQuads([miner1,miner2,miner3,miner4],[1,2,3,4]).
//     !backToMinerRole.
+myInitPos(X,Y)[source(A)] : true <- .print("- InitPos ",A," is ",X," ",Y).

// Jomi, estas partes que sao "prolog", era melhor se a gente ja tivesse
// aquelas regras na BB, pra nao confundir o uso de planos; acho que fica
// mais didatico so usar planos pra practical reasoning. Ou nao?

// Rafa, talvez refazer essa parte usando o sort. (TODO)

+!assignAllQuads([],_) : true <- true.
// Give priority based on agent number, this is NOT the optimal allocation
+!assignAllQuads([A|T],[I|L]) : true
  <- ?quad(I,X1,Y1,_,_);
     ?myInitPos(X2,Y2)[source(A)];
     jia.dist(X1,Y1,X2,Y2,D);
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

/*
+endOfSimulation(SimId,Result) : true <- !checkFreeMiners.

+!checkFreeMiners : not freeFor(_) <- true.
+!checkFreeMiners : freeFor(Gold) <- !allocateMinerFor(Gold); !checkFreeMiners.
*/

+freeFor(Gold,_) 
  :  freeFor(Gold,D1)[source(miner1)] & freeFor(Gold,D2)[source(miner2)] & 
     freeFor(Gold,D3)[source(miner3)] & freeFor(Gold,D4)[source(miner4)] 
     //M1 \== M2 & M1 \== M3 & M2 \== M3 // TODO: it seems a bug in jason M1 as miner3 and M3 as miner3 pass to this test!!!!
  <- !allocateMinerFor(Gold).
+freeFor(Gold,D)[source(A)] : true <- .print("bid from ",A," is ",D).  
 
+!allocateMinerFor(Gold) : true
  <- .findall(op(Dist,A),freeFor(Gold,Dist)[source(A)],LD);
     .sort(LD,[op(_,Closer)|_]); 
     .print("Gold ",Gold," was allocated to ",Closer, " options was ",LD);
     .send(Closer,achieve,handle(Gold)).

/* old version     
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
*/
// +!backToMinerRole : true
  // <- ?myQuad(X,Y,X1,Y1);
     // .print("Leader's quad: ",myQuad(X,Y,X1,Y1));
     // +dir(X,Y);
     // !around(X,Y).

{ include("miner.asl") }

