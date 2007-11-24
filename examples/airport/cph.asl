!test_disarm. // initial goal
+!test_disarm <- disarm(Bomb). // illegal tentative action, must cause an error

// carry a Bomb to a safe place
+!carry_to_safe_place(RN,Place) : true 
   <- ?unattended_luggage(Trmnl,Gate,RN);
      !go(Trmnl,Gate);
      pick_up(Bomb);
      !go(Place);
      drop(Bomb).
     

// void plans, not implemented yet.
+!go(T,G).
+!go(P).
