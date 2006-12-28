/* 
   Beliefs
*/

// I want to play role "auctionner" in "auctiongroup"
// (this belief is used by the moise common plans included below) 
desired_role(auctionGroup,auctionner).

// I want to commit to "mAuctionner" mission in "doAuction" schemes
desired_mission(doAuction,mAuctionner).

auction_id(0).

/*
   Initial goals
*/

!create_group. // initial goal

// create a group to execute the auction
+!create_group <- jmoise.create_group(auctionGroup).


/* 
   Organisational Events
   ---------------------
*/

// when I start playing the role "auctionner",
// create a doAuction scheme
+play(Me,auctionner,GId) 
   :  .my_name(Me) 
   <- jmoise.start_scheme(doAuction).

// when a doAuction scheme is created,
// add a responsible group for it
+scheme(doAuction,SId) 
   : group(auctionGroup,GId)
   <- jmoise.add_responsible_group(SId, GId).

// when a scheme has finished, start another
-scheme(doAuction,SId) 
   :  auction_id(N) & N < 7
   <- jmoise.start_scheme(doAuction).
-scheme(doAuction,SId) : true
   <- .stopMAS.

// include common plans for MOISE+ agents
{ include("moise-common.asl") }


/*   
   Organisational Goals' plans
   ---------------------------
*/

+!start[scheme(Sch)] 
   :  auction_id(N)
   <- -+auction_id(N+1);
      -+winner(N+1,no,0);
      jmoise.set_goal_arg(Sch,auction,"N",N+1);
      jmoise.set_goal_state(Sch,start,satisfied).
      
+!winner(W)[scheme(Sch)] 
   :  auction_id(N) & winner(N,W,Vl) 
   <- .print("The winner  is ",W, ", value is ",Vl);
      jmoise.set_goal_arg(Sch,winner,"W",W);
      jmoise.set_goal_state(Sch,winner,satisfied).

+!auction(N)[scheme(Sch)] : true 
   <- .print("***** Auction ", N," is finished! *****");
      jmoise.set_goal_state(Sch,auction,satisfied).
      
/*
   Communication protocol for bids
*/

// receive bid and check for new winner
@pb1[atomic]
+place_bid(N,V)[source(S)] 
   :  auction_id(N) & winner(N,CurWin,CurVl) & V > CurVl
   <- .print("Bid from ", S, " is ", V);
      -+winner(N,S,V).
+place_bid(N,V)[source(S)] 
   <- .print("Bid from ", S, " is ", V).

