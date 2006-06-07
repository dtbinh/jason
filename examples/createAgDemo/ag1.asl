demo.
+demo : true 
   <- .print("Creating agent");
      .createAgent(bob, "bob.asl");
      .send(bob, achieve, a);
      .wait(100);
      .print("Killing agent bob!");
      .killAgent(bob);
      !end(10000).
      
+!end(T) : T <= 0 <- .stopMAS.
+!end(T) : true   <- .print("The MAS will stop in ",T/1000," seconds!"); .wait(2000); !end(T-2000).
      
      