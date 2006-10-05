// Agent bob in project Communication.mas2j

!start.

+!start : true 
   <- .println("Sending tell vl(10)");
      .send(maria, tell, vl(10));
      
      .println("Sending achieve goto(10,2)");
      .send(maria, achieve, goto(10,2));
      
      .println("Sending ask ");
      .send(maria, askOne, vl(X), vl(X));
      .println("Answer from ask is ", X);

      .println("Sending ask for something Maria does not know ");
      .send(maria, askOne, t1(X2), Ans);
      .println("Answer from ask is ", Ans);
      
      .println("Sending ask for something Maria does not know, but can handle by +? ");
      .send(maria, askOne, t2(X3), Ans);
      .println("Answer from ask is ", Ans);
      
      .println("Sending askAll values");
      .send(maria, askAll, value(Y,vl(Y)), List);
      .println("Answer from askAll is ", List);

      .println("Sending ask full name");
      .send(maria, askOne, fullname, FN);
      .println("Full name is ",FN);
      
      .println("Asking Maria to achieve 'hello'");
      .send(maria,achieve, hello(bob));
      .wait(2000);
      
      .println("Asking Maria to unachieve 'hello'");
      .send(maria,unachieve, hello(bob)).

