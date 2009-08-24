/*
   -- plans for passing a fence 
*/

need_cross_fence(FX,FY) :- 
    target(TX,TY) & pos(MX, MY, _) & 
    jia.has_object_in_path(MX, MY, TX, TY, closed_fence, FX, FY, Dist) &
    .print("fff I have a fence in my path at ",Dist," steps") &
    Dist < 10.

all_passed([]).
all_passed([Ag|Others]) :-
   ally_pos(Ag,AX,AY) &
   //jia.path_length(AX,AY,SX,SY,_,fences) &
   goal_state(_,pass_fence(FX,FY,_,Direction),_) &
   ( is_horizontal(FX,FY)  & .print("fff ",FX,FY," is horizontal") & AY * Direction > (FY * Direction) | 
     is_vertical(FX,FY)    & .print("fff ",FX,FY," is vertical")   & AX * Direction > (FX * Direction)) &
   .print("fff ",Ag," passed, it is at ",AX,",",AY," should pass fence ",FX,",",FY,", direction is ",Direction) &
   all_passed(Others).

is_horizontal(FX,FY) :- fence(FX+1,FY,_) | fence(FX-1,FY,_).
is_vertical(FX,FY)   :- fence(FX,FY+1,_) | fence(FX,FY-1,_).

{ begin maintenance_goal("+pos(_,_,_)") }

