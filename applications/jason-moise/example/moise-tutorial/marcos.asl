// the agent is controled by GUI

+!do(X) <- .print("doing ",X); X; gui.list_bels.
-!do(X)[code(C)] <- .print("error in ",C).

