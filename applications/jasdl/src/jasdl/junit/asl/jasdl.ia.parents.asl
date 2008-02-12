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

@t1[atomic]
+!t1
	<-
	jasdl.ia.parents(P1, beach, travel);
	.member(destination, P1);
	.member(thing, P1);
	
	jasdl.ia.parents(P2, beach, travel, false);
	.member(destination, P2);
	.member(thing, P2);	

	jasdl.ia.parents(P3, beach, travel, true);
	.member(destination, P3);
	
	success.