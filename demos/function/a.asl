// Agent a in project function.mas2j


/* define a new function in Java */
{ register_function("myf.sin") } // Register an user defined function
                                 // the code of this function is in the
								 // class myf.sin (see sin.java for more
								 // info)

/* define a new function in AgentSpeak */

{ register_function("sum",2) }   // Register an user defined function
                                 // the code of this function is in the
								 // rule sum below, the last argument
								 // is the return of the function
sum(X,Y,S) :- S = X + Y.								 
								 
{ register_function("limit", 0) } // example of constant function
limit(10).

								 
/* Initial beliefs (used to show the use of .count) */
b(10).
b(20).
t(x).

/* Initial goals */

!show_predef_funtion.
!show_userdef_funtion.

/* Plans */

+!show_predef_funtion  
   <- X = math.max(4, math.abs(-10)); 
      .print("Max=",X);
	  .print("Max=",math.max(4, math.abs(-10)));
	  .print("Number of b/1 beliefs=", .count(b(_))).

+!show_userdef_funtion
   <- .print("Sin of 90   =", myf.sin(90));
      .print("limit       =", limit);
      .print("5+(2+limit) =", sum(5,sum(2,limit))).
   
