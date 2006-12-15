// Agent bob in project Communication.mas2j

!start.

+!start : true 
   <- .println("Sending tell vl(10)");
      .send(maria, tell, vl(10));
      
      .println("Sending achieve goto(10,2)");
      .send(maria, achieve, goto(10,2));
      
      .println("Sending synchronous ask ");
      .send(maria, askOne, vl(X), vl(X));
      .println("Answer from ask is ", X);

      .println("Sending assynchronous ask ");
      .send(maria, askOne, vl(X4)); // assync ask has no fourth argument
	  // the answer is received as an event +vl(X)

      .println("Sending ask for something Maria does not know ");
      .send(maria, askOne, t1(X2), Ans1);
      .println("Answer from ask is ", Ans1);
      
      .println("Sending ask for something Maria does not know, but can handle by +? ");
      .send(maria, askOne, t2(X3), Ans2);
      .println("Answer from ask is ", Ans2);
      
      .println("Sending askAll values");
      .send(maria, askAll, vl(Y), List);
      .println("Answer from askAll is ", List);

      .println("Sending ask full name");
      .send(maria, askOne, fullname, FN);
      .println("Full name is ",FN);
      
      .println("Asking Maria to achieve 'hello'");
      .send(maria,achieve, hello(bob));
      .wait(2000);
      
      .println("Asking Maria to unachieve 'hello'");
      .send(maria,unachieve, hello(bob)).

+vl(X)[source(A)]
   <- .print("Received value ",X," from ",A).
   
