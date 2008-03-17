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

//+luxuriousHotel(L)[o(holidays), source(Source)]
+luxuriousHotel(L)[o(holidays), source(Source)]
	<-
	
	?hotel(L)[o(holidays)];	
	.print("The ", L, " luxurious hotel is available");
	jasdl.ia.define_class(query, "holidays:city and holidays:hasAccommodation value self:", L);
	.send(Source, askOne, query(City)[o(self)], query(City)[o(self)]);
	.print(L, " is located in the city ", City);
	
	// check given hotel is located in a destination that has some museums
	jasdl.ia.define_class(q2, "{self:",L,"} and holidays:isLocatedAt some (holidays:hasActivity some holidays:museums)");
	.send(Source, askOne, q2(Hotel)[o(self)], q2(Hotel)[o(self)]);
	.print(Hotel, " is located in a city with some museums");
	
	// bundle([h_1...h_n]) demonstrates how JASDL deals with nested se-content
	// in particular, this query bundles together an arbitrary number of queries into a single message
	.send(Source, askOne, bundle( [ luxuriousHotel(H1)[o(holidays)], hotel(H2)[o(holidays)], activity(H3)[o(holidays)], q2(H4)[o(self)] ] ), Response);	
	.print("Bundled query response: ", Response);	
	
	.send(Source, tell, example_KSAA_complete).


+all_different(XS)[o(holidays), source(Source)]
	<-
	.print(XS, " are mutually distinct individuals, according to ", Source);
	.send(Source, tell, building(travel_lodge)[o(places)]);
	.wait(1000); // wait to ensure travel_agent has had a chance to add this information to its belief base
	
	jasdl.ia.define_class(q3, "(places:place and places:building) and holidays:hotel");
	.send(Source, askOne, q3(Hotel)[o(self)], q3(Hotel)[o(self)]);
	.print(Hotel, " is a building and a hotel").
	
	
