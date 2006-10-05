// Agent maria in project Communication.mas2j

vl(1).
vl(2).

//!hello(maria).

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

/* When Bob asked me about t2(X), I do not have it my 
   belief base. So the event "+?t2(X)" is created and
   handled by the plan below.
*/
+?t2(X) : vl(Y) <- X = 10 + Y.

/* Plan below is used to customised the askOne performative.
   ask(and askAll) normally just consult the agent's belief base.
   with this customisation, the answer can be something
   not in belief base (e.g., the result of some operations).
   This customisation is applied only when the content
   of the askOne message is "fullname"
*/
+!kqmlReceived(Sender, askOne, fullname, ReplyWith) : true
   <- .send(Sender,tell,"Maria dos Santos", ReplyWith). // send the answer
   
// A plan to achieve the hello goal

+!hello(Who) <- .print("Hello ",Who); .wait(100); !!hello(Who).
