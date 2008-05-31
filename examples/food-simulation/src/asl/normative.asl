
// food in my position
+step(_) : food(X,Y,my_pos,A) & (pos(A,_,_) | A == -1) <- eat.

// food I see
+step(_) : food(X,Y,see,Me)  & pos(Me,_,_) & not agent(_,X,Y,_,_) <- move(X,Y).
+step(_) : food(X,Y,see,-1)  & not agent(_,X,Y,_,_)               <- move(X,Y).
+step(_) : food(X,Y,see,OAg) & agent(AgId,X,Y,_,eating) & 
           AgId \== OAg & OAg \== -1 
        <- attack(X,Y).

// food I smell
+step(_) : food(X,Y,smell,Me) & pos(Me,_,_) <- move(X,Y).
+step(_) : food(X,Y,smell,-1)               <- move(X,Y).

+step(_) <- random_move.
