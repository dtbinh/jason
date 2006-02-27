demo.
+demo : true 
   <- .print("Creating agent");
      .createAgent(bob, "bob.asl");
      .send(bob, achieve, a);
      .wait(100);
      .print("Killing agent bob!");
      .killAgent(bob);
      .print("The MAS will stop in 10 seconds!");
      .wait(10000);
      stopMAS. // the environment will stop the MAS
      