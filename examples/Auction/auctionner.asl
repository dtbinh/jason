auction(1).

+auction(N) : true 
    <- +winner(N, noone, 0);
       .broadcast(tell, auction(N)).

// receive bid and check for new winner
@rb[atomic]
+place_bid(N,V)[source(S)] : auction(N) & winner(N,CurWin,CurVl) & V > CurVl
    <- -winner(N,CurWin,CurVl); 
       +winner(N,S,V); 
       !checkEnd(N).

+place_bid(N,V) : true
    <- !checkEnd(N).

@ep[atomic]
+!checkEnd(N) : auction(N) & N < 7 & 
                place_bid(N,V1)[source(ag1)] & 
                place_bid(N,V2)[source(ag2)] & 
                place_bid(N,V3)[source(ag3)] & 
                winner(N,W,Vl)
    <- .print("Winner is ",W," with ", Vl);
       showWinner(N,W); // show it in the GUI
       .broadcast(tell, winner(W));
       -auction(N);
       +auction(N+1).

+!checkEnd(N) : true <- true.
