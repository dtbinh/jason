demo.
+demo : not a(_) 
  <- .print("First run."); 
     +a(1);
     .wait(1000);
     .stopMAS.
     
+demo : a(X) 
  <- -+a(X+1); 
     .print("Not first run, I already run ",X," times.");
     .wait(1000);
     .stopMAS.

