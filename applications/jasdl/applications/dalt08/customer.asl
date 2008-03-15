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
	/* 1 */ .print("The ", L, " luxurious hotel is available");
	/* 2 */ jasdl.ia.define_class(query, "city and hasAccommodation value ", L, holidays);
	/* 3 */ .send(Source, askOne, query(City)[o(holidays)], query(City)[o(holidays)]);
	/* 4 */ .print(L, " is located in the city ", City);	
	
	// check given hotel is located in a destination that has some museums
	jasdl.ia.define_class(q2, "{",L,"} and isLocatedAt some (hasActivity some museums)", holidays);
	.send(Source, askOne, q2(Hotel)[o(holidays)], q2(Hotel)[o(holidays)]);
	.print(Hotel, " is located in a city with some museums");
	
	// bundle([h_1...h_n]) demonstrates how JASDL deals with nested se-content
	// in particular, this query bundles together an arbitrary number of queries into a single message
	.send(Source, askOne, bundle( [ luxuriousHotel(H1)[o(holidays)], hotel(H2)[o(holidays)], activity(H3)[o(holidays)] ] ), Response);	
	.print("Bundled query response: ", Response);	
	
	.send(Source, tell, example_KSAA_complete).

+all_different(XS)[o(holidays), source(Source)]
	<-
	.print(XS, " are mutually distinct individuals, according to ", Source).
	
	
