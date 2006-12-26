plays(initiator,c). 
+plays(initiator,I)
   :  .my_name(Me)
   <- .send(I,tell,introduction(participant,Me)).

// plan to answer a CFP
+cfp(CNPId,Object)[source(A)] 
   :   plays(initiator,A)
   <- .send(A,tell,refuse(CNPId)).

