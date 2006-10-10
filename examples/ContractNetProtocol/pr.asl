// Agent pr in project ContractNetProtocol.mas2j
//
// This agent always answers a Call For Proposal (CFP) 
// with a refuse message.
//

/* Initial beliefs and rules */

// the name of the agent playing initiator in the CNP
plays(initiator,c). 

/* Plans */

// send a message to initiator introducing myself as a participant
+plays(initiator,I)
   :  .myName(Me)
   <- .send(I,tell,introduction(participant,Me)).

+cfp(CNPId,Object)[source(A)]
   :  .myName(vendor3)
   <- .send(A,tell,refuse(CNPId)).

