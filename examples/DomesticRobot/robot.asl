// robot in project House.mas2j

/* Initial beliefs and rules */

// initially, I believe that there are some beer in the fridge
available(beer,fridge).

// my owner should not consume more than 10 beers a day :-)
limit(beer,10). 

too_much(B) :- 
    .date(YY,MM,DD) &
    .findall(B,consumed(YY,MM,DD,_,_,_,B),LB) &
    .length(LB,SizeLB) &
    limit(B,Limit) &
    SizeLB > Limit.

    
/* Plans */
    
+!has(owner,beer)
   :  available(beer,fridge) & not too_much(beer)
   <- !at(robot,fridge);
      open(fridge);
      get(beer);
      close(fridge);
      !at(robot,owner);
      hand_in(beer);
      ?has(owner,beer);
      // remember that one beer is consumed
      .date(YY,MM,DD); .time(HH,NN,SS);
      +consumed(YY,MM,DD,HH,NN,SS,beer).

+!has(owner,beer)
   :  not available(beer,fridge)
   <- .send(supermarket, achieve, order(beer,5)).

+!has(owner,beer)
   :  too_much(beer) & limit(beer,L)    
   <- .concat("The Department of Health does not allow me to give you more than ", L, M1);
      .concat(M1," beers a day! I am very sorry about that!",M);
      .send(owner,tell,msg(M)).    

// when the supermarket finished the order, try the 'has' goal again   
+delivered(beer,Qtd,OrderId)[source(supermarket)]
  :  true
  <- +available(beer,fridge);
     !has(owner,beer). 
   
   
-!has(_,_)
   :  true
   <- .currentIntention(I); 
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).
   
+!at(robot,P) : at(robot,P) <- true.
+!at(robot,P) : not at(robot,P)
  <- move_towards(P);
     !at(robot,P).

// when the fridge is openned, the beer stock is perceived
// and thus the available belief is updated
+stock(beer,0) 
   :  available(beer,fridge)
   <- -available(beer,fridge).
+stock(beer,N) 
   :  N > 0 & not available(beer,fridge)
   <- -+available(beer,fridge).

+?time(T) : true
  <-  time.check(T).

// changing KQML default plan
+!received(S, ask, time(Now), M) : true
  <- ?time(Now);
     //time.check(Now);
     .send(S, tell, time(Now), M).

