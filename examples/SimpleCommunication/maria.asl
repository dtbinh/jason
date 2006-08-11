// Agent maria in project Communication.mas2j

// plan trigged when a tell message is received
+vl(X)[source(Ag)] : true
   <- .print("Received tell ",vl(X)," from ", Ag).
   
// plan trigged when an achieve message is received   
+!goto(X,Y)[source(Ag)] : true
   <- .print("Received achieve ",goto(X,Y)," from ", Ag).

   
