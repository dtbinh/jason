// Agent bob in project Communication.mas2j

!start.

+!start : true 
   <- .print("Sending tell vl(10)");
      .send(maria, tell, vl(10));
      .print("Sending achieve goto(10,2)");
      .send(maria, achieve, goto(10,2)).

