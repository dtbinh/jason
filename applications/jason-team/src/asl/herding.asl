/* -- plans for herding phase -- */

/* -- initial beliefs -- */

// missions I can commit to
desired_mission(herd_sch, herd) :- desired_role(herding_grp, herder).
desired_mission(herd_sch, help_herder) :- desired_role(herding_grp, herdboy).


/* -- plans for the goals of role herder -- */

+!recruit[scheme(Sch)]
  <- .print("ooo I should revise the size of the cluster and recruit!");
     // TODO
     .wait("+pos(_,_,_)"); // wait next cycle
     !!recruit[scheme(Sch)].


+!define_formation[scheme(Sch)]
  <- .print("ooo I should define the formation of my group!");
     ?my_group(G, herder);
     .length(G,NumP);
     jia.herd_position(NumP,L);
     .print("ooo formation is ",L);
	 !alloc_all(G,L);
     .wait("+pos(_,_,_)"); // wait next cycle
     !!define_formation[scheme(Sch)].

// get the list G of participants of the group where I play R
+?my_group(G,R) 
  <- .my_name(Me);
     play(Me,R,Gid);
     .findall(P, play(P,_,Gid), G);
     +my_group(G,R).
// TODO, IMPORTANT: Quando tiver mudanca no grupo, apagar essa crenca
// pra deixar esse plano rodar de novo.

+!alloc_all([],LA).
+!alloc_all([HA|TA],LA)
  <- !find_closest(HA,LA,pos(X,Y),NLA);
     .send(HA,tell,target(X,Y));
	 -+alloc_target(HA,Alloc);
     !alloc_all(TA,NLA).

+!find_closest(Ag, List, Alloc, Rest)
  <- ?alloc_target(Ag,pos(X,Y));
     !closest(List,[],Sorted,pos(X,Y),9999);
	 Sorted = [Alloc|Rest];
	 .print("FIND CLOSEST: ",Sorted).

+!closest([],S,S,P,D).
+!closest([pos(XH,YH)|T],Aux,S,pos(XP,YP),LD)
  :  jia.dist(XH,YH,XP,YP,D) & D < LD
  <- !closest(T,[pos(XH,YH)|Aux],S,pos(XP,YP),D).
+!closest([pos(XH,YH)|T],Aux,S,pos(XP,YP),LD)
  <- .concat(Aux,[pos(XH,YH)],Aux2);
     !closest(T,Aux2,S,pos(XP,YP),LD).

+?alloc_target(Ag,X,Y) <- .send(Ag, askOne, pos(X,Y,_), pos(X,Y,_)).

/* -- plans for the goals of all roles (herder and herdboy) -- */

+!be_in_formation[scheme(Sch)]
  <- .print("ooo I should be in formation!");
     // TODO
     .wait("+pos(_,_,_)"); // wait next cycle
     !!be_in_formation[scheme(Sch)].

