// Agent bob in project Communication.mas2j

!start.

+!start : true 
   <- .println("Sending tell vl(10)");
      .send(maria, tell, vl(10));
      
      .println("Sending achieve goto(10,2)");
      .send(maria, achieve, goto(10,2));
      
      .println("Sending ask ");
      .send(maria, askOne, vl(X), vl(X));
      .println("Answer from ask is ", X).

