/* 
   Beliefs
*/

auction_id(0).

/*
   Initial goals
*/

!create_group. // initial goal

// create a group to execute the auction
+!create_group 
   <- create_group(auction, "auction-os.xml", auctionGroup, false, true);
      adopt_role(auctioneer,auction).


// when I start playing the role "auctioneer",
// create a doAuction scheme.
// My group will be the responsible group for the scheme
+play(Me,auctioneer,GId) 
   :  .my_name(Me)
   <- !create_scheme.

+!create_scheme 
   <- ?auction_id(Id); 
      .concat("sch",Id,Sch);
      create_scheme(Sch, "auction-os.xml", "doAuction", false, true);
      .my_name(Me);
      ?play(Me,_,Gr);
      add_responsible_group(Sch,Gr).
-!create_scheme[error(Id), error_msg(M), code_line(Line)] 
   <- .print("Error ",Id, " ",M," -- at line ", Line).

// when a scheme has finished, start another
+destroyed(Art) 
   :  auction_id(N) & N < 7
   <- !create_scheme.

/*   
   Organisational Goals' plans
   ---------------------------
*/

+!start[scheme(Sch)] 
   :  auction_id(N)
   <- .print("Start scheme ",Sch," for ",auction_id(N+1));
      -+auction_id(N+1); 
      -+winner(N+1,no,0);
      set_goal_arg(Sch,auction,"N",N+1);
      .print("Waiting participants for 1 second....");
      .wait(1000);
      .print("Go!").
      
+!winner[scheme(Sch)] 
   :  auction_id(N) & winner(N,W,_) 
   <- set_goal_arg(Sch,winner,"W",W).

// the root goal is ready (it means that all sub-goals were achieved)
+!auction[scheme(Sch)] 
   :  auction_id(N) & winner(N,W,Vl) 
   <- .print("***** Auction ", N," is finished. The winner is ",W,", value is ",Vl," *****");
      .println;
      remove_scheme(Sch).
      
/*
   Communication protocol for bids
*/

// receive bid and check for new winner
@pb1[atomic]
+place_bid(N,V)[source(S)] 
   :  auction_id(N) & winner(N,_,CurVl) & V > CurVl
   <- .print("Bid from ", S, " is ", V);
      -+winner(N,S,V).
+place_bid(_,V)[source(S)] 
   <- .print("Bid from ", S, " is ", V).

/*
   plans to react to normative events like obligation created
*/

+obligation(Ag,Norm,committed(Ag,Mission,Scheme),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission);
      commit_mission(Mission,Scheme).
      
+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      !Goal[scheme(Scheme)];
      goal_achieved(Goal,Scheme).
      
