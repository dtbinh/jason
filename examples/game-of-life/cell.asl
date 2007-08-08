// Agent cell in project game-of-life.mas2j

/* Plans */

// If there are 2 alive neighbors, the cell remains in the state it is in.
+alive_neighbors(2).

// If there are exactly three alive neighbors, the cell becomes alive.
+alive_neighbors(3) <- live.

// If there are less than two alive neighbors, then the cell dies.
// If there are more than three alive neighbors, the cell dies.
+alive_neighbors(X) : X < 2 | X > 3 <- die.


