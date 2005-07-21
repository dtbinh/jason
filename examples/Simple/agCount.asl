count(0).
ok.

vl(a).
vl(test(1)).
vl(10).

@p1[atomic]
+!sayHello : true 
   <- ?count(X);
      .print(hello, X);
      C = X+1;
      -count(X);+count(C).
