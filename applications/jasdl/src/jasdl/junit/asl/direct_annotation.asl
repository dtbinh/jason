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
!t1_a.
!t1_b.
!t2_a.
!t2_b.
!end.

// demonstrates that adding sports(t1)[direct] functions correctly
// i.e. is NOT equivalent to saying Sports(t1) since uncapitalising transposition is prevented
@t1_a_s[atomic]
+!t1_a
	<-
	+sports(t1)[o(travel), direct];
	?sports(t1)[o(travel)];	
	failure("t1: transposition prevention on class assertion not functioning correctly").

// demonstrates t1_a expressed correctly (with transposition) working
@t1_b_s[atomic]	
+!t1_b
	<-
	+sports(t1)[o(travel)];
	?sports(t1)[o(travel)].

@t1_b_f[atomic]
-!t1_b
	<-
	failure("t1_b: simple concept assertion not functioning correctly").

// demonstrates transposition prevention using direct annotation functioning correctly
// for an individual whose name clashes with an uncapitalised class
@t2_a_s[atomic]
+!t2_a
	<-
	+nationalPark(nationalPark[direct])[o(travel)];
	?nationalPark(nationalPark[direct])[o(travel)].

@t2_a_f[atomic]
-!t2_a
	<-
	failure("t2_a: transposition preventon on individual not functioning correctly").


@end[atomic]	
+!end
	<-
	success.