+!abolish_order(Order)	:
	order(Order)[o(c)]
	<-
	// Abolish beliefs about purchases included in this order, we no longer need them
		.findall(Purchase, includes(Order, Purchase)[o(c)], Purchases);
		!abolish_purchases(Purchases);	
	
		-order(Order)[o(c), source(_)];
	// note: inverse of hasOrder
		-hasCustomer(Order, Customer)[o(c), source(_)]. 
		
+!abolish_purchases([]).
+!abolish_purchases([Purchase|Purchases])
	<-
	!abolish_purchase(Purchase);
	!abolish_purchases(Purchases).
	
	
+!abolish_purchase(ID) :
	purchase(ID)[o(c)]
	<-		
		-hasProductDescription(ID, _)[o(c), source(_)];
		-includedIn(ID, _)[o(c), source(_)];
		-hasQuantity(ID, _)[o(c), source(_)];
		-purchase(ID)[o(c), source(_)];
		-hasShop(ID, _)[o(c), source(_)].
	
