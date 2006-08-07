// robot in project House.mas2j

available(beer,fridge).

+!has(owner,beer)
   :  not has(owner,beer) & available(beer,fridge)
   <- !at(robot,fridge);
      open(fridge);
      get(beer);
      close(fridge);
      !at(robot,owner);
      hand_in(beer);
      ?has(owner,beer).

+!has(owner,beer)
   :  not has(owner,beer) & not available(beer,fridge)
   <- +wants(owner,beer); // to remember that owner want a beer 
      .send(supermarket, tell, order(beer,5)).

       
+!at(robot,P) : at(robot,P) <- true.
+!at(robot,P) : not at(robot,P)
  <- move_towards(P);
     !at(robot,P).

+stock(beer,1) : true 
  <- -available(beer,fridge).

+delivered(beer,N)
  : true
  <- +available(beer,fridge);
     -delivered(beer,N);
     !checkOwnerOK.

+!checkOwnerOK : wants(owner,beer)
  <- -wants(owner,beer);
     !has(owner,beer).
+!checkOwnerOK : not wants(owner,beer)
  <- true.
     
//+?time(T) : true
//  <-  time.check(T).

// changing KQML default plan
+!received(S, ask, time(Now), M) : true
  <- //?time(Now);
     time.check(Now);
     .send(S, tell, time(Now), M).

