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

+luxuriousHotel(L)[o(holidays), source(Source)]
	<-	
	.print("The ", L, " luxurious hotel is available");
	
	// construct a class containing all cities in which L is located
	jasdl.ia.define_class(cityOfL, "holidays:city and holidays:hasAccommodation value self:", L);
	.send(Source, askOne, cityOfL(City)[o(self)], cityOfL(City)[o(self)]); // use to "query" to sender of this luxuriousHotel (travel_agent)
	.print(L, " is located in the city ", City);
	
	// check given hotel is located in a destination that has some museums
	jasdl.ia.define_class(hotelNearMuseums, "holidays:isLocatedAt some (holidays:hasActivity some holidays:museums)");
	.send(Source, askOne, hotelNearMuseums(HotelNearMuseums)[o(self)], hotelNearMuseums(HotelNearMuseums)[o(self)]); // use to "query" to sender of this luxuriousHotel (travel_agent)
	.print(HotelNearMuseums, " is a hotel located in a city with some museums");
	
	// Shows a class definition in part composed of other run-time class definitions
	jasdl.ia.define_class(luxuriousHotelNearMuseums, "self:hotelNearMuseums AND holidays:luxuriousHotel");
	.send(Source, askOne, luxuriousHotelNearMuseums(LuxuriousHotelNearMuseums)[o(self)], luxuriousHotelNearMuseums(LuxuriousHotelNearMuseums)[o(self)]); // use to "query" to sender of this luxuriousHotel (travel_agent)
	.print(LuxuriousHotelNearMuseums, " is a luxurious hotel located in a city with some museums");
	
	// bundle([h_1...h_n]) demonstrates how JASDL deals with nested se-content
	// in particular, this query bundles together an arbitrary number of queries into a single message
	.send(Source, askOne, bundle( [ luxuriousHotel(H1)[o(holidays)], hotel(H2)[o(holidays)], activity(H3)[o(holidays)], hotelNearMuseums(H4)[o(self)] ] ), Response); // use to "query" to sender of this luxuriousHotel (travel_agent)
	.print("Bundled query response: ", Response);	
	
	// ontology refered to by "places" is not known by travel_agent, but will be instantiated at run-time so it can usefully deal with this message
	.send(Source, tell, building(travel_lodge)[o(places)]);
	.send(Source, askOne, building(travel_lodge)[o(places)], _, 1000); // ensure information has been recieved by sender (travel_agent)	
	
	// demonstrates use of class definitions that mix entities from different ontologies
	jasdl.ia.define_class(buildingAndHotel, "places:building and holidays:hotel");
	.send(Source, askOne, buildingAndHotel(BuildingAndHotel)[o(self)], buildingAndHotel(BuildingAndHotel)[o(self)]);
	.print(BuildingAndHotel, " is a building and a hotel");
	
	.send(Source, tell, example_KSAA_complete).

// Shows how we can receive all_different assertions that can now be sent (since they are no longer asserted using internal actions)
+all_different(XS)[o(holidays), source(Source)]
	<-
	.print(XS, " are mutually distinct individuals, according to ", Source).
	
	
