{include("common/society.asl")}
{include("common/commerce.asl")}

/* Orders specified incrementally and tracked by PA. Allows real-time feedback about e.g. product availability
 * Notice that ontology and syntactic translation is providing a well-defined shared vocabulary between agents.
 * Note with an accurate ontology alignment our agents could communicate even without prior agreement on terms.
 * - the structural information in the ontology gives us a richer palette for semantic matching over and above that available through
 *   e.g. synonym matching.
 * Assuming central repository of agents and their types (society.owl schema)
 * IDs are propagated through annotations to match product request brands with quantities
 */
 

+!init
	<-
	// ask the PA if it already has a current order listed for this custoemr
		?employs(PA);
		.send(PA, askOne, order(Order)[o(c)], false);
		
	// ... it does not, give it a new order ID and associate it with this customer
		?employs(PA);
	
		jasdl.ia.get_anonymous_individual(Order);
		
		.my_name(Me);	
		+order(Order)[o(c)];
		+hasOrder(Me, Order)[o(c)];	// note: inverse of hasCustomer
		
		.send(PA, tell, order(Order)[o(c)]);
		.send(PA, tell, hasOrder(Me, Order)[o(c)]).
	
-!init.			

	
/**
 * The user has hit the "submit request" button on the customer UI.
 */
+ui_product_request(ProductDescription, ShopDescription, Qty)[source(percept)]
	<-
	!init;
	
	// Get a PA employed by me
	?employs(PA);
	
	// Explicitly discard the percept - in case an identical request comes in next perception cycle (we still want to catch it)
	-ui_product_request(ProductDescription, ShopDescription, Qty)[source(percept)];
	
	// Must be defined locally in order to take advantage of syntactic translation.
	// If these were simply sent as string to PA and this agent had different entity aliases / ontology labels
	// then the PA would not be able to understand the request.
	
	jasdl.ia.define_class(suitableProduct, ProductDescription);
	jasdl.ia.define_class(suitableShop, ShopDescription);	
	
	// Request a suitable brand from the PA
	.send(PA, achieve, suitable(suitableProduct(_)[o(self)], suitableShop(_)[o(self)], Qty)).

/*
 * The user has hit the "confirm order" button on the customer UI.
 */
+ui_confirm_order[source(Percept)]
	<-
	// Find the PA I employ
		?employs(PA);
	// Ask the PA to achieve the state of affairs such that the customer's current order is confirmed with the necessary shop agents.
		.send(PA, achieve, order_confirmed).

/**
 * A message has been recieved from the PA. Execute the "message" environmental action,
 * thus displaying a dialog box on the customer UI.
 */
+message(Message)[source(PA)] : pA(PA)[o(s)]
	<-
	// We no longer need (or want) this belief
		-message(Message)[source(PA)];
	// Display the dialog on the customer UI
		message(Message).
	
/**
 * The PA has asked for approval from the customer to make a purchase (identified by the approve annotation).
 * This results in the execution of the "approve" environmental action. This displays a confirm dialog on the
 * customer UI and suspends the thread awaiting a response.
 * The event and resulting response is annotated with the id annotation, thus allowing the PA to match
 * the approval response with the correct purchase.
 */
+?approve(Brand, Answer)
	<-
		// Execute the "approve" environmental action
		approve(Brand);
		// Will only be reached if the user hits "OK" - otherwise the above environmental action will fail
		Answer = true.
	
	


