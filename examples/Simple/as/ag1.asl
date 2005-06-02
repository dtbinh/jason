p(a).
p(b).
q(a,b).
r(b).
t(b,c).

@planLabel
+!g(Z) : not p(c) & not q(b,b) & r(X)
      <- ?t(Z,Y);
         .print("Ok - i received the achieve with Z=", Z);
         b(Y).
