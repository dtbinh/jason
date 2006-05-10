// owner in project House.mas2j

+has(owner,beer) : true <- !drink(beer).
-has(owner,beer) : true <- .send(robot, achieve, has(owner,beer)).

+!drink(beer) : has(owner,beer)
  <- sip(beer);
     !drink(beer).
+!drink(beer) : not has(owner,beer)
  <- true.

+bored : true
  <- .send(robot, askOne, time(Now), R);
     .print(R).

