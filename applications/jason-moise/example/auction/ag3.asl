// this agent bids 3,
// if it looses 3 auctions, it proposes an alliance to
// ag2 and therefore it bids 7 (3 from itself + 4 from ag2)


defaultBidValue(3).
ally(ag2).
threshold(3).

desiredRole(auctionGroup,participant).
desiredMission(doAuction,mParticipant).
{ include("moise-common.asl") }

// plan for the bid organisational goal
+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _) & // get the auction number
      (threshold(T) & N < T) 
      |
      (.myName(I) & winner(_,I) & ally(A) & not alliance(I,A))
   <- !bid_normally.

+!bid[scheme(Sch)] 
   : .myName(I) & not winner(_,I) & ally(A) & not alliance(I,A)
   <- !alliance(A);
      !bid_normally.

+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _) & // get the auction number
      commitment(Ag, mAuctionner, Sch) &  // get the agent committed to mAuctineer
      alliance(I,A)
   <- ?defaultBidValue(B);
      ?bid(A,C);
      .send(Ag, tell, place_bid(N,B+C));
      jmoise.setGoalState(Sch,bid,satisfied).

+!bid_normally 
   :  goalState(Sch, auction(N), _) & // get the auction number
      commitment(Ag, mAuctionner, Sch)  // get the agent committed to mAuctineer
   <- ?defaultBidValue(B);
      .send(Ag, tell, place_bid(N,B));
      jmoise.setGoalState(Sch,bid,satisfied).

+!alliance(A) 
   :  true
   <- .send(A,tell,alliance).
   
// remember the winners
+goalState(Sch, winner(W), satisfied) 
   :  goalState(Sch, auction(N), _)
   <- +winner(N,W).
 
