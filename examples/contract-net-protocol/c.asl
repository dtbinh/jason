/* Initial beliefs and rules */

all_proposals_received(CNPId) 
  :- .count(introduction(participant,_),NP) & // number of participants
     .count(propose(CNPId,_), NO) &           // number of proposes received
     .count(refuse(CNPId), NR) &              // number of refusals received
     NP = NO + NR.

/* Initial goals */

!startCNP(1,fix(computer)).

/* Plans */

// start the CNP
+!startCNP(Id,Task) 
   <- .wait(2000);  // wait participants introduction
      +cnp_state(Id,propose);   // remember the state of the CNP
      .findall(VendorName,introduction(participant,VendorName),LV);
      .print("Sending CFP to ",LV);
      .send(LV,tell,cfp(Id,Task));
      .concat("+!contract(",Id,")",Event);
      // the deadline of the CNP is now + 4 seconds, so
      // the event +!contract(Id) is generated that time
      .at("now +4 seconds", Event).


// receive proposal 
// if all proposal are already received, do not wait fot the deadline
@r1 +propose(CNPId,Offer)
   :  cnp_state(CNPId,propose) & all_proposals_received(CNPId)
   <- !contract(CNPId).

// receive refusals   
@r2 +refuse(CNPId) 
   :  cnp_state(CNPId,propose) & all_proposals_received(CNPId)
   <- !contract(CNPId).

// this plan needs to be atomic to not accept 
// proposals or refusals while contracting
@lc1[atomic]
+!contract(CNPId)
   :  cnp_state(CNPId,propose)
   <- -+cnp_state(CNPId,contract);
      .findall(offer(O,A),propose(CNPId,O)[source(A)],L);
      .print("Offers are ",L);
      L \== []; // constraint the plan execution to one offer at least
      .min(L,offer(WOf,WAg));
      .print("Winner is ",WAg," with ",WOf);
      !announce_result(CNPId,L,WAg);
      -+cnp_state(Id,finished).

@lc2 +!contract(CNPId). // nothing todo, the last phase was not 'propose'

-!contract(CNPId)
   <- .print("CNP ",CNPId," has failed!").

+!announce_result(_,[],_).
// announce to the winner
+!announce_result(CNPId,[offer(O,WAg)|T],WAg) 
   <- .send(WAg,tell,accept_proposal(CNPId));
      !announce_result(CNPId,T,WAg).
// announce to others
+!announce_result(CNPId,[offer(O,LAg)|T],WAg) 
   <- .send(LAg,tell,reject_proposal(CNPId));
      !announce_result(CNPId,T,WAg).

