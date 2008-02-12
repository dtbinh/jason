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
!t1.
!t2.
!end.

@t1_s[atomic]
+!t1
	<-
	+yoga(t1_petes)[o(travel)];
	?yoga(t1_petes)[o(travel)];
	?relaxation(t1_petes)[o(travel)].
	
@t1_f[atomic]
-!t1
	<-
	failure("t1").

@t2_s[atomic]
+!t2
	<-
	+urbanArea(bristol)[o(travel)];
	?ruralArea(bristol)[o(travel)];
	failure("t2").

@end[atomic]	
+!end
	<-
	success.