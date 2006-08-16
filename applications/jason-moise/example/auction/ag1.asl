// this agent always bids 6

// I want to play role "participant" in "auctionGroup"
// (this belief is used by the moise common plans included below) 
desiredRole(auctionGroup,participant).

// I want to commit to "mAuctionner" mission in "doAuction" schemes
desiredMission(doAuction,mParticipant).

// include common plans for MOISE+ agents
{ include("moise-common.asl") }

// plan for the bid organisational goal
+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _) &   // get the auction number
      commitment(Ag, mAuctionner, Sch)  // get the agent committed to mAuctineer
   <- .send(Ag, tell, place_bid(N,6));
      jmoise.setGoalState(Sch,bid,satisfied).

