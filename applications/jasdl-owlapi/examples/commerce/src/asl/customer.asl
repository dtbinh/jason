{include("common/society.asl")}

/* Orders specified incrementally and tracked by PA. Allows real-time feedback about e.g. product availability
 * Notice that ontology and syntactic translation is providing a well-defined shared vocabulary between agents.
 * Note with an accurate ontology alignment our agents could communicate even without prior agreement on terms.
 * - the structural information in the ontology gives us a richer palette for semantic matching over and above that available through
 *   e.g. synonym matching.
 * Assuming central repository of agents and their types (society.owl schema)
 */

!behave.

 // need to use SE-Literals to take advantage of syntactic translation
+!behave
	<-
	.random(Rand);
	.wait(Rand*2000);
	
	.my_name(Me);
	jasdl.ia.define_class(employedPA, "s:pA and s:isEmployedBy value s:", Me);
	?employedPA(PA)[o(self)];
	!order(PA, Me, bread(_)[o(c)], 1);
	!order(PA, Me, milk(_)[o(c)], 1);
	!order(PA, Me, bread(hovis)[o(c)], 1);	
	.send(PA, achieve, confirm_order(Me)).
	
	
+!order(PA, Order, Product, Qty)
	<-
	.send(PA, achieve, order(Order, Product, Qty));
	Confirmed = confirmed(Order, Brand, Qty)[source(PA)];
	.concat("+", Confirmed, WaitConfirmed); .wait(WaitConfirmed);
	-Confirmed.
	
+order_complete(Order)[source(Shop)]
	<-
	-order_complete(Order)[source(Shop)];
	!behave.
	
	

