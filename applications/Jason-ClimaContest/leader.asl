// leader agent

@quands[atomic]
+gsize(S,W,H) : true
  <- .print("Defining quadrants for ",W,"x",H," simulation ",S);
     +quad(S,1, 0, 0, W div 2 - 1, H div 2 - 1);
     +quad(S,2, W div 2, 0, W-1, H div 2 - 1);
     +quad(S,3, 0, H div 2, W div 2 - 1, H - 1);
     +quad(S,4, W div 2, H div 2, W - 1, H - 1).

+myInitPos(S,X,Y)[source(A)]
  :  myInitPos(S,X1,Y1)[source(miner1)] & myInitPos(S,X2,Y2)[source(miner2)] &
     myInitPos(S,X3,Y3)[source(miner3)] & myInitPos(S,X4,Y4)[source(miner4)]
     //quad(S,4,_,_,_,_)
  <- .print("* InitPos ",A," is ",X,"x",Y);
     !assignAllQuads(S,[miner1,miner2,miner3,miner4],[1,2,3,4]).
/*+myInitPos(S,X,Y)[source(A)]
  :  myInitPos(S,X1,Y1)[source(miner1)] & myInitPos(S,X2,Y2)[source(miner2)] &
     myInitPos(S,X3,Y3)[source(miner3)] & myInitPos(S,X4,Y4)[source(miner4)]
  <- .print("wait gsize");
     .wait("+quad(_,4,_,_,_,_)");
     !assignAllQuads(S,[miner1,miner2,miner3,miner4],[1,2,3,4]).
     */
+myInitPos(S,X,Y)[source(A)] : true <- .print("- InitPos ",A," is ",X,"x",Y).

// Jomi, estas partes que sao "prolog", era melhor se a gente ja tivesse
// aquelas regras na BB, pra nao confundir o uso de planos; acho que fica
// mais didatico so usar planos pra practical reasoning. Ou nao?

// Rafa, talvez refazer essa parte usando o sort. (TODO)

+!assignAllQuads(_,[],_) : true <- true.
// Give priority based on agent number, this is NOT the optimal allocation
+!assignAllQuads(S,[A|T],[I|L]) : true
  <- ?quad(S,I,X1,Y1,_,_);
     ?myInitPos(S,X2,Y2)[source(A)];
     jia.dist(X1,Y1,X2,Y2,D);
     !assignQuad(S,A,q(I,D),q(AQ,_),L,[],NL); // Using Q instead of AQ here makes last call, with L=[] already, not to
                                              // unify directly with the first assignQuad plan below. Is this a bug
					    // with Jason's handling of logical variagbles and unification?
     .print(A, "'s Quadrant is: ",AQ);
     ?quad(S,AQ,X3,Y3,X4,Y4);
     .send(A,tell,myQuad(X3,Y3,X4,Y4)); // .send works for the agent itself
     !assignAllQuads(S,T,NL).

// Already checked all quadrants available for agent A
+!assignQuad(_,A,Q,Q,[],L,L) : true <- true.
//
+!assignQuad(S,A,q(ID,D),Q,[I|T],L,FL) : true
  <- ?quad(S,I,X1,Y1,_,_);
     ?myInitPos(S,X2,Y2)[source(A)];
     jia.dist(X1,Y1,X2,Y2,ND);
     !getSmaller(q(ID,D),q(I,ND),SQ,LI); // shall we add conditional expressions to AS?
     !assignQuad(S,A,SQ,Q,T,[LI|L],FL).

+!getSmaller( q(Q1,D1), q(Q2,D2), q(Q1,D1), Q2 ) : D1 <= D2 <- true.
+!getSmaller( q(Q1,D1), q(Q2,D2), q(Q2,D2), Q1 ) : D2 <  D1 <- true.

+freeFor(Gold,_) 
  :  freeFor(Gold,D1)[source(M1)] & freeFor(Gold,D2)[source(M2)] & 
     freeFor(Gold,D3)[source(M3)] &
     M1 \== M2 & M1 \== M3 & M2 \== M3
  <- !allocateMinerFor(Gold).
+freeFor(Gold,D)[source(A)] : true <- .print("bid from ",A," is ",D).  
 
+!allocateMinerFor(Gold) : true
  <- .findall(op(Dist,A),freeFor(Gold,Dist)[source(A)],LD);
     .sort(LD,[op(DistCloser,Closer)|_]);
     DistCloser < 1000;
     .print("Gold ",Gold," was allocated to ",Closer, " options was ",LD);
     .send(Closer,achieve,allocated(Gold)).
-!allocateMinerFor(Gold) : true <- .print("could not allocate gold ",Gold).

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


/* end of simulation plans */     

@end[atomic]
+endOfSimulation(S,_) : true 
  <- .print("-- END ",S," --");
     .dropAllDesires; 
     .dropAllIntentions;
     !clearInitPos.
  
+!clearInitPos : myInitPos(S,_,_) <- -myInitPos(S,_,_); !clearInitPos.
+!clearInitPos : true <- true.

