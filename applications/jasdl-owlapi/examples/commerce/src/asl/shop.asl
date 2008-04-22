{include("common/society.asl")}
{include("common/commerce.asl")}

/*
 * Unify with a shop that is a member of the class specified by Expression
 *
 * This is a good example of how certain JASDL features (such as run-time class definition and SE-Literal queries) 
 * can be conveniently expressed within a rule.
 */
possibleStockist(ID, Expression, PossibleStockist) :-
	jasdl.ia.define_class(possibleStockist, Expression) &
	possibleStockist(PossibleStockist)[o(self)] &
	shopInSameCompany(PossibleStockist) &	// note: this constraint could be more flexible by using an expression such as '' in the above class definition
	not exhausted(ID)[source(PossibleStockist)] &
	not (PossibleStockist == Me).
	
	

/**
 * Unify with a shop, other than this one, that is owned by the same company as this one.
 */
shopInSameCompany(Shop) :-
	shop(Shop)[o(s)] &
	.my_name(Me) &
	ownedBy(Me, Company)[o(s)] &
	owns(Company, Shop)[o(s)] &
	(not Shop==Me).
	
	
	
/*
 * This shop has managed to find a suitable product in its belief base, directly
 * in response to a PA. Respond to the PA, keeping id annotation intact so the
 * PA can match this response with the customer's request.
 */
@found_product_for_PA
+?product(Brand)[o(c), id(ID), stockist(Me), jasdl_tg_cause(OriginalQuery)] :
	OriginalQuery	// the agent has found a suitable product within its belief base
	<-
		.print("Recieved request and is able to service it");
		.my_name(Me);
		jasdl.ia.get_individual(OriginalQuery, Brand).

/**
 * Notice that the two plans below also with "not OriginalQuery" in their context will take precence over this one,
 * since their are both more specific than owl:thing (which is the most general concept) - additionally, plan ordering
 * is irrelevant here since JASDL automatically assigns precence according to concept specificity for trigger generalisation
 */
+?thing(Brand)[o(owl), id(_), stockist(_), jasdl_tg_cause(OriginalQuery)] :
	not OriginalQuery	// the agent has found a suitable product within its belief base
	<-
		.print("Recieved request and is UNABLE to service it");
		.fail.	
		
/**
 * Requested a type of product that this agent cannot supply, try asking any other shop.
 */
+?product(Brand)[o(c), id(ID), stockist(PossibleStockist), jasdl_tg_cause(OriginalQuery)] :
	not OriginalQuery &
	possibleStockist(ID, "s:shop", PossibleStockist)
	<-
	.print("Unknown GENERAL PRODUCT requested");
	!exhausted(ID);
	?stocked_by_another_shop(PossibleStockist, ID, OriginalQuery, Brand).

/**
 * Requested a type of vegetable product that this agent cannot supply, try asking a greengrocer or a supermarket.
 */
+?vegetable(Brand)[o(c), id(ID), stockist(PossibleStockist), jasdl_tg_cause(OriginalQuery)] :
	not OriginalQuery &
	possibleStockist(ID, "s:greenGrocers or s:supermarket", PossibleStockist)
	<-
	.print("Unknown VEGETABLE PRODUCT requested");
	!exhausted(ID);
	?stocked_by_another_shop(PossibleStockist, ID, OriginalQuery, Brand).

/**
 * Requested a type of meat product that this agent cannot supply, try asking a butchers.
 */
+?meatProduct(Brand)[o(c), id(ID), stockist(PossibleStockist), jasdl_tg_cause(OriginalQuery)] :
	not OriginalQuery &
	possibleStockist(ID, "s:butchers", PossibleStockist)
	<-
	.print("Unknown MEAT PRODUCT requested");
	!exhausted(ID);
	?stocked_by_another_shop(PossibleStockist, ID, OriginalQuery, Brand).
	
+?stocked_by_another_shop(PossibleStockist, ID, Query, Answer)
	<-
	.print("Trying ", PossibleStockist);
	.add_annot(Query, id(ID), Q1);
	.add_annot(Q1, stockist(PossibleStockist), Q2);
	.add_annot(Q2, jasdl_tg_cause(_), Q3);	
	.send(PossibleStockist, askOne, Q3, Q3);	
	jasdl.ia.get_individual(Q3, Answer).
	
	
	
/**
 * Inform all shops that I have already been tried for this order and unable to service it
 * (so I am not asked again)
 */
+!exhausted(ID)
	<-
	.findall(Shop, shop(Shop)[o(s)], Shops);
	.send(Shops, tell, exhausted(ID)).
	
+!reset(ID)
	<-
	.findall(Shop, shopInSameCompany(Shop), Shops);
	.send(Shops, untell, exhausted(ID)).
   

   
   
/**
 * All plans below this point deal with coordinating delivery with a delivery van employed by the shop.
 * Note that these are not so interesting in terms of exploring JASDL extensions to Jason's core functionality, so
 * don't worry about them too much.
 */
	
/**
 * Attempts to find an avaiable delivery van (identified by available(Van)).
 * Once found, it deletes the van's status as available.
 * This plan must be atomic to prevent a van being allocated simultaneously to more
 * than one order (i.e. multiple ?'s before we get a chance to remove the van's available status)
 */
 
 
 /**
 * An order request has been recieved from a PA, coordinate delivery of it
 */
