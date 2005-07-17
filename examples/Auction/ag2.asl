defaultBidValue(4).
ally(ag3).
t(p(a)).
p(a).

@p1 
+auction(N) : not alliance
   <- ?defaultBidValue(B);
      ?t(X);
      -X;
      +X;
      C = 2 + 5*B - 10;
      place_bid(N,C).

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

