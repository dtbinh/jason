!test. // initial goal
+!test : true <- .print("start"); !g1(X); .print("End, X=",X).

+!g1(X) : true <- .print(inig1); !g2(X); .print(endg1).
+!g2(X) : true <- .print(inig2); !g3(X); .print(endg2).
+!g3(X) : true <- .print(inig3); !g4(X); .print(endg3).
+!g4(X) : true <- .print(inig4); !g5(X); .print(endg4).
+!g5(X) : true <- .fail.
-!g3(failure) : true 
  <- .current_intention(I);
     .print("In failure handling plan, current intention is: ",I).

