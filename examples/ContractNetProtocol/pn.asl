// the name of the agent playing initiator in the CNP
plays(initiator,c).

// send a message to initiator introducing myself as a participant
+plays(initiator,I)
   :  .my_name(Me)
   <- .send(I,tell,introduction(participant,Me)).
   
