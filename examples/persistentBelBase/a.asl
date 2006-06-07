demo.
+demo : not a(_) 
  <- .print("First run."); 
     +a(1); 
     .stopMAS.
     
+demo : a(X) 
  <- -a(_); +a(X+1); 
     .print("not first run, I already run ",X," times.");
     .stopMAS.

