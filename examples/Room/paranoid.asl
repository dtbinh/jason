+~locked(door) : true
  <- -~locked(door); // remove my belief about the door state
     .send(porter,achieve,locked(door)). // ask porter to lock

//+locked(door) : true
//  <- .print("Thanks for locking the door!").
  
