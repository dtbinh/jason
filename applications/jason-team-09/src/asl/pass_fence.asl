/*
   -- plans for passing a fence 
*/

{ begin maintenance_goal("+pos(_,_,_)") }

+!pass_fence[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : target(TX,TY) & pos(MX, MY, _) & jia.has_object_in_path(MX, MY, TX, TY, closed_fence, FX, FY, Dist) &
     .print("fff I have a fence in my path at ",Dist," steps") &
     Dist < 10 &
     jia.fence_switch(FX, FY, SX, SY)
  <- .print("fff stopping current scheme ");
     jmoise.remove_scheme(Sch);
     jmoise.create_scheme(open_corral, [Gr], SchId);
     .print("fff Created pass fence scheme ", SchId); 
     // TODO: use switch place

     jmoise.set_goal_arg(SchId,goto_switch1,"X",SX);
     jmoise.set_goal_arg(SchId,goto_switch1,"Y",SY+1);
     jmoise.set_goal_arg(SchId,goto_switch2,"X",SX);
     jmoise.set_goal_arg(SchId,goto_switch2,"Y",SY-1);
     .findall(P, play(P,_,Gr), Cand1);
     !find_closest(Cand1,pos(SX,SY+1),HA1);
     .findall(P, play(P,_,Gr) & P \== HA1, Cand2);
     !find_closest(Cand2,pos(SX,SY-1),HA2); // do it latter
     .print("fff near 1 is ",HA1, " near 2 is ",HA2);
     .send(HA1, achieve, change_role(gatekeeper1, Gr));
     .send(HA2, achieve, change_role(gatekeeper2, Gr)).
     // others shoud just pass.

+!pass_fence[scheme(Sch),mission(Mission),group(Gr),role(Role)].
	 
{ end }	 

+!goto_switch1(X,Y)[scheme(Sch)]
  <- .print("fff going to switch1 ",X,",",Y);
     -+target(X,Y);
     .wait({ +pos(X,Y,_) } );
     jmoise.set_goal_state(Sch,goto_switch1,satisfied).

+!goto_switch2(X,Y)[scheme(Sch)]
  <- .print("fff going to switch2 ",X,",",Y);
     -+target(X,Y);
     .wait({ +pos(X,Y,_) } );
     jmoise.set_goal_state(Sch,goto_switch2,satisfied).

+!pass[scheme(Sch), group(Gr)]
  <- .print("fff I should pass the fence ").
//     jmoise.set_goal_state(Sch,allocate_gatekeeper2,satisfied).

{ begin maintenance_goal("+pos(_,_,_)") }

+!wait_team_pass[scheme(Sch),mission(Mission), group(Gr), role(Role)]
   : true
  <- .print("fff wait team to pass ").
// TODO: when ok, set statisfied and quite the role, goind back to boy/scouter/....

+!wait_team_pass[scheme(Sch),mission(Mission),group(Gr),role(Role)].
	 
{ end }	 
