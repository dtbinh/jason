// a in project testBelBase.mas2j
demo.
+demo : not a(_) 
  <- .print("First run."); 
     +a(1); 
     .stopMAS.
     
+demo : a(X) 
  <- -a(_); +a(X+1); 
     .print("not first run, a value is ",X);
     .stopMAS.

