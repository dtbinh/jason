hovis(p002)[o(c)].
unary(hovis(p001)[o(c)]).
binary(hovis(p001)[o(c)], product(p002)[o(c)]).
ternary(product(p001)[o(c)], product(p002)[o(c)], hovis(p003)[o(c)]).
binaryNestedInBinary(binary(product(p001)[o(c)], product(p002)[o(c)]), binary(product(p003)[o(c)], product(p004)[o(c)])).

+product(PID)[o(c)]
	<-
	.print(PID).


+unary(product(PID)[o(c)])
	<-
	.print(PID).
	
+binary(product(X)[o(c)], product(Y)[o(c)])
	<-
	.print(X, ", ", Y).
	
+ternary(product(X)[o(c)], product(Y)[o(c)], product(Z)[o(c)])
	<-
	.print(X, ", ", Y, ", ", Z).
	
	
+binaryNestedInBinary(binary(product(A)[o(c)], product(B)[o(c)]), binary(product(C)[o(c)], product(D)[o(c)]))
	<-
	.print(A, ", ", B, ", ", C, ", ", D).
