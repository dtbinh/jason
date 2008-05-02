/* -- plans for herding phase -- */

/* -- initial beliefs -- */


/* -- plans for the goals of role herder -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!recruit[scheme(Sch)]
  <- .print("ooo I should revise the size of the cluster and recruit!").

{ end }

{ begin maintenance_goal("+pos(_,_,_)") }

+!define_formation[scheme(Sch)]
  <- .print("ooo I should define the formation of my group!");
     ?my_group_players(G, herder);
     jia.herd_position(.length(G),L);
     .print("ooo formation is ",L);
	 !alloc_all(G,L).
	 
{ end }

+!alloc_all([],LA).
+!alloc_all([HA|TA],LA)
  <- !find_closest(HA,LA,pos(X,Y),NLA);
     .send(HA,tell,target(X,Y));
	 -+alloc_target(HA,Alloc);
     !alloc_all(TA,NLA).

+!find_closest(Ag, List, Alloc, Rest) // rule
  <- ?ally_pos(Ag,XY);
     !closest(List,[],Sorted,pos(X,Y),9999);
	 Sorted = [Alloc|Rest];
	 .print("FIND CLOSEST: ",Sorted).
// TODO: use min
+!closest([],S,S,P,D).
+!closest([pos(XH,YH)|T],Aux,S,pos(XP,YP),LD)
  :  jia.path_length(XH,YH,XP,YP,D) & D < LD // usar A*
  <- !closest(T,[pos(XH,YH)|Aux],S,pos(XP,YP),D).
+!closest([pos(XH,YH)|T],Aux,S,pos(XP,YP),LD)
  <- .concat(Aux,[pos(XH,YH)],Aux2);
     !closest(T,Aux2,S,pos(XP,YP),LD).


/* -- plans for the goals of all roles (herder and herdboy) -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!be_in_formation[scheme(Sch)]
  <- .print("ooo I should be in formation!").
     // TODO
	 
{ end }
