// Agent monitor that just print the exchanged messages

// msgSent beliefs are stored in a database

+msgSent(Time,Id,Irt,Ilf,Sender,Receiver,Content)
   <- .print("Message ",Id," from ",Sender," to ",Receiver," = ", Content, " at ", Time).
