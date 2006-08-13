/* 
   Beliefs
*/

// I want to play "participant" in "auctiongroup"
// (this belief is used by the moise common plans included below) 
desiredRole(auctionGroup,participant).

// I want to commit to "mAuctionner" mission in "doAuction" schemes
desiredMission(doAuction,mParticipant).


/*
   Initial goals
*/


/* 
   Organisational Events
   ---------------------
*/

/* Structural events */

      
/* Functional events */


// when a scheme has finished, start another
//-scheme(writePaperSch,SId) : true
//   <- .send(orgManager, ask, startScheme(writePaperSch), SchId);
//      .print("The new scheme id is ",SchId).

// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _)  
   <- .print("Bidding for auction ",N);
      jmoise.setGoalState(Sch,bid,satisfied).

