count(0).
ok.

vl(10).
vl(a).
vl(test(1)).

@p1[atomic]
+!sayHello[source(S)] : true 
   <- -count(X);
      .print("hello for ",S,": ", X);
      +count(X+1).
      
      
