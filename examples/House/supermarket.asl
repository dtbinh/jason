// supermarket in project House.mas2j

+order(P,N)[source(Ag)] : true
  <- deliver(P,N,Ag);
     .send(Ag, tell, delivered(P,N));
     -order(P,N).

