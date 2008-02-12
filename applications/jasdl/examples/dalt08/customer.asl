+luxuryHotel(L)[o(holidays), source(Source)]
	<-
	/* 1 */ .print("The ", L, " luxury hotel is available");
	///* 2 */ jasdl.ia.define_class(query, "city and hasAccommodation value ", L, holidays);
	/* 2 */ jasdl.ia.define_class(query, "city and hasAccommodation some {",L,"}", holidays);
	/* 3 */ .send(Source, askOne, query(City)[o(holidays)], query(City)[o(holidays)]);
	/* 4 */ .print(L, " is located in the city ", City);
	.send(Source, tell, example_KSAA_complete).
	
	
	
	
	
