
// food in my position
+step(_) : food(_,_,my_pos,A) & (pos(A,_,_) | A == -1 | cheater(A)) <- eat.

// food I see
+step(_) : food(X,Y,see,Me)  & pos(Me,_,_) & not agent(_,X,Y,_,_) <- move(X,Y).
+step(_) : food(X,Y,see,-1)  & not agent(_,X,Y,_,_)               <- move(X,Y).
+step(_) : food(X,Y,see,_)   & agent(AgId,X,Y,S,eating) & 
           cheater(AgId) & 
           strength(MS) & MS > S
        <- attack(X,Y).


// food I smell
+step(_) : food(X,Y,smell,Me) & pos(Me,_,_) <- move(X,Y).
+step(_) : food(X,Y,smell,-1)               <- move(X,Y).

+step(_) <- random_move.


// reputation model

+attacked(A,_) <- +cheater(A); .broadcast(tell, cheater(A)).

//+new_cheater(A,N) <- +cheater(A)[name(N)]; -new_cheater(A,N)[source(_)].

