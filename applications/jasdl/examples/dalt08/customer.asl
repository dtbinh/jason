hasAccommodation(butlins, butlins_hotel)[o(holidays), source(tom)].
hasActivity(butlins, swimming)[o(holidays), source(tom)].
hasActivity(butlins, tennis)[o(holidays), source(tom)].
hotel(butlins_hotel)[o(holidays), source(ben)].
sports(tennis)[o(holidays), source(tom), source(ben)].
relaxation(swimming)[o(holidays), source(tom), asdasda(ssss)].
destination(butlins)[o(holidays), source(ben)].

!begin.

+!begin
	<-
	?destination(butlins)[o(holidays)];
	+quietDestination(butlins)[o(holidays)].








+luxuryHotel(L)[o(holidays), source(Source)]
	<-
	/* 1 */ .print("The ", L, " luxury hotel is available");
	///* 2 */ jasdl.ia.define_class(query, "city and hasAccommodation value ", L, holidays);
	/* 2 */ jasdl.ia.define_class(query, "city and hasAccommodation some {",L,"}", holidays);
	/* 3 */ .send(Source, askOne, query(City)[o(holidays)], query(City)[o(holidays)]);
	/* 4 */ .print(L, " is located in the city ", City);
	.send(Source, tell, example_KSAA_complete).
	
