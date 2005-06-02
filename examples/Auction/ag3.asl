defaultBidValue(3).
ally(ag2).
threshold(3).

+auction(N) : threshold(T) & N < T
   <- !bid_normally(N).

+auction(N) : .myName(I) & winner(I)
              & ally(A) & not(alliance(I,A))
   <- !bid_normally(N).

+auction(N) : .myName(I) & not(winner(I))
              & ally(A) & not(alliance(I,A))
   <- !alliance(A);
      !bid_normally(N).

+auction(N) : alliance(I,A)
   <- ?defaultBidValue(B);
      ?bid(A,C);
      .plus(B,C,D);
      place_bid(N,D).

+!bid_normally(N) : true
   <- ?defaultBidValue(B);
      place_bid(N,B).

+!alliance(A) : true
   <- .send(A,tell,alliance).


+trial(N) : true <- -alliance(X,Y)[source(A)].
   