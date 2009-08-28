/* -- plans for herding phase -- */

has_enough_boys(Boys, Cows) :- Boys > 3 & cows_by_boy(K) & Cows < Boys*K.


/* -- plans for herding groups creation -- */

+!create_herding_gr
   : not .intend(create_herding_gr)
  <- .print("ooo Creating herding group.");
     .my_name(Me);
	 
     // create the new  group
     ?group(team,TeamId);
     jmoise.create_group(herding_grp, TeamId, HG);
	 .print("ooo Herding group ",HG," created.");
	 
	 // store the list of scouter in my group
     ?play(Me,explorer,EG);
     .findall(Scouter,play(Scouter,scouter,EG),LScouters);
	 
     !change_role(herder,HG);

     // ask scouters to change role
	 .print("ooo Asking ",LScouters," to adopt the herdboy role in ",HG);
	 .send(LScouters,achieve,change_role(herdboy,HG)).
	 
	 
// If I start playing explorer in a group that has no scheme, create the scheme
+play(Me,herder,G)
   : .my_name(Me) &
     not scheme_group(_,G)
  <- jmoise.create_scheme(herd_sch, [G]);
     +group_leader(G,Me);
     .broadcast(tell, group_leader(G,Me)).
	 
// If I stop playing herder, destroy the herding groups I've created
-play(Me,herder,_)
   : .my_name(Me)
  <- for( scheme(herd_sch,S)[owner(Me)] ) {
	    .print("ooo removing scheme ",S);
	    jmoise.remove_scheme(S)
	 };
	 for( group(herding_grp,G)[owner(Me)] ) {
	    -group_leader(G,Me);
        .broadcast(untell, group_leader(G,Me));
        .print("ooo removing the group ",G);
	    jmoise.remove_group(G)
	 }.
	 
// If I stop playing herdboy (because the group was destroied by the herder),
// I should try yo create my new exploration group
/* This plan does not work with merging!
-play(Me,herdboy,_)
   : .my_name(Me)
  <- .print("ooo I do not play herdboy anymore, try to play a role in an exploration group.");
     !create_exploration_gr.
*/
  
/* -- plans for the goals of role herder -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!recruit[scheme(Sch),mission(Mission)]
  <- .print("ooo I should revise the size of the cluster and recruit!");
     !check_merge.

{ end }

+!check_merge
    : .my_name(Me) &
	  play(Me, herder, Gi) &
      //.count(play(_,_,Gi), N) & N < 3 & // only merge small group
	  current_cluster(MyC)
  <-  // for all other groups
      for(group_leader(Gj, L)  & Me < L & L \== Me & not play(L,herdboy,Gi)) { // 
	     .print("ooo Checking merging with ",Gj);
         // ask their clusters
         .send(L, askOne, current_cluster(_), current_cluster(TC));
		 .intersection(MyC,TC,I);
		 
		 if (.length(I) > 0) {
            //MyC = [pos(MyCX,MyCY)|_];
            //TC  = [pos(TCX,TCY)|_];
            //?corral_center(CorralX, CorralY);
            //jia.path_length(MyCX,MyCY,CorralX,CorralY,MCD);
            //jia.path_length(TCX,TCY,CorralX,CorralY,TCD);
            //.print("ooo check merging: my distance to corral = ",MCD," other group distance = ",TCD);
            //if (MCD <= TCD) {
               .print("ooo merging my herding group ",Gi," with ",Gj, " lead by ",L);
               .send(L, achieve, change_role(herdboy,Gi))
            //}
		 }{
		    .print("ooo no common cows, so no merging, my cows \n",MyC,"\n",TC)
		 }
	  };
	  .wait(2000). // give some time for them to adopt the roles before check merging again
+!check_merge.
	 	 
{ begin maintenance_goal("+pos(_,_,_)") }

/*+!release_boys[scheme(Sch),mission(Mission),group(Gr)]
   : .count(play(_,herdboy,Gr),N) & N > 4
  <- .print("xxx release gaucho5 from my herding group");
     .send(gaucho5,achieve,create_exploration_gr);
     .send(gaucho6,achieve,restart).
     */
+!release_boys[scheme(Sch),mission(Mission),group(Gr)]
   : .count(play(_,herdboy,Gr),N) &
     current_cluster(CAsList) &
     has_enough_boys(N-1, .length(CAsList))
     // (N > 3 | (N > 1 & current_cluster(CAsList) & .length(CAsList) < 5))
  <- .print("rrr release an agent of my herding group, I have ",N," boys for a cluster of size ",.length(CAsList));
     !release_boy([gaucho9,gaucho10,gaucho7,gaucho8,gaucho5,gaucho6,gaucho3,gaucho4],Gr);
     .wait({+pos(_,_,_)}); // wait an extra step before try to release agents again
     .wait({+pos(_,_,_)});
     .wait({+pos(_,_,_)}).
+!release_boys[scheme(Sch),mission(Mission),group(Gr)].

{ end }

+!release_boy([],_).
+!release_boy([A|_],Gr)
   : play(A,herdboy,Gr)
  <- .print("rrr releasing boy ",A);
     .send(A,achieve,quit_all_missions_roles);
     .wait(1000); // wait the agent to quit the roles
     .send(A,achieve,restart).
+!release_boy([A|O],Gr)
  <- !release_boy(O,Gr).

