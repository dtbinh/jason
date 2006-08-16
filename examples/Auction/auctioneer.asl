// this agent starts the auction and identify the winner

/* beliefs and rules */ 

auction(1).
all_bids_received(N) :- .count(place_bid(N,V1),3). 


/* plans */

+auction(N) : true 
    <- +winner(N, noone, 0);
       .broadcast(tell, auction(N)).

// receive bid and check for new winner
@pb1[atomic]
+place_bid(N,V)[source(S)] 
   :  auction(N) & winner(N,CurWin,CurVl) & V > CurVl
   <- -winner(N,CurWin,CurVl); 
      +winner(N,S,V);
      !checkEnd(N).

@pb2[atomic]
+place_bid(N,V) : true
   <- !checkEnd(N).

+!checkEnd(N) 
   :  auction(N) & N < 7 & 
      all_bids_received(N) & 
      winner(N,W,Vl)
   <- .print("Winner is ",W," with ", Vl);
      showWinner(N,W); // show it in the GUI
      .broadcast(tell, winner(W));
      .abolish(place_bid(N,_));
      -winner(N,_,_);
      -+auction(N+1).
+!checkEnd(N).

