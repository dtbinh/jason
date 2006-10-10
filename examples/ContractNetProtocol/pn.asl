// Agent pn in project ContractNetProtocol.mas2j
//
// This agent never answers a Call For Proposal (CFP)
//

// the name of the agent playing initiator in the CNP
plays(initiator,c).

// send a message to initiator introducing myself as a participant
+plays(initiator,I)
   :  .myName(Me)
   <- .send(I,tell,introduction(participant,Me)).
   
