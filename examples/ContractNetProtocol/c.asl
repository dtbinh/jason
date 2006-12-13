// Agent c in project ContractNetProtocol.mas2j
//
// This agent starts the protocol and plays the
// initiator role in it.
//

/* Initial beliefs and rules */

allProposalsReceived(CNPId) 
  :- .count(introduction(participant,_),NP) & // number of participants
     .count(propose(CNPId,_), NO) &           // number of proposes received
     .count(refuse(CNPId), NR) &              // number of refusals received
     NP = NO + NR.

/* Initial goals */

!startCNP(1).

/* Plans */

// start the CNP
+!startCNP(Id) 
   <- .wait(2000);  // wait participants presentation
      +cnpState(Id,propose);   // remember the state of the CNP
      .findall(VendorName,introduction(participant,VendorName),LV);
      .print("Sending CFP to ",LV);
      .send(LV,tell,cfp(Id,pc(3,[dual,ram(5,gb),hd(1,tb)])));
      .concat("+!contract(",Id,")",Event);
      // the deadline of the CNP is now + 4 seconds, so
      // the event +!contract(Id) is generated that time
      .at("now +4 seconds", Event).


// receive proposal 
// if all proposal are already received, do not wait fot the deadline
@r1 +propose(CNPId,Offer)
   :  cnpState(CNPId,propose) & allProposalsReceived(CNPId)
   <- !contract(CNPId).

// receive refusals   
@r2 +refuse(CNPId) 
   :  cnpState(CNPId,propose) & allProposalsReceived(CNPId)
   <- !contract(CNPId).

// this plan needs to be atomic to not accept 
// proposals or refusals while contracting
@lc1[atomic]
+!contract(CNPId)
   :  cnpState(CNPId,propose)
   <- -+cnpState(Id,contract);
      .findall(offer(O,A),propose(CNPId,O)[source(A)],L);
      .print("Offers are ",L);
      L \== []; // constraint the plan execution to one offer at least
      .sort(L,[offer(WOf,WAg)|_]); // sort offers, the first is the best
      .print("Winner is ",WAg," with ",WOf);
      !announceResult(CNPId,L,WAg);
      -+cnpState(Id,finished).

@lc2 +!contract(CNPId). // nothing todo, the last phase was not 'propose'

-!contract(CNPId)
   <- .print("CNP ",CNPId," has failed!").

+!announceResult(_,[],_).
// announce to the winner
+!announceResult(CNPId,[offer(O,WAg)|T],WAg) 
   <- .send(WAg,tell,acceptProposal(CNPId));
      !announceResult(CNPId,T,WAg).
// announce to others
+!announceResult(CNPId,[offer(O,LAg)|T],WAg) 
   <- .send(LAg,tell,rejectProposal(CNPId));
      !announceResult(CNPId,T,WAg).

