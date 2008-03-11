/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

!example_UBB_1.
!example_UBB_2.
!example_QBB.
!example_RPP.
!example_all_different.
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
	
	+museums(scienceMuseum)[o(travel)];
	+hasActivity(london, scienceMuseum)[o(travel)];	
	+hotel(travel_lodge)[o(travel)];
	
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
	/* 6 */ ?hasPricePerNight(hilton, Price)[o(travel)];
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

// for dealing with bundled queries - see customer.asl
@bundle_1[atomic]
+?bundle(Hs)
	<-
	?bundle(Hs, 0).
	
@bundle_2[atomic]
+?bundle(Hs, N) : .length(Hs,L) & N<L
	<-
	.nth(N, Hs, H);
	?H;
	?bundle(Hs, N+1).
	
@bundle_3[atomic]
+?bundle(Hs, N) : .length(Hs,L) & N==L.

@example_KSAA_complete[atomic]
+example_KSAA_complete
	<-
	.print("Completed: Knowledge Sharing Among Agents").
	
	
/**
 * Demonstrates use of jasdl.ia.all_different internal action
 */
@example_all_different[atomic]
+!example_all_different
	<-
	.print("Example: all_different internal action");	
	+destination(butlins)[o(travel)];
	+hotel(butlins_hotel)[o(travel)];
	+hasAccommodation(butlins, butlins_hotel)[o(travel)];	
	+yoga(butlins_yoga)[o(travel)];
	+sunbathing(butlins_sunbathing)[o(travel)];		
	+hasActivity(butlins, butlins_yoga)[o(travel)];
	+hasActivity(butlins, butlins_sunbathing)[o(travel)];	
	// Query below will not succeed unless butlins_yoga and butlins_sunbathing are different individuals since family destination requires min 2 *different* activities.
	// Note: OWL doesn't make UNA and since these individuals do not belong to disjoint classes, therefore they must be explicitly asserted as different.
	jasdl.ia.all_different([butlins_yoga, butlins_sunbathing], travel);
	?familyDestination(butlins)[o(travel)];
	.print("Completed: all_different internal action").
	

	




