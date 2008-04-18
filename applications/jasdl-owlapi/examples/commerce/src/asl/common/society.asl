/* 
 * PA is employed by a single customer (enforced by the fact that employs is inverse functional - and isEmployedBy is functional)
 * Unfortunately, since employs has an inverse property, it cannot also be transitive
 */
 
 // Using SE-Literals in rules.
 // In this case essentially grounding the domain of an object property with a commonly used value (i.e. my name)
isEmployedBy(Employer) :- .my_name(Me) & isEmployedBy(Me, Employer)[o(s)].
employs(Employee) :- .my_name(Me) & employs(Me, Employee)[o(s)].