+!pass_fence[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : need_cross_fence(FX, FY) &
     jia.fence_switch(FX, FY, SX, SY)
  <- jmoise.create_scheme(pass_fence, SchId);
     .print("fff Created pass fence scheme ", SchId); 

     // set ID of fence based on switch
     jmoise.set_goal_arg(SchId,pass_fence,"X",SX); 
     jmoise.set_goal_arg(SchId,pass_fence,"Y",SY);
     
     ?scheme(SchType,Sch);
     jmoise.set_goal_arg(SchId,pass_fence,"NextScheme",SchType);
     ?pos(MyX,MyY,_);
     if (is_horizontal(FX,FY)) {
         if (FY > MyY) { // fence below
            jmoise.set_goal_arg(SchId,pass_fence,"Direction",1)
         }{
            jmoise.set_goal_arg(SchId,pass_fence,"Direction",-1)
         }
     }{
         if (FX > MyX) { // fence rigth
            jmoise.set_goal_arg(SchId,pass_fence,"Direction",1)
         }{
            jmoise.set_goal_arg(SchId,pass_fence,"Direction",-1)
         }
     };
     
     jia.switch_places(SX,SY,P1X,P1Y,P2X,P2Y);
     .print("fff places for switch ",SX,",",SY," are ",P1X,",",P1Y," and ",P2X,",",P2Y);
     jmoise.set_goal_arg(SchId,goto_switch1,"X",P1X);
     jmoise.set_goal_arg(SchId,goto_switch1,"Y",P1Y);
     jmoise.set_goal_arg(SchId,goto_switch2,"X",P2X);
     jmoise.set_goal_arg(SchId,goto_switch2,"Y",P2Y);
     jmoise.add_responsible_group(SchId, Gr); // after seting parameters, so that the goal will be correctly generated
     .findall(P, play(P,_,Gr), Cand1);
     !find_closest(Cand1,pos(P1X,P1Y),HA1);
     .findall(P, play(P,_,Gr) & P \== HA1, Cand2);
     !find_closest(Cand2,pos(P2X, P2Y),HA2);
     .print("fff near 1 is ",HA1, " near 2 is ",HA2);
     .send(HA1, achieve, change_role(gatekeeper1, Gr));
     .send(HA2, achieve, change_role(gatekeeper2, Gr));
     .print("fff stopping current scheme ",Sch);
     jmoise.remove_scheme(Sch). // should be the last thing, since this goal will be dropped due to the end of the scheme
     // others shoud just pass.

+!pass_fence[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : need_cross_fence(FX, FY) &
     not jia.fence_switch(FX, FY, _, _)
  <- .print("fff I need to discover where the switch is **** not well implemented yet ****");
     -+target(FX, FY).
     
+!pass_fence[scheme(Sch),mission(Mission),group(Gr),role(Role)].
	 
{ end }	 

+!goto_switch(X,Y,Sch,Goal)
   : pos(X,Y,_) 
  <- .print("fff I am at  switch ",X,",",Y," -- ",Goal);
     jmoise.set_goal_state(Sch,Goal,satisfied).
+!goto_switch(X,Y,Sch,Goal)
   : not pos(X,Y,_) 
  <- .print("yyyy going to switch ",X,",",Y," -- ",Goal);
     -+target(X,Y);
     .wait({ +pos(X,Y,_) } );
     .print("fff reached switch ",X,",",Y," -- ",Goal);
     jmoise.set_goal_state(Sch,Goal,satisfied).
     	

+!goto_switch1(X,Y)[scheme(Sch)]
  <- !goto_switch(X,Y,Sch,goto_switch1).

+!goto_switch2(X,Y)[scheme(Sch)]
  <- !goto_switch(X,Y,Sch,goto_switch2).

+!wait_gatekeeper2[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : play(GP2,gatekeeper2,Gr) &
     goal_state(Sch, goto_switch2(S2X,S2Y), _) &
     ally_pos(GP2,S2X, S2Y)
  <- .print("fff gatekeeper2 passed"); 
     jmoise.set_goal_state(Sch,wait_gatekeeper2,satisfied).
     
+!wait_gatekeeper2[scheme(Sch),mission(Mission),group(Gr),role(Role)]
   : play(GP2,gatekeeper2,Gr) &
     goal_state(Sch, goto_switch2(S2X,S2Y), _) 
  <- //.wait( { +pos(_,_,_) } );
     //!!wait_gatekeeper2[scheme(Sch),mission(Mission),group(Gr),role(Role)].
     .wait( { +ally_pos(GP2,S2X, S2Y) } );
     jmoise.set_goal_state(Sch,wait_gatekeeper2,satisfied).


+!last_pass[scheme(Sch), group(Gr)]
  <- ?goal_state(Sch,pass_fence(FX,FY,_NextSch,_),_);
     .print("fff I should pass the fence ",FX,",",FY);
     jia.other_side_fence(FX,FY,TX,TY);
     .print("fff the new target is ",TX,",",TY);
     -+target(TX,TY);
     .wait({ +pos(TX,TY,_) } ).
     
-!last_pass[error(E), error_msg(M),code_line(L)] 
  <- .print("fff error ",E," ",M," line ",L).
  
+!wait_others_pass[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : //.my_name(Me) & .findall(P, play(P,_,Gr) & P \== Me, Others) &
     play(GP1, gatekeeper1, Gr) &
     .print("fff I should wait ",GP1) &
     all_passed([GP1])
  <- .print("fff all passed");
     jmoise.set_goal_state(Sch,wait_others_pass,satisfied);
     
     ?goal_state(Sch,pass_fence(_FX,_FY,NextSch,_),_);
     
     // if I am the poter1 (the last to pass), I need to finish it all
     .findall(P, play(P,_,Gr), Players);
     // and restart team mates
     if (NextSch == explore_sch) {
        .print("fff asking ",Players," to create exploration group");
    	.send(Players, achieve, quit_all_missions_roles);
    	.wait(200); // wait them to quit
    	.send(Players, achieve, create_exploration_gr)
     }{
        .print("fff not implemented yet")
     };
     .print("fff removing the scheme ",Sch," since all agentes has passed");
  	 jmoise.remove_scheme(Sch). // must be the last thing (since the deletion of the scheme cause the drop of this goal)
     
+!wait_others_pass[scheme(Sch),mission(Mission),group(Gr),role(Role)]
  <- .wait( { +ally_pos(_,_,_) } ); // any change in ag loc, check
     !!wait_others_pass[scheme(Sch),mission(Mission),group(Gr),role(Role)].
