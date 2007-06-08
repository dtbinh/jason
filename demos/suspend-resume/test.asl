// Agent test in project suspend.mas2j

/* Initial goals */

!dots.
!control.

/* Plans */

// loop printing dots
+!dots 
  <- .print(".");
	 !!dots.

// another loop that resumes and suspend the start
+!control
  <- .wait(30);
     bdi.suspend(dots); // suspend the intention with goal dots
	 .println;
     .wait(200);
	 bdi.resume(dots);  // resume it
	 !!control.
