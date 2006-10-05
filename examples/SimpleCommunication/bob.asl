// Agent bob in project Communication.mas2j

!start.

+!start : true 
   <- .print("Sending tell vl(10)");
      .send(maria, tell, vl(10));
      
      .print("Sending achieve goto(10,2)");
      .send(maria, achieve, goto(10,2));
      
      .print("Sending ask ");
      .send(maria, askOne, vl(X), vl(X));
      .print("Answer from ask is ", X);

      .print("Sending ask for something Maria does not know ");
      .send(maria, askOne, t1(X2), Ans);
      .print("Answer from ask is ", Ans);
      
      .print("Sending ask for something Maria does not know, but can handle by +? ");
      .send(maria, askOne, t2(X3), Ans);
      .print("Answer from ask is ", Ans);
      
      .print("Sending askAll values");
      .send(maria, askAll, value(Y,vl(Y)), List);
      .print("Answer from askAll is ", List);

      .print("Sending ask full name");
      .send(maria, askOne, fullname, FN);
      .print("Full name is ",FN);
      
      .print("Asking Maria to achieve 'hello'");
      .send(maria,achieve, hello(bob));
      .wait(2000);
      
      .print("Asking Maria to unachieve 'hello'");
      .send(maria,unachieve, hello(bob)).

