// the agent is controled by GUI

+!do(X) <- .print("doing ",X); X; gui.list_bels.

+!wtitle[scheme(S)] <- jmoise.set_goal_state(S,wtitle,satisfied).
+!wabs[scheme(S)] <- jmoise.set_goal_state(S,wabs,satisfied).
+!wsectitles[scheme(S)] <- jmoise.set_goal_state(S,wsectitles,satisfied).
