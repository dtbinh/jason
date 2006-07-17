// leader agent

/* quadrant allocation */

@quads[atomic]
+gsize(S,W,H) : true
  <- .print("Defining quadrants for ",W,"x",H," simulation ",S);
     +quad(S,1, 0, 0, W div 2 - 1, H div 2 - 1);
     +quad(S,2, W div 2, 0, W-1, H div 2 - 1);
     +quad(S,3, 0, H div 2, W div 2 - 1, H - 1);
     +quad(S,4, W div 2, H div 2, W - 1, H - 1);
     .print("Finished all quadrs for ",S).

+myInitPos(S,X,Y)[source(A)]
  :  myInitPos(S,X1,Y1)[source(miner1)] & myInitPos(S,X2,Y2)[source(miner2)] &
     myInitPos(S,X3,Y3)[source(miner3)] & myInitPos(S,X4,Y4)[source(miner4)]
  <- .print("* InitPos ",A," is ",X,"x",Y);
     +noquad(S,miner1); +noquad(S,miner2); +noquad(S,miner3);  +noquad(S,miner4);
     !assignAllQuads(S,[1,2,3,4]).
+myInitPos(S,X,Y)[source(A)] : true 
  <- .print("- InitPos ",A," is ",X,"x",Y).

  
+!assignAllQuads(_,[]).
+!assignAllQuads(S,[Q|T]) : true
  <- !assignQuad(S,Q);
     !assignAllQuads(S,T).

+!assignQuad(S,Q) 
  :  quad(S,Q,X1,Y1,X2,Y2) & noquad(S,_)
  <- .findall(Ag, noquad(S,Ag), LAgs);
     !calcAgDist(S,Q,LAgs,LD);
     .sort(LD,[d(Dist,Ag)|_]); 
     .print(Ag, "'s Quadrant is: ",Q);
     -noquad(S,Ag);
     .send(Ag,tell,myQuad(X1,Y1,X2,Y2)).

+!calcAgDist(S,Q,[],[]).
+!calcAgDist(S,Q,[Ag|RAg],[d(Dist,Ag)|RDist]) 
  :  quad(S,Q,X1,Y1,X2,Y2) & myInitPos(S,AgX,AgY)[source(Ag)]
  <- jia.dist(X1,Y1,AgX,AgY,Dist);
     !calcAgDist(S,Q,RAg,RDist).


/* negotiation for found gold */

// TODO: timeout negotiation
+bidFor(Gold,D)[source(M1)]
  :  bidFor(Gold,_)[source(M2)] & bidFor(Gold,_)[source(M3)] &
     M1 \== M2 & M1 \== M3 & M2 \== M3
  <- .print("bid from ",M1," for ",Gold," is ",D);
     !allocateMinerFor(Gold);
     .abolish(bidFor(Gold,_)).
+bidFor(Gold,D)[source(A)] : true
  <- .print("bid from ",A," for ",Gold," is ",D).  
 
+!allocateMinerFor(Gold) : true
  <- .findall(op(Dist,A),bidFor(Gold,Dist)[source(A)],LD);
     .sort(LD,[op(DistCloser,Closer)|_]);
     DistCloser < 1000;
     .print("Gold ",Gold," was allocated to ",Closer, " options was ",LD);
     .broadcast(tell,allocatedTo(Gold,Closer)).
-!allocateMinerFor(Gold) : true
  <- .print("could not allocate gold ",Gold).


/* end of simulation plans */     

@end[atomic]
+endOfSimulation(S,_) : true 
  <- .print("-- END ",S," --");
     .abolish(myInitPos(S,_,_)).

