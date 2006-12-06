!start.
+!start : true <- myp.listPlans; !d(1); !d(2).

{ begin ld }
+!d(X) <- act.
+!d(X) <- act.
{ end }

/*
The above plans will be changed by the directive to:
  +!d(X) <- .print("Entering ",d(X)); .print(d1); .print("Leaving ",d(X)).
  +!d(X) <- .print("Entering ",d(X)); .print(d2); .print("Leaving ",d(X)).
*/

