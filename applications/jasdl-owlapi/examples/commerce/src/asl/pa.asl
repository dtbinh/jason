{include("common/society.asl")}



+!order(Order, Product, Qty)[source(Employer)] : isEmployedBy(Employer)
	<-
	?shop(Shop)[o(s)];
	jasdl.ia.send(Shop, askOne, hasOrder(Employer, Order)[o(c)], hasOrder(Employer, Order)[o(c)]);	
	// get me a brand that is in stock
	jasdl.ia.get_class_definition(Product, ProductDefinition);
	jasdl.ia.define_class(productInStock, ProductDefinition);//, " and (c:hasInStock some integer [>= ",Qty ," ])");	
	jasdl.ia.send(Shop, askOne, productInStock(Brand)[o(self)], product(Brand)[o(c)]);
	.print("Brand name: ", Brand);
	/* Check against medical requirements of customer */
	.send(Shop, achieve, add_to_order(Order, Brand, Qty));
	Confirmed = confirmed(Order, Brand, Qty)[source(Shop)];
	.concat("+", Confirmed, WaitConfirmed); .wait(WaitConfirmed);
	-Confirmed;
	.send(Employer, tell, Confirmed).
	
	
+!confirm_order(Order)[source(Employer)] : isEmployedBy(Employer)
	<-
	?shop(Shop)[o(s)];
	.send(Shop, achieve, confirm_order(Order)).
	


	
	
	
