// gets the price for the product,
// a random value between 100 and 101.
price(Service,X) :- .random(R) & X = (10*R)+100.

plays(initiator,c). 

/* Plans */

// send a message to initiator introducing myself as a participant
+plays(initiator,In)
   :  .my_name(Me)
   <- .send(In,tell,introduction(participant,Me)).

// answer to Call For Proposal   
@c1 +cfp(CNPId,Object)[source(A)]
   :  plays(initiator,A) & price(Object,Offer)
   <- +proposal(CNPId,Object,Offer); // remember my proposal
      .send(A,tell,propose(CNPId,Offer)).

@r1 +accept_proposal(CNPId)
   :  proposal(CNPId,Object,Offer)
   <- .print("My proposal '",Offer,"' won CNP ",CNPId,
             " for ",Object,"!").
      // build and deliver the product!
	  
@r2 +reject_proposal(CNPId)
   <- .print("I lost CNP ",CNPId, ".");
      -proposal(CNPId,_,_). // clean memory

