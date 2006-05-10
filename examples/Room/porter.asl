+!locked(door)[source(paranoid)]
  : ~locked(door)
  <- lock.
  
+!unlocked(door)[source(claustrophobe)]
  : locked(door)
  <- unlock.
