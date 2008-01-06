// Agent a in project function.mas2j


{ register_function("myf.sin") } // Register an user defined function
                                 // the code of this function is in the
								 // class myf.sin (see sin.java for more
								 // info)
								 
/* Initial goals */

!show_predef_funtion.
!show_userdef_funtion.

/* Plans */

+!show_predef_funtion  
   <- X = math.max(4, math.abs(-10)); 
      .print("Max=",X);
	  .print("Max=",math.max(4, math.abs(-10))).

+!show_userdef_funtion
   <- .print("Sin of 90=", myf.sin(90)).
   
