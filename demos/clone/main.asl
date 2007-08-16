// Agent main in project clone.mas2j

/* Initial beliefs and rules */

test(1).

/* Initial goals */

!print.
!start.

/* Plans */

+!start 
   <- +a(40); 
      .wait(100);
      myia.clone("bob");
      .send(bob,tell,p(10)).
	  
+!print <- .wait(500); .print(hello); !print.

+p(X) <- .print(X).

