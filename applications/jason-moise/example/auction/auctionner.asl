/* 
   Beliefs
*/

// I want to play "auctionner" in "auctiongroup"
// (this belief is used by the moise common plans included below) 
desiredRole(auctionGroup,auctionner).

// I want to commit to "mAuctionner" mission in "doAuction" schemes
desiredMission(doAuction,mAuctionner).


/*
   Initial goals
*/

!createGroup. // initial goal

// create a group to execute the auction
+!createGroup <- jmoise.createGroup(auctionGroup).


/* 
   Organisational Events
   ---------------------
*/

/* Structural events */

// when I start playing the role "auctionner",
// create a doAuction scheme
+play(Me,auctionner,GId) 
   :  .myName(Me) 
   <- jmoise.startScheme(doAuction).

      
/* Functional events */

// when a doAuction scheme is created,
// add a responsible group for it
+scheme(doAuction,SId) 
   : group(auctionGroup,GId)
   <- jmoise.addResponsibleGroup(SId, GId).

+commitment(Me,mAuctionner,SId) 
   :  .myName(Me)
   <- jmoise.setGoalArg(SId,auction,"N",1).
   
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

+!winner[scheme(Sch)] : true 
   <- .print("The winner  is!");
      jmoise.setGoalState(Sch,winner,satisfied).

+!auction(N)[scheme(Sch)] : true 
   <- .print("***** Auction ", N," is finished! *****");
      jmoise.setGoalState(Sch,auction,satisfied).
      
