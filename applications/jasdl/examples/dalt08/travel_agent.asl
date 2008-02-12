!example_UBB_1.
!example_UBB_2.
!example_QBB.
!example_RPP.
!example_KSAA.

@example_ubb_1[atomic]
+!example_UBB_1
	<-
	.print("Example: Updating Belief Base 1");
	+hotel(hilton)[o(travel)];						// hilton is a hotel
	+hasRating(hilton, threeStarRating)[o(travel)];	// hilton has three-star rating
	+city(london)[o(travel)];						// london is a city
	+hasAccommodation(london, hilton)[o(travel)];	// hilton is in london
	+country(england)[o(travel)];					// england is a country
	+urbanArea(windsor)[o(travel)];					// windsor is an urban area
	+isPartOf(windsor, london)[o(travel)];			// windsor is a part of london
	+isPartOf(london, england)[o(travel)];			// london is a part of england
	+hasPricePerNight(hilton, 22.0)[o(travel)];		// hilton costs £22 a night
	.print("Completed: Updating Belief Base 1").



@example_ubb_2[atomic]
+!example_UBB_2
	<-
	.print("Example: Updating Belief Base 2");
	/* 1 */ +hasRating(hilton, twoStarRating)[o(travel)];
	/* 2 */ +ruralArea(london)[o(travel)];
	.print("Completed: Updating Belief Base 2").
	
	
@example_qbb[atomic]
+!example_QBB
	<-
	.print("Example: Querying Belief Base");
	/* 1 */ ?accommodation(hilton)[o(travel)];
	/* 2 */ ?luxuryHotel(LuxuryHotel)[o(travel)];
	/* 3 */ ?~budgetAccommodation(hilton)[o(travel)]; 	
	/* 4 */ .findall(Thing, thing(Thing)[o(Ontology)], E);	
	/* 5 */ ?countryOf(windsor, Country);
	/* 6 */ ?hasPricePerNight(Hotel, Price)[o(travel)];
	.print("Completed: Querying Belief Base").
+?countryOf(Destination, Country) :
	isPartOf(Destination, Country)[o(travel)] &
	country(Country)[o(travel)].

@example_rpp[atomic]	
+!example_RPP
	<-
	.print("Example: Retrieving Relevant Plans");
	!luxuryHotel(fourSeasons)[o(travel)];
	.print("Completed: Retrieving Relevant Plans").
/* 1 */ +!hotel(hilton)[o(travel)] 		  <- +hotel(hilton)[o(travel)].
/* 2 */ +!hotel(H)[o(travel)] : false 	  <- +hotel(H)[o(travel)].
/* 3 */ +!accommodation(A)[o(travel)]     <- +accommodation(A)[o(travel)].

@example_ksaa[atomic]
+!example_KSAA
	<-
	.print("Example: Knowledge Sharing Among Agents");
	.send(customer, tell, luxuryHotel(hilton)[o(travel)]).
	
+example_KSAA_complete
	<-
	.print("Completed: Knowledge Sharing Among Agents").
	
	

	




