// supermarket in project House.mas2j

/* Initial beliefs and rules */

last_order_id(1).


/* Plans */

+!order(Product,Qtd)[source(Ag)] 
  :  true
  <- deliver(Product,Qtd);
     ?last_order_id(N);
     OrderId = N + 1;
     -+last_order_id(OrderId);
     .send(Ag, tell, delivered(Product,Qtd,OrderId)).

