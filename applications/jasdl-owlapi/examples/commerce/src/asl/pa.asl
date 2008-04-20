{include("common/society.asl")}



+!suitable(ProductClass, ShopClass, Qty)[source(Customer)] :
	isEmployedBy(Customer)
	<-	
	?hasOrder(Customer, Order)[o(c)];
	.print(Order);
	
	?ShopClass;
	jasdl.ia.get_individual(ShopClass, Shop);
	

	// Generate a unique (guaranteed across entire agent society) request identifier (individual)
	jasdl.ia.get_anonymous_individual(ID);
	
	jasdl.ia.get_class_definition(ShopClass, ShopDescription);
	jasdl.ia.get_class_definition(ProductClass, ProductDescription);
	
	// Add the condition that the product must be in stock (this is not something the customer should be concerned with)
	.concat("(",ProductDescription,") and c:hasInStock some integer [ >= ",Qty,"]",ProductDescriptionInStock);
	
	// Parse the product description into a class
	jasdl.ia.define_class(productInStock, ProductDescriptionInStock);	
	
	.print("Asking ",Shop,":", ProductDescriptionInStock);
	
	// Ask for all brands that match these criteria
	//jasdl.ia.send(Shop, achieve, suitable(description(Brand)[o(self)])[id(ID)]).
	jasdl.ia.send(Shop, askOne, productInStock(Brand)[o(self), id(ID)]);	

	+purchase(ID)[o(c)];
	+includes(Order, ID)[o(c)];
	+hasQuantity(ID, Qty)[o(c)];
	+hasProductDescription(ID, ProductDescription)[o(c)];
	+hasShopDescription(ID, ShopDescription)[o(c)].
	
	
+product(Brand)[o(c), id(ID), source(Shop)] :
	shop(Shop)[o(s)] // from a known shop
	<-
	!approve(product(Brand)[o(c), id(ID)]).
	
+referral(Referred)[id(ID), source(Shop)] :
	shop(Referred)[o(s)] &
	shop(Shop)[o(s)]
	<-
	-referral(Referred)[id(ID), source(Shop)];
	.print("referred to ", Referred).
	
+failed[id(ID), source(Shop)] :
	shop(Shop)[o(s)]
	<-
	!abolish_purchase(ID);
	.print("Failed!").
	
+!approve(A)
	<-
	
	// get my employer (rule expressed in terms of SE-Literals, see common/society.asl)
	?isEmployedBy(Employer);
	
	.my_name(Me);
	.add_annot(A, source(Me), B);
	
	// ask customer for approval
	// We add the approve annotation to isolate this
	// type of query from others
	.add_annot(B, approve, C);
	.send(Employer, achieve, C).
	
+approved(product(Brand)[o(c), id(ID)])[source(Employer)] :
	isEmployedBy(Employer)
	<-
	+hasBrand(ID, Brand)[o(c)];
	.print("Approved ", Brand, " (id: ", ID, ")").
	
@sdfdsf[breakpoint]
+rejected(product(Brand)[o(c), id(ID)])[source(Employer)] :
	isEmployedBy(Employer)
	<-
	?includedIn(ID, Order)[o(c)];
	?hasQuantity(ID, Qty)[o(c)];
	?hasProductDescription(ID, ProductDescription)[o(c)];
	?hasShopDescription(ID, ShopDescription)[o(c)];
	
	!abolish_purchase(ID);
	
	.concat("(",ProductDescription,") and not {c:",Brand,"}", AmmendedProductDescription);

	.print("Rejected ", Brand, " (id: ", ID, ")");
	
	// We need this individual to be instantiated in our own commerce ontology
	// to allow us to safely refer to it in class expressions
	//+product(Brand)[o(c)];
	
	// Get the class describing the type of product, adding the additional requirement that it
	// also not be the individual rejected by the customer
	jasdl.ia.define_class(ammendedProductClass, AmmendedProductDescription);
	
	// Get the class describing the desired type of shop, without changing any requirements
	jasdl.ia.define_class(shopClass, ShopDescription);
	
	.print("Rejected ", Brand, ". Retry with shop=", ShopDescription, " and product=", AmmendedProductDescription);
	
	// We use the same achieve goal used by a customer here, effectively emulating a customer request, but with the ammended product
	// description, given the customer's rejection
	!suitable(ammendedProductClass(_)[o(self)], shopClass(_)[o(self)], Qty)[source(Employer)].

+!abolish_purchase(ID) :
	purchase(ID)[o(c)]
	<-		
	-hasProductDescription(ID, _)[o(c), source(self)];
	-hasShopDescription(ID, _)[o(c), source(self)];
	-includedIn(ID, _)[o(c), source(self)];
	-hasQuantity(ID, _)[o(c), source(self)];
	-purchase(ID)[o(c), source(self)].
	


/* Opening a new order.
 * Will only be generated with no order can be found (or at least none by this name)
 */
+?hasOrder(Customer, Order)[o(c)] :
	isEmployedBy(Customer)
	<-
	jasdl.ia.get_anonymous_individual(Order);
	+hasOrder(Customer, Order)[o(c)].
	
	