+order(Order)[o(c), source(PA)] :
	hasCustomer(Order, Customer)[o(c)] &                      
	employs(Customer, PA)[o(s)]
	<-
		// Delopy crates containing product
		!deploy(Order);
		// Obtain an available van (or suspend this intention until one it available)
		!allocate_available(Van);
		// Instruct this van to move to shop's position
		!recall(Van);
		// Instruct the van to load the deployed products into its cargo hold
		!load(Order, Van);
		// Instruct the van to move to the customer's position
		!dispatch(Order, Van);
		// Instruct the van to unload its cargo at its current position (i.e. the customer's)
		!unload(Order, Van);
		// Abolish beliefs about this order (and all included purchases), we no longer need them
		!abolish_order(Order);
		// Make the van available again
		+available(Van).
 
@available[atomic]
+!allocate_available(Van)
	<-
		?available(Van)[source(_)];
		-available(Van)[source(_)].

/*
 * The agent has attempted to find an available van (see above) within the belief base but has failed.
 * Simply suspend the intention until a van notifies the agent that it is available.
 */
+?available(Van)
	<-
		.concat("+",available(Van)[source(_)], WaitFor);
		.wait(WaitFor);
		?available(Van)[source(_)].

/**
 * Achieve the state of affairs such that a van has loaded an entire order into its cargo.
 * (Suspends the intention until this is so) 
 */
+!load(Order, Van) : .my_name(Me) & inVicinityOf(Van)
	<-
	-hasPosition(Van, X, Y)[source(Van)];	
	.findall(PID, includes(Order, PID)[o(c)], PIDs);
	.send(Van, achieve, load(PIDs));
	L=loading_complete[source(Van)];
	.concat("+", L, WaitFor);
	.wait(WaitFor);
	-L.
	
/**
 * Achieve the state of affairs such that a van has unloaded its entire cargo at its current destination
 * (Suspends the intention until this is so) 
 */	
+!unload(Order, Van)
	<-
	.findall(PID, includes(Order, PID)[o(c)], PIDs);
	.send(Van, achieve, unload(PIDs));
	L=unloading_complete[source(Van)];
	.concat("+", L, WaitFor);
	.wait(WaitFor);
	-L.

/**
 * Instruct a van to go to the shop's position
 * (Suspends the intention until this is so) 
 */
+!recall(Van)
	<-
	.my_name(Me);
	?hasPosition(Me, MX, MY);
	!hasPosition(Van, MX, MY).

/**
 * Instruct a van to go to the location of the customer assocaited with an order
 * (Suspends the intention until this is so) 
 */
+!dispatch(Order, Van)
	<-
	?hasCustomer(Order, Customer)[o(c)];
	.send(Customer, askOne, hasPosition(Customer, X, Y), hasPosition(Customer, X, Y));
	.print("Dispatching ", Van, " to ", X,",",Y,"(",Customer,")");
	!hasPosition(Van, X, Y).	
	
/**
 * Achieve the state of affairs such that a van is at a position on the grid.
 * (Suspends the intention until this is so)
 */
+!hasPosition(Van, X, Y) : L=hasPosition(Van, X, Y)
	<-	
		.send(Van, achieve, hasPosition(Van, X, Y));
		.concat("+", hasPosition(Van, X, Y)[source(Van)], WaitFor);
		.wait(WaitFor);
		-hasPosition(Van, X, Y)[source(_)].

/**
 * We think the van is already at the specified position,
 * but we'll try again just to be sure
 */
+!hasPosition(Van, X, Y) : hasPosition(Van, X, Y)
	<-
	-hasPosition(Van, X, Y)[source(_)];
	!hasPosition(Van, X, Y).



/**
 * (Recursively) achieve the state of affairs such that all purchases are deployed (i.e present
 * on the grid as a "crate") at the shop's location. 
 */
+!deploy([]).
+!deploy([Purchase|Purchases])
	<-
		?hasBrand(Purchase, Brand)[o(c)];
		?hasQuantity(Purchase, Qty)[o(c)];
	
	// The "deploy" environmental action results in the creation of a ModelCrate object
	// at the shop's current position and decreases it's stock for the supplied brand accordingly.
		deploy(Purchase, Brand, Qty);
		.print("Deploying crate", Purchase);
		!deploy(Purchases).
		
/**
 * Convenience plan that finds all the purchases in an order and instantiates !deploy above.
 */
+!deploy(Order)
	<-
		.findall(PID, includes(Order, PID)[o(c)], PIDs);
		!deploy(PIDs).

	

+?details(Brand, Details) : product(PID)[o(c)]
	<-
		?hasPrice(Brand, Price)[o(c)];
		?hasInStock(Brand, StockLevel)[o(c)];
	// Unifies Types with all asserted (since third parameter is "true") classifications of the individual Brand 
	// in the ontology identified by c
		jasdl.ia.get_types(Brand, c, true, Types);
		.concat("Brand Name=", Brand, ", Stock level=", StockLevel, ", Price=", ", Classifications=", Types, Price, Details).

/* Failed to get details. Reason: No price listed for product type */
-?details(Brand, Details) : product(Brand)[o(c)] & not hasPrice(Brand, Price)
	<-
	.concat("No price listed for product ", Brand, Details).
	
+!print_details(Brand) : product(Brand)[o(c)]
	<-
	?details(Brand, Details);
	.print(Details).
