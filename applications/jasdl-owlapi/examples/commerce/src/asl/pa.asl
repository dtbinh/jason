{include("common/society.asl")}
{include("common/commerce.asl")}


/** Plans that are called by the customer agent */

+!suitable(ProductClass, ShopClass, Qty)
	<-		
		?isEmployedBy(Customer);
		
		?hasOrder(Customer, Order)[o(c)];
		
		?ShopClass;
		jasdl.ia.get_individual(ShopClass, Shop);		
	
		// Generate a unique (guaranteed across entire agent society) request identifier (individual) for the new purchase
		jasdl.ia.get_anonymous_individual(ID);
	
		jasdl.ia.get_class_definition(ProductClass, ProductDescription);
		
		// Add the condition that the product must be in stock (this is not something the customer should be concerned with)
		.concat("(",ProductDescription,") and c:hasInStock some integer [ >= ",Qty,"]",ProductDescriptionInStock);
		
		// Parse the product description into a class
		jasdl.ia.define_class(productInStock, ProductDescriptionInStock);	
		
		.print("Asking ",Shop,":", ProductDescriptionInStock);
		
		// Ask for all brands that match these criteria
		.send(Shop, askOne,
			productInStock(Brand)[o(self), id(ID), stockist(Stockist), jasdl_tg_cause(_)],
			productInStock(Brand)[o(self), id(ID), stockist(Stockist), jasdl_tg_cause(_)]);	
		
		.print(Stockist);

		// ask for the customer's approval
		!!approve(ID, Brand, ProductDescription, Shop, Qty, Order, Stockist).
		

		
+!approve(ID, Brand, ProductDescription, Shop, Qty, Order, Stockist)
	<-
		?isEmployedBy(Customer);
		.send(Customer, askOne,
			approve(Brand, Answer),
			approve(Brand, true));
			
		// Commit the purchase details to the belief base
		+hasBrand(ID, Brand)[o(c)];
		+hasShop(ID, Stockist)[o(c)];
		+includes(Order, ID)[o(c)];
		+purchase(ID)[o(c)];	
		+hasQuantity(ID, Qty)[o(c)].
			
-!approve(ID, Brand, ProductDescription, Shop, Qty, Order, Stockist)
	<-
		?isEmployedBy(Customer);
		
		// Get the class describing the type of product, adding the additional requirement that it
		// also not be the individual rejected by the customer
		jasdl.ia.define_class(ammendedProductClass, "(",ProductDescription,") and not {c:",Brand,"}");		
		
		// We use the same achieve goal used by a customer here, effectively emulating a customer request, but with the ammended product
		// description, given the customer's rejection
		!suitable(ammendedProductClass(_)[o(self)], shop(Shop)[o(s)], Qty).
	
	
-!suitable(ProductClass, ShopClass, Qty)
		<-
		?isEmployedBy(Customer);
		.send(Customer, tell, message("I am sorry but I am unable to service your request."));
		.fail.

		
+!order_confirmed[source(Customer)] : isEmployedBy(Customer)
	<-
		?hasOrder(Customer, Order)[o(c)];
		.findall(ID, purchase(ID)[o(c)], Purchases);
		!order_confirmed(Purchases, []).
		
-!order_confirmed[source(Customer)] : isEmployedBy(Customer) & not hasOrder(Customer, _)[o(c)]
	<-
		.send(Customer, tell, message("You must request some products first")).


+!order_confirmed([Purchase|Purchases], Shops)
	<-		
	
	// Get the details of this purchase
		?hasBrand(Purchase, Brand)[o(c)];
		?hasQuantity(Purchase, Qty)[o(c)];
	// JASDL current doesn't support domain-unground property queries
	// Instead, we use inverse properties such as this one (includedIn is the inverse of includes)
	// Note that both types of assertions have exactly the same meaning as far as JASDL is concerned
		?includedIn(Purchase, Order)[o(c)];	
	
	// ... and send them to the appropriate shop agent		
		?hasShop(Purchase, Shop)[o(c)];
		.send(Shop, tell, purchase(Purchase)[o(c)]);
		.send(Shop, tell, includedIn(Purchase, Order)[o(c)]);
		.send(Shop, tell, hasBrand(Purchase, Brand)[o(c)]);
		.send(Shop, tell, hasQuantity(Purchase, Qty)[o(c)]);
		
	// Confirm the remaining purchases
		!order_confirmed(Purchases, [Shop|Shops]).
		
		
+!order_confirmed([], Shops)
	<-
	// Now we have sent all purchase details, send the location where the order should be delivered to (i.e. the customer)
		?isEmployedBy(Customer);
		?hasOrder(Customer, Order)[o(c)];	
		.send(Shops, tell, hasCustomer(Order, Customer)[o(c)]);
		
	// ... and 	finish with the order itself, which will instantiate the dispatch process in the shop agents
		.send(Shops, tell, order(Order)[o(c)]);
		
	// abolish the order (and all included purchases), we no longer need it
		!abolish_order(Order);
	
	// Inform the customer		
		.send(Customer, tell, message("Your order has been confirmed and will be dispatched shortly.")).

		
/******************************************************************************/

	
	
+failed(Message)[id(ID), source(Shop)] :
	shop(Shop)[o(s)]
	<-
	// Remove this belief, we no longer need it
		-failed(Message)[id(ID), source(Shop)];
	
	// Delete all trace of the purchase
		!abolish_purchase(ID);
	
	// Get the customer
		?isEmployedBy(Customer);
		
	// Pass the news on to the customer
		.send(Customer, tell, message(Message)).



	
