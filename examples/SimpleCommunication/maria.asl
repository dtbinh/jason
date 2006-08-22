// Agent maria in project Communication.mas2j

vl(1).
vl(2).

/* Plan triggered when a tell message is received.
   It is like the belief addition, but with a source
   that is not self.
*/
+vl(X)[source(Ag)] 
   :  Ag \== self
   <- .print("Received tell ",vl(X)," from ", Ag).
   
/* Plan triggered when an achieve message is received.
   It is like a new goal, but with a different source.
*/
+!goto(X,Y)[source(Ag)] : true
   <- .print("Received achieve ",goto(X,Y)," from ", Ag).

/* Plan used to customised the ask performative.
   Ask(all) normally just consult the agent's belief base.
   with this customisation, the answer can be something
   not in belief base (the result of some operations).
   This customisation is applied only when the content
   of the ask message is "fullname"
*/
+!received(Sender, ask, fullname, ReplyWith) : true
   <- .send(Sender,tell,"Maria dos Santos", ReplyWith). // send the answer
   
