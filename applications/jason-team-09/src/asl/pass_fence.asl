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
     is_vertical(FX,FY)    & .print("fff ",FX,FY," is vertical")   & AX * Direction > (FX * Direction) |
     .print("fff it is impossible that the fence is neither horizontal nor vertical") & false) &
   .print("fff ",Ag," passed, it is at ",AX,",",AY," shoud reach place ",FX,",",FY) &
   all_passed(Others).

is_horizontal(FX,FY) :- fence(FX+1,FY,_) | fence(FX-1,FY,_).
is_vertical(FX,FY)   :- fence(FX,FY+1,_) | fence(FX,FY-1,_).

{ begin maintenance_goal("+pos(_,_,_)") }

+!pass_fence[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : need_cross_fence(FX, FY) &
     jia.fence_switch(FX, FY, SX, SY)
  <- jmoise.create_scheme(pass_fence, SchId);
     .print("fff Created pass fence scheme ", SchId); 
     jia.switch_places(SX,SY,P1X,P1Y,P2X,P2Y);
     .print("fff places for switch ",SX,",",SY," are ",P1X,",",P1Y," and ",P2X,",",P2Y);
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
  <- .print("fff I need to discover where the switch is ");
     -+target(FX, FY).
     
+!pass_fence[scheme(Sch),mission(Mission),group(Gr),role(Role)].
	 
{ end }	 

+!goto_switch1(X,Y)[scheme(Sch)]
  <- .print("fff going to switch1 ",X,",",Y);
     -+target(X,Y);
     .wait({ +pos(X,Y,_) } );
     .print("fff reached switch1");
     jmoise.set_goal_state(Sch,goto_switch1,satisfied).

+!goto_switch2(X,Y)[scheme(Sch)]
  <- .print("fff going to switch2 ",X,",",Y);
     -+target(X,Y);
     .wait({ +pos(X,Y,_) } );
     .print("fff reached switch2");
     jmoise.set_goal_state(Sch,goto_switch2,satisfied).

+!last_pass[scheme(Sch), group(Gr)]
  <- ?goal_state(Sch,pass_fence(FX,FY,NextSch,_),_);
     .print("fff I should pass the fence ",FX,",",FY);
     jia.other_side_fence(FX,FY,TX,TY);
     .print("fff the new target is ",TX,",",TY);
     -+target(TX,TY);
     .wait({ +pos(TX,TY,_) } );
     // if I am the poter1 (the last to pass), I need to finish it all
     .findall(P, play(P,_,Gr), Players);
     .print("fff removing the scheme ",Sch," since all agentes has passed");
  	 jmoise.remove_scheme(Sch);
     // and restart team mates
     if (NextSch == explore_sch) {
    	.send(Players, achieve, create_exploration_gr)
     }{
        .print("fff not implemented yet")
     }.

-!last_pass[error(E), error_msg(M),code_line(L)] 
  <- .print("fff error ",E," ",M," line ",L).
  
+!wait_others_pass[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : .print("fff wait team to pass ") &
     .my_name(Me) & .findall(P, play(P,_,Gr) & P \== Me, Others) &
     .print("fff I should wait agents ",Others) &
     all_passed(Others)
  <- .print("fff all passed");
     jmoise.set_goal_state(Sch,wait_others_pass,satisfied).
     
+!wait_others_pass[scheme(Sch),mission(Mission),group(Gr),role(Role)]
  <- .wait( { +pos(_,_,_) } );
     !!wait_others_pass[scheme(Sch),mission(Mission),group(Gr),role(Role)].
