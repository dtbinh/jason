+locked(door) : true
  <- -locked(door); // remove my belief about the door state
     .send(porter,achieve,unlocked(door)). // ask porter to unlock
 
//-locked(door) : true
//  <- .print("Thanks for unlocking the door!").

