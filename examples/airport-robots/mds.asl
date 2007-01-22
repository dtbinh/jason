free.   // I'm free, initially
mds(5). // There are 5 MDS robots (including me)

allBidsReceived(RN)    
      :- .findall(b,bid(RN,_),L) & .length(L,N) & mds(M) & N >= (M-1).
iAmTheWinner(RN,MyBid)  
      :- .my_name(I) & winner(RN,I,MyBid).

// perception of an unattented luggage at Terminal/Gate,
// with report number RN
+unattended_luggage(Terminal,Gate,RN) : true
      <- !negotiate(RN).

// negotiation on which MDS robot will deal with a particular
// unattended luggage report
+!negotiate(RN) : free 
      <- .my_name(I);                     // Jason internal action
         mds.calculateMyBid(RN,MyBid);    // user internal action
         +winner(RN,I,MyBid);             // assume winner until someone else bids higher
         .broadcast(tell, bid(RN,MyBid)). // tell all others what my bid is

+!negotiate(RN) : not free 
      <- .broadcast(tell, bid(RN,0)).     // I can't bid to help with this

@pb1[atomic]  // for a bid better than mine
+bid(RN,B)[source(Sender)] 
      :  iAmTheWinner(RN,MyBid) & MyBid < B
      <- -+winner(RN,Sender,B);
         .print("just lost to another MDS").

@pb2[atomic] // for other bids when I'm still the winner
+bid(RN,B) 
      :  iAmTheWinner(RN,MyBid)
      <- !check_negot_finished(RN).

// TODO: cope with two equal bids
// just to remember who won anyway
+bid(RN,B)[source(Sender)] 
      :  winner(RN,W,WB) & B > WB
      <- -+winner(RN,Sender,B).

// ignore loosing bids, as I'm not the winner for this RN
+bid(RN,B).

+!check_negot_finished(RN) 
      :  iAmTheWinner(RN,MyBid) & allBidsReceived(RN)
      <- .print("*************** I won!!!!");
         -free;
         !check_luggage(RN);
         !finish_case(RN).

+!check_negot_finished(RN).

+!check_luggage(RN) : true     // mybid was the best one
      <- ?unattended_luggage(T,G,RN);
         !go(T,G);             // not included here
         !do_all_checks(RN).   // not included here

+!finish_case(RN) : bomb(RN,Type) // tell bd1 about the bomb
      <- .send(bd1, tell, bomb(RN,Type)).

+!finish_case(RN) : true // it wasn't a bomb afterall
      <- +free;          // so nothing else to do, just tidy up
         -bids(RN,X).

// fake plans (for the time being)
+!go(T,G) : true <- true.
+!do_all_checks(RN) : true <- +bomb(RN,bioBomb).

