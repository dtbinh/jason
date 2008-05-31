// rule 1: food in my position
+step(_) : food(X,Y,my_pos,_) <- eat.

// rule 2: food I see
+step(_) : food(X,Y,see,_) & not agent(_,X,Y,_,_) <- move(X,Y).

// rule 3: food I see
+step(_) : food(X,Y,see,_) & agent(_,X,Y,_,eating) <- attack(X,Y).

// rule 4: food I smell
+step(_) : food(X,Y,smell,_) <- move(X,Y). 

// rule 5
+step(_) <- random_move.
