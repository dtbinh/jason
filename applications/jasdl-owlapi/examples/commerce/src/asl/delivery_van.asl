{include("common/society.asl")}
{include("common/commerce.asl")}

!init.

+!init
	<-
	.my_name(Me);
	?isEmployedBy(Me, Employer)[o(s)];
	.send(Employer, tell, available(Me)).


+!hasPosition(Me, X, Y)[source(Employer)] : .my_name(Me) & hasPosition(Me, X, Y) & isEmployedBy(Employer)
	<-
	.send(Employer, tell, hasPosition(Me, X, Y)).

+!hasPosition(Me, X, Y)[source(Employer)] :
	.my_name(Me) &
	not hasPosition(Me, X, Y) &					// Destination must be specified
	isEmployedBy(Employer)
	<-
	move_towards(X, Y);
	.wait(10); // <- simulate movement speed
	!!hasPosition(Me, X, Y)[source(Employer)].

@load_all_2[atomic]	
+!load([])[source(Employer)]
	<-
	.send(Employer, tell, loading_complete).

@load_all_1[atomic]
+!load([Purchase|Purchases])[source(Employer)] :
	isEmployedBy(Employer)
	<-
	load(Purchase);										// Load purchase
	!!load(Purchases)[source(Employer)].

@unload_all_2[atomic]
+!unload([])[source(Employer)]
	<-
	.send(Employer, tell, unloading_complete).

@unload_all_1[atomic]
+!unload([Purchase|Purchases])[source(Employer)] :
	isEmployedBy(Employer)
	<-
	unload(Purchase);												// Unload purchase
	!!unload(Purchases)[source(Employer)].						// Unload remaining purchases
	
+?~cargo(Purchase) : not cargo(Purchase).	// (not cargo) implies ~cargo for the delivery van
	
	
