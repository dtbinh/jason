// owner in project House.mas2j

!check_bored. // initial goal: verify whether I am getting bored

+has(owner,beer) : true 
   <- !drink(beer).
-has(owner,beer) : true 
   <- .send(robot, achieve, has(owner,beer)).

+!drink(beer) : has(owner,beer)
   <- sip(beer);
     !drink(beer).
+!drink(beer) : not has(owner,beer)
   <- true.
 
+!check_bored 
   :  true
   <- .random(X); .wait(X*5000+2000);  // i get bored at random times
      .send(robot, ask, time(Now), R); // when bored, ask the robot about the time
      .print(R);
      !!check_bored.

