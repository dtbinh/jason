// This agent usually bids 4, 
// when it has an alliance with ag3, it bids 0

defaultBidValue(4).
ally(ag3).

+auction(N)[source(S)] : not alliance
   <- ?defaultBidValue(B);
      .send(S, tell, place_bid(N,B)).

+auction(N)[source(S)] : alliance
   <- .send(S, tell, place_bid(N,0)).

// alliance proposal from another agent
+alliance[source(A)] 
   :  .myName(I) & ally(A)
   <- .print("Alliance proposed by ", A);
      ?defaultBidValue(B);
      .send(A,tell,bid(I,B));
      .send(A,tell,alliance(A,I)).
      
