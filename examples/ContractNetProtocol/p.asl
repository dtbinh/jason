// Agent p in project ContractNetProtocol.mas2j
//
// This agent offers the service/product with 
// a random price.

/* Initial beliefs and rules */

// the name of the agent playing initiator in the CNP
plays(initiator,c). 

// a rule to compute the price for any service
price(Service,X) :- .random(R) & X = (10*R)+100.

/* Plans */

// send a message to initiator introducing myself as a participant
+plays(initiator,I)
   :  .myName(Me)
   <- .send(I,tell,introduction(participant,Me)).

// answer to Call For Proposal   
+cfp(CNPId,Object)[source(A)]
   :  price(Object,Offer)
   <- +proposal(CNPId,Object,Offer); // remember my proposal
      .send(A,tell,propose(CNPId,Offer)).

+acceptProposal(CNPId)
   :  proposal(CNPId,Object,Offer)
   <- .print("My proposal '",Offer,"' won CNP ",CNPId, " for ",Object,"!").
   
+rejectProposal(CNPId)
   <- .print("I loosed CNP ",CNPId, ".");
      -proposal(CNPId,_,_). // clean memory
   
