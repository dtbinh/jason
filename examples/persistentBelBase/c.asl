!start. // initial goal.

+!start : not a(_) 
  <- .print("First run."); 
     +a(1).
     
+!start : a(X) 
  <- -+a(X+1); // a is stored in a DB
     .print("Not first run, I already run ",X," times.");
     !show_book(X).

+!show_book(X) 
  :  Id = (X mod 2)+1 &
     book(Id, Title, PubId, Year, ISBN) & publisher(PubId, Publisher)
     // book and publisher are tables in a DB
  <- .print(Title, ". ", Publisher, ", ",Year, ". (ISBN ",ISBN,")");
     .findall(Author, book_author(Id,Author), LA);
     .print("Authors: ");
     !show_authors(LA).
       
+!show_authors([]).
+!show_authors([A|T])
  :  author(A,Name)
  <- .print("          ",Name);
     !show_authors(T).
+!show_authors([A|T])
  <- !show_authors(T).

