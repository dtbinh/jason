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
 
hasAccommodation(butlins, butlins_hotel)[o(travel)].
hasActivity(butlins, swimming)[o(travel)].
hasActivity(butlins, tennis)[o(travel)].
hotel(butlins_hotel)[o(travel)].
activity(tennis)[o(travel)].
activity(swimming)[o(travel)].
destination(butlins)[o(travel)].

!t1.
!t2.

// due to cardinality constraint that a familyDestination must have min 2 activities, this test will fail unless
// tennis and swimming are sucessfully explicitly specified to be different individuals
@t1[atomic]
+!t1
	<-
	?familyDestination(X)[o(travel)];
	failure("").
	
@t2[atomic]
+!t2
	<-
	jasdl.ia.all_different([tennis, swimming], travel);
	?familyDestination(X)[o(travel)];
	success("").