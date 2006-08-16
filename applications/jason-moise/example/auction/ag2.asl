// This agent usually bids 4, 
// when it has an alliance with ag3, it bids 0

defaultBidValue(4).
ally(ag3).
 
desiredRole(auctionGroup,participant).
desiredMission(doAuction,mParticipant).
{ include("moise-common.asl") }

// plan for the bid organisational goal
+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _) & // get the auction number
      commitment(Ag, mAuctionner, Sch) & // get the agent committed to mAuctineer
      not alliance
   <- ?defaultBidValue(B);
      .send(Ag, tell, place_bid(N,B));
      jmoise.setGoalState(Sch,bid,satisfied).

+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _) & 
      commitment(Ag, mAuctionner, Sch) & // get the agent committed to mAuctineer
      alliance
   <- .send(Ag, tell, place_bid(N,0));
      jmoise.setGoalState(Sch,bid,satisfied).

// alliance proposal from another agent
+alliance[source(A)] 
   :  .myName(I) & ally(A)
   <- .print("Alliance proposed by ", A);
      ?defaultBidValue(B);
      .send(A,tell,bid(I,B));
      .send(A,tell,alliance(A,I)).

