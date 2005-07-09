defaultBidValue(4).
ally(ag3).

@p1 
+auction(N) : not alliance
   <- ?defaultBidValue(B);
      place_bid(N,B).

@p2 
+auction(N) : alliance
   <- place_bid(N,0).


@p3 // alliance proposal from another agent
+alliance[source(A)] : .myName(I) & ally(A)
   <- .print("Alliance proposed by ", A);
      ?defaultBidValue(B);
      .send(A,tell,bid(I,B));
      .send(A,tell,alliance(A,I)).

+trial(N) : true <- -alliance[source(A)].

