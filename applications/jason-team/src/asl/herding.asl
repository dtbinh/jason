/* -- plans for herding phase -- */

/* -- initial beliefs -- */

// missions I can commit to
desired_mission(exploring, mherder).
desired_mission(exploring, mherdboy).


/* -- plans for the goals of role herder -- */

+!recruit[scheme(Sch)]
  <- .print("ooo I should revise the size of the cluster and recruit!");
     // TODO
     .wait("+pos(_,_,_)"); // wait next cycle
     !!recruit[scheme(Sch)].


+!define_formation[scheme(Sch)]
  <- .print("ooo I should define the formation of my group!");
     jia.herd_position(2,L); // formation in two (TODO: get the number of players in the group or scheme)
     .print("ooo formation is ",L);
     // TODO: allocate and share the formation
     .wait("+pos(_,_,_)"); // wait next cycle
     !!define_formation[scheme(Sch)].


/* -- plans for the goals of all roles (herder and herdboy) -- */

+!be_in_formation[scheme(Sch)]
  <- .print("ooo I should be in formation!");
     // TODO
     .wait("+pos(_,_,_)"); // wait next cycle
     !!be_in_formation[scheme(Sch)].

