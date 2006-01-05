p(a).
p(b).
q(a,b).
r(b).
t(b,c).
c(0).

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
      <- .print("begining g, testing performatives ----");
      
	 .send(agCount, askAll, value(A,vl(A)), ListVl); 
	 .print("should be [10,a,test(1)] = ",ListVl);
         
         .send(agCount, achieve, sayHello);

         .send(agCount, askOne, count(Count), Rc);
         .print("---- Answer for askOne count(C) is ", Rc);

         .send(agCount, askIf, ok, true); 
	 .print("Answer for askIf is true");

         .getRelevantPlans("+!te1(X)", L); .print("Relevant plans for +te1(X) ",L);
         .send(agCount, tellHow, L);
         ?c(C); -c(C); +c(C+1);
         .send(agCount, achieve, te1(C)); 

         .send(ag1,achieve,g(b));
         b(b).

// a plan to be send to others
@alabel1 +!te1(X) : ok & X > 3  <- .print(" ** X > 3 ",ok(X)).
@alabel2 +!te1(X) : ok & X < 3  <- .print(" ** X < 3 ",ok(X)).
@alabel3 +!te1(X) : true <- .print(" ** ",nok(X)).
