/* -- plans for herding phase -- */

/* -- initial beliefs -- */

/* -- plans for herding groups creation -- */


+!create_herding_gr
   : not .intend(create_herding_gr)
  <- .print("ooo Creating herding group.");
     .my_name(Me);
	 
     // create the new  group
     ?group(team,TeamId);
     jmoise.create_group(herding_grp, TeamId, HG);
	 .print("ooo Group ",HG," created.");
	 
	 // store the list of scouter in my group
     ?play(Me,explorer,EG);
     .findall(Scouter,play(Scouter,scouter,EG),LScouters);
	 
     !change_role(herder,HG);

     // ask scouters to change role
	 .print("ooo Asking ",LScouters," to adopt the herdboy role in ",HG);
	 .send(LScouters,achieve,change_role(herdboy,HG)).
	 
	 
// If if start playing explorer in a group that has no scheme, create the scheme
+play(Me,herder,G)
   : .my_name(Me) &
     not scheme_group(_,G)
  <- jmoise.create_scheme(herd_sch, [G]);
     +group_leader(G,Me);
     .broadcast(tell, group_leader(G,Me)).
	 
// If I stop playing herder, destroy the herding groups I've created
-play(Me,herder,_)
   : .my_name(Me)
  <- .wait(4000);
     for( group(herding_grp,G)[owner(Me)] ) {
	    -group_leader(G,Me);
        .broadcast(untell, group_leader(G,Me));
	    jmoise.remove_group(G);
		.wait(4000)
	 }.

	 
/* -- plans for the goals of role herder -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!recruit[scheme(Sch),mission(Mission)]
  <- .print("ooo I should revise the size of the cluster and recruit!");
     !check_merge.

{ end }

+!check_merge
    : .my_name(Me) &
	  play(Me, herder, Gi) &
	  current_cluster(MyC)
  <-  // for all other groups
      for( group_leader(Gj, L) & L \== Me & Me < L & not play(L,herdboy,Gi)) {
	     .print("ooo Checking merging with ",Gj);
         // ask their cluster
         .send(L, askOne, current_cluster(_), current_cluster(TC));
		 .intersection(MyC,TC,I);
		 
		 if (.length(I) > 0) {
            .print("ooo Merging my herding group ",Gi," with ",Gj, " lead by ",L);
            .send(L, achieve, change_role(herdboy,Gi))
		 }
	  };
	  .wait(2000). // give some time for them to adopt the roles before check merging again
+!check_merge.
	 	 

{ begin maintenance_goal("+pos(_,_,_)") }

+!define_formation[scheme(Sch),mission(Mission)]
  <- .print("ooo I should define the formation of my group!");
     ?my_group_players(G, herder);
     jia.cluster(Cluster,CAsList);
     -+current_cluster(CAsList);
     jia.herd_position(.length(G),Cluster,L);
     .print("ooo Formation is ",L, " for agents ",G," in cluster ", Cluster);
     !alloc_all(G,L).
	 
{ end }

// version "near agent of each position 
+!alloc_all([],[]).
+!alloc_all([],L) <- .print("ooo there is no agent for the formation ",L).
+!alloc_all(G,[]) <- .print("ooo there is no place in the formation for ",G).
+!alloc_all(Agents,[pos(X,Y)|TLoc])
  <- !find_closest(Agents,pos(X,Y),HA);
     .print("ooo Allocating position ",pos(X,Y)," to agent ",HA);
     .send(HA,tell,target(X,Y));
	 .delete(HA,Agents,TAg);
     !alloc_all(TAg,TLoc).

+!find_closest(Agents, pos(FX,FY), NearAg) // find the agent near to pos(X,Y)
  <- .my_name(Me);
     .findall(d(D,Ag),
              .member(Ag,Agents) & (ally_pos(Ag,AgX,AgY) | Ag == Me & pos(AgX,AgY,_)) & jia.path_length(FX,FY,AgX,AgY,D),
			  Distances);
	 //.print("Distances for ",pos(FX,FY)," are ",Distances);
	 .min(Distances,d(_,NearAg)).
	 
/* 
// version "near  place of the agent"
+!alloc_all([],[]).
+!alloc_all(G,[]) <- .print("ooo there is no place in the formation for ",G).
+!alloc_all([HA|TA],LA)
  <- !find_closest(HA,LA,pos(X,Y),NLA);
     .print("ooo Alocating position ",pos(X,Y)," to agent ",HA);
     //.send(HA,untell,target(_,_));
     .send(HA,tell,target(X,Y));
	 //-+alloc_target(HA,Alloc);
     !alloc_all(TA,NLA).

+!find_closest(Ag, ListPos, MinDist, Rest) // find the location in ListPos nearest to agent Ag
  <- ?ally_pos(Ag,X,Y);
     //.print("ooo try to alloc ",Ag," in ",X,Y," with ",ListPos);
     ?calc_distances(ListPos,Distances,pos(X,Y));
	 .print("Distances for ",ag_pos(Ag,X,Y)," are ",Distances);
	 .min(Distances,d(_,MinDist));
	 .delete(MinDist,ListPos,Rest).
	 //!closest(ListPos,[],[MinDist|Rest],pos(X,Y),9999).

calc_distances([],[],_) :- true.
calc_distances([pos(Fx,Fy)|TP], [d(D,pos(Fx,Fy))|TD], pos(AgX,AgY))
  :- jia.path_length(Fx,Fy,AgX,AgY,D) & calc_distances(TP, TD, pos(AgX,AgY)).
*/

/*
+!closest([],S,S,_,_).
+!closest([pos(XH,YH)|T],Aux,S,pos(XP,YP),LD)
  :  jia.path_length(XH,YH,XP,YP,D) & D < LD 
  <- !closest(T,[pos(XH,YH)|Aux],S,pos(XP,YP),D).
+!closest([pos(XH,YH)|T],Aux,S,pos(XP,YP),LD)
  <- .concat(Aux,[pos(XH,YH)],Aux2);
     !closest(T,Aux2,S,pos(XP,YP),LD).
*/


/* -- plans for the goals of all roles (herder and herdboy) -- */


// This goal behaviour is set by the message "tell target" of the leader of the group
+!be_in_formation[scheme(Sch),mission(Mission)]
  <- .print("ooo I should be in formation!");
     .suspend.