{ begin maintenance_goal("+pos(_,_,_)") }

+!define_formation[scheme(Sch),mission(Mission), group(Gr)]
  <- .print("ooo I should define the formation of my group ",Gr);
     jia.cluster(Cluster,CAsList);
     -+current_cluster(CAsList);
     .abolish(has_boy_beyond_fence(_,_));
     if ( .length(CAsList) > 0) {
        .findall(Boy, play(Boy, herdboy,Gr), Boys); //?my_group_players(G, herder);
        .my_name(Me);      
        GrFor = [Me|Boys]; .length(GrFor,ForSize);  // TODO: fix bug to use funcion
        jia.herd_position(ForSize,Cluster,L);
        //.reverse(L,RL); // use the reversed list so to priorise the border positions
        .print("ooo Formation is ",L, " for agents ",GrFor," in cluster ", Cluster);
        !alloc_all(GrFor,L);
        .wait({+pos(_,_,_)}); // wait 3 cycles to redefine formation
        .wait({+pos(_,_,_)}); 
        .wait({+pos(_,_,_)})
     }{
        .print("ooo No cluster to define the formation ********!")
     }.

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
	 if (boy_beyond_fence(X,Y,FX,FY)) {
        +has_boy_beyond_fence(FX,FY)
     };
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


/* -- Open Corral -- */

{ begin maintenance_goal("+pos(_,_,_)") }

+!start_open_corral[scheme(Sch),mission(Mission),group(Gr)]
   : switch(X,Y) & jia.is_corral_switch(X,Y) & // get the switch of our corral
     .print("yyy init test open corral for switch ",X,",",Y) & 
     not (scheme(open_corral,SchId) & scheme_group(SchId, _) ) & // there is no scheme to open
     pos(MeX, MeY, _) &  jia.path_length(MeX,MeY,X,Y,Dist) & 
     .print("yyy my distance from corral switch is ",Dist) & Dist < 15 & // if I am near
     .findall(Boy, play(Boy, herdboy,Gr), Cand) &
     .print("yyy candidates for porter1 in group ",Gr, " are ",Cand) & Cand \== []
  <- .print("ooo yyy I should start an open corral scheme for group ",Gr);
     !find_closest(Cand,pos(X,Y),HA);
     .print("yyy near corral is ",HA);
     jmoise.create_scheme(open_corral, [Gr], SchId);
     ?ally_pos(HA,AX,AY);
     jia.switch_places(X,Y,AX,AY,PX,PY,_,_);
     jmoise.set_goal_arg(SchId,goto_switch1,"X",PX);
     jmoise.set_goal_arg(SchId,goto_switch1,"Y",PY);
     .send(HA, achieve, change_role(gatekeeper1, Gr)).

+!start_open_corral[scheme(Sch),mission(Mission),group(Gr)].

{ end }


{ begin maintenance_goal("+pos(_,_,_)") }

+!end_open_corral[scheme(Sch),mission(Mission),group(Gr)]
   : // if the leader is too far, remove the sch
     group(_,Gr)[owner(O)] & 
     ally_pos(O,OX,OY) &
     pos(MeX, MeY, _) &
     jia.path_length(MeX,MeY,OX,OY,Dist) & 
     .print("yyy vvv my distance to leader is ",Dist) & 
     Dist > 20 & 
     not (ally_pos(_,AlX,AlY) & jia.corral(AlX,AlY) & .print("yyy vvv but there is an agent in the corral")) // no ally in corral
  <- !!change_role(herdboy,Gr);
     jmoise.remove_scheme(Sch).

+!end_open_corral[scheme(Sch),mission(Mission),group(Gr)]
   : // if I am too far from switch
     pos(MeX, MeY, _) &
     goal_state(Sch,goto_switch1(SX,SY),_) & 
     jia.path_length(MeX,MeY,SX,SY,Dist) & 
     .print("yyy vvv my distance to switch is ",Dist) & 
     Dist > 20
  <- !!change_role(herdboy,Gr);
     jmoise.remove_scheme(Sch).

+!end_open_corral[scheme(Sch),mission(Mission),group(Gr)].

{ end }



//+!goto_switch(X,Y)[scheme(Sch)]
//  <- !goto_switch(X,Y,Sch,goto_switch). // the basic implementation of goto switch (pass_fence.asl)
     	
     	
/* -- Change to exploring -- */
     	
{ begin maintenance_goal("+pos(_,_,_)") }

+!change_to_exploring[scheme(Sch),mission(Mission),group(Gr)]
   : not cow(_,_,_)
  <- .print("ooo I see no cow anymore");
     // wait two cycles to decide to change the formation (due to fault perception we may not see the cows)
	 .wait({+pos(_,_,_)});
	 .wait({+pos(_,_,_)});
	 if (not cow(_,_,_)) {
	    .findall(P, play(P,herdboy,Gr), ListBoys);
        !!create_exploration_gr;
		// ask helpers in my group to change the role (or create a exploration group if we merged)
		.send(ListBoys, achieve, create_exploration_gr)
	 }.

+!change_to_exploring[scheme(Sch),mission(Mission),group(Gr)].

{ end }


/* -- plans for the goals of all roles (herder and herdboy) -- */

// This goal behaviour is set by the message "tell target" of the leader of the group
+!be_in_formation[scheme(Sch),mission(Mission)]
  <- .print("ooo I should be in formation!");
     .suspend.

