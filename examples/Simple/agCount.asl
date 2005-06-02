count(0).
ok.

vl(a).
vl(test(1)).
vl(10).

+!sayHello : true 
   <- ?count(X);
      .print(hello, X);
      .plus(X,1,C);
      -count(X);+count(C).
