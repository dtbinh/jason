hotel(hilton)[o(travel)].
hasRating(hilton, threeStarRating)[o(travel)].
hasPricePerNight(hilton, 22.0)[o(travel)].
all_different([hilton, fourSeasons])[o(travel)].
!start.

+!start
	<-
	?all_different([hilton, fourSeasons])[o(travel)];
	?hotel(X)[o(travel)];
	?hotel(X)[o(travel)];
	.print(X);
	
	?hasRating(hilton, Y)[o(travel)];
	?hasRating(hilton, Y)[o(travel)];
	.print(Y);
	
	?hasPricePerNight(hilton, Z)[o(travel)];
	?hasPricePerNight(hilton, Z)[o(travel)];	
	.print(Z);
	
	?thing(A)[o(owl)];
	.print(A);
	
	?~hotel(B)[o(travel)];
	?~hotel(B)[o(travel)];
	.print(B);
	
	+~activity(fourSeasons)[o(travel)];
	?~activity(fourSeasons)[o(travel)];
	.print("Completed").
