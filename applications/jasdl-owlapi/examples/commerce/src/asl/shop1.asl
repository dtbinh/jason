{include("common/society.asl")}

/* Product catalogue could be specified in a file and imported at run-time */

	

+!add_to_order(Order, Brand, Qty)[source(PA)] :
	product(Brand)[o(c)] &															// check known product brand
	hasCustomer(Order, Customer)[o(c)] & employs(Customer, PA)[o(s)] &				// check PA is authorised to speak on behalf of customer whose order it is
	hasInStock(Brand, StockLevel)[o(c)] & StockLevel>=Qty							// check we have enough stock
	<-
	
	// get a unique purchase identifier
	jasdl.ia.get_anonymous_individual(PID);
	
	// add purchase
	+purchase(PID)[o(c)];
	+includes(Order, PID)[o(c)];
	+hasBrand(PID, Brand)[o(c)];
	+hasQuantity(PID, Qty)[o(c)];
	.send(PA, tell, confirmed(Order, Brand, Qty)).

@available[atomic]
+!allocate_available(Van)
	<-
	?available(Van)[source(_)];
	-available(Van)[source(_)].
	
+?available(Van)
	<-
	.concat("+",available(Van)[source(_)], WaitFor);
	.wait(WaitFor);
	?available(Van)[source(_)].

+!confirm_order(Order)[source(PA)] :
	hasCustomer(Order, Customer)[o(c)] &                      
	employs(Customer, PA)[o(s)]
	<-
	!deploy(Order);
	!allocate_available(Van);
	!recall(Van);
	!load(Order, Van);
	!dispatch(Order, Van);
	!unload(Order, Van);
	+available(Van);
	-includes(Order, _)[o(c)];
	.send(Customer, tell, order_complete(Order)).

+!load(Order, Van) : .my_name(Me) & inVicinityOf(Van)
	<-
	-hasPosition(Van, X, Y)[source(Van)];	
	.findall(PID, includes(Order, PID)[o(c)], PIDs);
	.send(Van, achieve, load(PIDs));
	L=loading_complete[source(Van)];
	.concat("+", L, WaitFor);
	.wait(WaitFor);
	-L.
	
+!unload(Order, Van)
	<-
	.findall(PID, includes(Order, PID)[o(c)], PIDs);
	.send(Van, achieve, unload(PIDs));
	L=unloading_complete[source(Van)];
	.concat("+", L, WaitFor);
	.wait(WaitFor);
	-L.
	
+!recall(Van)
	<-
	.my_name(Me);
	?hasPosition(Me, MX, MY);
	!hasPosition(Van, MX, MY).

+!dispatch(Order, Van)
	<-
	?hasCustomer(Order, Customer)[o(c)];
	.send(Customer, askOne, hasPosition(Customer, X, Y), hasPosition(Customer, X, Y));
	!hasPosition(Van, X, Y).	
	
	
+!hasPosition(Van, X, Y) : L=hasPosition(Van, X, Y) & not L
	<-	
	.send(Van, achieve, hasPosition(Van, X, Y));
	.concat("+", L[source(Van)], WaitFor);
	.wait(WaitFor);
	-L.
	
+!hasPosition(Van, X, Y) : L=hasPosition(Van, X, Y) & L.




+!deploy([]).
+!deploy([Purchase|Purchases])
	<-
	?hasBrand(Purchase, Brand)[o(c)];
	?hasQuantity(Purchase, Qty)[o(c)];
	deploy(Purchase, Brand, Qty);
	.print("Deploying crate", Purchase);
	!deploy(Purchases).
+!deploy(Order)
	<-
	.findall(PID, includes(Order, PID)[o(c)], PIDs);
	!deploy(PIDs).

	
	

+?details(Brand, Details) : product(PID)[o(c)]
	<-
	?hasPrice(Brand, Price)[o(c)];
	?hasInStock(Brand, StockLevel)[o(c)];
	jasdl.ia.get_types(Brand, self, true, Types);
	.concat("Brand Name=", Brand, ", Stock level=", StockLevel, ", Price=", ", Classifications=", Types, Price, Details).

/* Failed to get details. Reason: No price listed for product type */
-?details(Brand, Details) : product(Brand)[o(c)] & not hasPrice(Brand, Price)
	<-
	.concat("No price listed for product ", Brand, Details).
	
+!print_details(Brand) : product(Brand)[o(c)]
	<-
	?details(Brand, Details);
	.print(Details).
	
/* Opening a new order */
+?hasOrder(Customer, Order)[o(c)] // check customer is known?
	<-
	// do something else here?
	// instantiate order
	+hasOrder(Customer, Order)[o(c)].
	
	
/* Local Prices */

	
	
/* We trust the delivery_agent when it informs of a product removal */
/*
-hovis(PID)[o(c)]
	<-
	-product(PID)[o(c), source(self)];	
	?details(PID, Details);
	.print("Sold ", Details).
*/


/* Gets this shop's price for a class of individuals */
// TODO: jasdl.ia.seliteral(Literal)
/* SAFELY allowing classes to be subjects of properties*/
/*
Deprecated, prices are obtained statically
+?hasPrice(PID, Price) : .atom(PID)
	<-
	.print("Get price of ", PID);
	?hasPrice(product(PID)[o(c)], Price). // <- why doesn't this work?
	
	
	
	
+!hasPrice(product(PID)[o(c)], Price) // <- note: not semantically-enriched
	<-
	.findall(product(PID)[o(c)], product(PID)[o(c)], Groundings);
	.print("Setting price to ",Price," for ", Groundings);
	!hasPrice(Groundings, Price).
	
+!hasPrice([Grounding | Groundings], Price)
	<-
	jasdl.ia.get_individual(Grounding, PID);
	+hasPrice(PID, Price)[o(c)]; // <- note: semantically-enriched
	!hasPrice(Groundings, Price).
	
+!hasPrice([], _).
	
*/


/*
	Doesn't work, since concat creates string term and so includes quotation marks which are not valid URI characters
	.findall(ID, includes(Order, ID)[o(c)], IDs);
	.length(IDs, MaxID);
	.concat("purchase_", Order, "_", MaxID, PID);
	*/
