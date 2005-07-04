p(a).
p(b).
q(a,b).
r(b).
t(b,c).

@label(example1)[annot1, annot2]
+s(Y)  : p(X) & q(X,Y)
      <- a(Y);
         !g(Y);
         ?r(Z);
         a(Z);
         +u(a);
         -u(a);
         .print("finish s(Y)").

+!g(Z) : not p(c) & not q(b,b) & r(X)
      <- .send(agCount, askAll, value(A,vl(A)), ListVl); .print(ListVl);
         
         .getRelevantPlans("+!te1(X)", L); 
         .send(agCount, tellHow, L); 
         .send(agCount, achieve, te1(10)); 
         .send(ag1,achieve,g(b));
         .send(agCount, achieve, sayHello);
         //.send(agCount, askIf, ok, true); .print("Answer for askIf is true");
         .send(agCount, askOne, count(Count), R);
         .print("Answer for askOne is ", R);
         b(Y).

// a plan to be send to others
@alabel1 +!te1(X) : ok   <- .print(" ** ",ok(X)).
@alabel2 +!te1(X) : true <- .print(" ** ",nok(X)).
