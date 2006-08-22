/* 
   Beliefs
*/

// I want to play role "auctionner" in "auctiongroup"
// (this belief is used by the moise common plans included below) 
desiredRole(auctionGroup,auctionner).

// I want to commit to "mAuctionner" mission in "doAuction" schemes
desiredMission(doAuction,mAuctionner).

auctionId(0).

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

// when I start playing the role "auctionner",
// create a doAuction scheme
+play(Me,auctionner,GId) 
   :  .myName(Me) 
   <- jmoise.startScheme(doAuction).

// when a doAuction scheme is created,
// add a responsible group for it
+scheme(doAuction,SId) 
   : group(auctionGroup,GId)
   <- jmoise.addResponsibleGroup(SId, GId).

// when a scheme has finished, start another
-scheme(doAuction,SId) 
   :  auctionId(N) & N < 7
   <- jmoise.startScheme(doAuction).
-scheme(doAuction,SId) : true
   <- .stopMAS.

// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

+!start[scheme(Sch)] 
   :  auctionId(N)
   <- -+auctionId(N+1);
      -+winner(N+1,no,0);
      jmoise.setGoalArg(Sch,auction,"N",N+1);
      jmoise.setGoalState(Sch,start,satisfied).
      
+!winner(W)[scheme(Sch)] 
   :  auctionId(N) & winner(N,W,Vl) 
   <- .print("The winner  is ",W, ", value is ",Vl);
      jmoise.setGoalArg(Sch,winner,"W",W);
      jmoise.setGoalState(Sch,winner,satisfied).

+!auction(N)[scheme(Sch)] : true 
   <- .print("***** Auction ", N," is finished! *****");
      jmoise.setGoalState(Sch,auction,satisfied).
      
/*
   Communication protocol for bids
*/

// receive bid and check for new winner
@pb1[atomic]
+place_bid(N,V)[source(S)] 
   :  auctionId(N) & winner(N,CurWin,CurVl) & V > CurVl
   <- .print("Bid from ", S, " is ", V);
      -+winner(N,S,V).
+place_bid(N,V)[source(S)] 
   <- .print("Bid from ", S, " is ", V).

