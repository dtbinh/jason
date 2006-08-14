// Agent bob in project Communication.mas2j

!start.

+!start : true 
   <- .print("Sending tell vl(10)");
      .send(maria, tell, vl(10));
      
      .print("Sending achieve goto(10,2)");
      .send(maria, achieve, goto(10,2));
      
      .print("Sending ask ");
      .send(maria, ask, vl(X), vl(X));
      .print("Answer from ask is ", X);
      
      .print("Sending askAll values");
      .send(maria, askAll, value(Y,vl(Y)), List);
      .print("Answer from askAll is ", List).
      

