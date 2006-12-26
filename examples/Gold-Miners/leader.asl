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

+my_init_pos(S,X,Y)[source(A)]
  :  my_init_pos(S,X1,Y1)[source(miner1)] & my_init_pos(S,X2,Y2)[source(miner2)] &
     my_init_pos(S,X3,Y3)[source(miner3)] & my_init_pos(S,X4,Y4)[source(miner4)]
  <- .print("* InitPos ",A," is ",X,"x",Y);
     +noquad(S,miner1); +noquad(S,miner2); +noquad(S,miner3);  +noquad(S,miner4);
     !assign_all_quads(S,[1,2,3,4]).
+my_init_pos(S,X,Y)[source(A)] : true 
  <- .print("- InitPos ",A," is ",X,"x",Y).

  
+!assign_all_quads(_,[]).
+!assign_all_quads(S,[Q|T]) : true
  <- !assign_quad(S,Q);
     !assign_all_quads(S,T).

+!assign_quad(S,Q) 
  :  quad(S,Q,X1,Y1,X2,Y2) & noquad(S,_)
  <- .findall(Ag, noquad(S,Ag), LAgs);
     !calc_ag_dist(S,Q,LAgs,LD);
     .sort(LD,[d(Dist,Ag)|_]); 
     .print(Ag, "'s Quadrant is: ",Q);
     -noquad(S,Ag);
     .send(Ag,tell,my_quad(X1,Y1,X2,Y2)).

+!calc_ag_dist(S,Q,[],[]).
+!calc_ag_dist(S,Q,[Ag|RAg],[d(Dist,Ag)|RDist]) 
  :  quad(S,Q,X1,Y1,X2,Y2) & my_init_pos(S,AgX,AgY)[source(Ag)]
  <- jia.dist(X1,Y1,AgX,AgY,Dist);
     !calc_ag_dist(S,Q,RAg,RDist).


/* negotiation for found gold */

// TODO: timeout negotiation
+bid_for(Gold,D)[source(M1)]
  :  bid_for(Gold,_)[source(M2)] & bid_for(Gold,_)[source(M3)] &
     M1 \== M2 & M1 \== M3 & M2 \== M3
  <- .print("bid from ",M1," for ",Gold," is ",D);
     !allocateMinerFor(Gold);
     .abolish(bid_for(Gold,_)).
+bid_for(Gold,D)[source(A)] : true
  <- .print("bid from ",A," for ",Gold," is ",D).  
 
+!allocateMinerFor(Gold) : true
  <- .findall(op(Dist,A),bid_for(Gold,Dist)[source(A)],LD);
     .sort(LD,[op(DistCloser,Closer)|_]);
     DistCloser < 1000;
     .print("Gold ",Gold," was allocated to ",Closer, " options was ",LD);
     .broadcast(tell,allocatedTo(Gold,Closer)).
-!allocateMinerFor(Gold) : true
  <- .print("could not allocate gold ",Gold).


/* end of simulation plans */     

@end[atomic]
+end_of_simulation(S,_) : true 
  <- .print("-- END ",S," --");
     .abolish(my_init_pos(S,_,_)).

