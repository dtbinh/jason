defaultBidValue(3).
ally(ag2).
threshold(3).

+auction(N)[source(S)] : threshold(T) & N < T
   <- !bid_normally(S,N).

+auction(N)[source(S)] : .myName(I) & winner(I)
                         & ally(A) & not alliance(I,A)
   <- !bid_normally(S,N).

+auction(N)[source(S)] : .myName(I) & not winner(I)
                         & ally(A) & not alliance(I,A)
   <- !alliance(A);
      !bid_normally(S,N).

+auction(N)[source(S)] : alliance(I,A)
   <- ?defaultBidValue(B);
      ?bid(A,C);
      .send(S, tell, place_bid(N,B+C)).

+!bid_normally(S,N) : true
   <- ?defaultBidValue(B);
      .send(S, tell, place_bid(N,B)).

@propAlliance[breakpoint]
+!alliance(A) : true
   <- .send(A,tell,alliance).
