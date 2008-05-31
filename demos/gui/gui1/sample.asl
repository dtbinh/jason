/* Initial goals */

!start.
!print.

/* Plans */


// the internal action gui.yes_no succeed if the user click Yes and fails otherwise
//
// note that this IA blocks only the intention using it (the !start) and not all the
// intentions (the agent continues to print "." while the interface is being shown

+!start <- gui.yes_no("Is it Ok?"); .print(ok).
-!start <- .print(nok).

+!print <- .wait(500); .print("."); !!print.


