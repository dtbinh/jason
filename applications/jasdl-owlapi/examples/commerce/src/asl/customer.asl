{include("common/society.asl")}

/* Orders specified incrementally and tracked by PA. Allows real-time feedback about e.g. product availability
 * Notice that ontology and syntactic translation is providing a well-defined shared vocabulary between agents.
 * Note with an accurate ontology alignment our agents could communicate even without prior agreement on terms.
 * - the structural information in the ontology gives us a richer palette for semantic matching over and above that available through
 *   e.g. synonym matching.
 * Assuming central repository of agents and their types (society.owl schema)
 * IDs are propagated through annotations to match product request brands with quantities
 */

	
 
+ui_product_request(ProductDescription, ShopDescription, Qty)[source(percept)]
	<-	
	.print("UI recieved request");
	
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
	jasdl.ia.send(PA, achieve, suitable(suitableProduct(_)[o(self)], suitableShop(_)[o(self)], Qty)).
	
	
+!product(Brand)[o(c), source(PA), approve, id(ID)] :
	employs(PA)
	<-
	approve(Brand);
	.send(PA, tell, approved(product(Brand)[o(c), id(ID)])).
	
-!product(Brand)[o(c), source(PA), approve, id(ID)] :
	employs(PA)
	<-
	.send(PA, tell, rejected(product(Brand)[o(c), id(ID)])).
	
	


