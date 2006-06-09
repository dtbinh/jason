demo.
+demo : not a(_) 
  <- .print("First run."); 
     +a(1).
     
+demo : a(X) 
  <- -+a(X+1); // a is stored in a DB
     .print("Not first run, I already run ",X," times.");
     !showBook(X).

+!showBook(X) 
  :  Id = (X mod 2)+1 &
     book(Id, Title, PubId, Year, ISBN) & publisher(PubId, Publisher)
     // book and publisher are tables in a DB
  <- .print(Title, ". ", Publisher, ", ",Year, ". (ISBN ",ISBN,")");
     .findall(Author, book_author(Id,Author), LA);
     .print("Authors: ");
     !showAuthors(LA).
       
+!showAuthors([]).
+!showAuthors([A|T])
  :  author(A,Name)
  <- .print("          ",Name);
     !showAuthors(T).
+!showAuthors([A|T])
  <- !showAuthors(T).

