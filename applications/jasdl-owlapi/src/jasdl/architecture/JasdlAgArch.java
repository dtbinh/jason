/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package jasdl.architecture;

import jasdl.asSemantics.JasdlAgent;
import jasdl.util.exception.JasdlException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class JasdlAgArch extends AgArch {
	
	public static String NAMED_ANNOTATION_FUNCTOR = "named";
	public static String ANON_ANNOTATION_FUNCTOR = "anon";
	
	IncomingPropContProcessingStrategy incomingStrategy = new IncomingPropContProcessingStrategy();
	OutgoingPropContProcessingStrategy outgoingStrategy = new OutgoingPropContProcessingStrategy();


	@Override
	public void checkMail() {
		super.checkMail();		
		
		// temporary set to hold processed message clones
		Queue<Message> tempMail = new ConcurrentLinkedQueue<Message>();		
		Queue<Message> mail = getTS().getC().getMailBox();
		
		// mustn't affect original message object, otherwise effects are global (i.e. at infrastructure level) 
		for(Message message : mail){			
			try {
				Message clone = (Message)message.clone();
				processMessage(clone, incomingStrategy);
				tempMail.add(clone);
			} catch (JasdlException e) {
				// don't fail, just print warning
				getLogger().warning("Error processing incoming message "+message+". Reason: "+e);
			}
		}
		// need to affect original mail queue
		mail.clear();
		mail.addAll(tempMail);
		
		if(!mail.isEmpty()){
			getLogger().fine("Pending messages: "+mail);
		}
	}

	@Override
	public void sendMsg(Message msg) throws Exception {
		processMessage(msg, outgoingStrategy);
		getLogger().fine("Sending message: "+msg);
		super.sendMsg(msg);
	}
	
	@Override
	public void broadcast(Message msg) throws Exception {
		processMessage(msg, outgoingStrategy);
		getLogger().fine("Broadcasting message: "+msg);
		super.broadcast(msg);
	}	
	
	
	private Logger getLogger(){
		return getAgent().getLogger();
	}
	
	private JasdlAgent getAgent(){
		return (JasdlAgent)getTS().getAg();
	}
	
	private void processMessage(Message msg, PropContProcessingStrategy strategy) throws JasdlException{
		Object propcont = msg.getPropCont();
		if(propcont == null){
			return;
		}
		if(propcont instanceof Structure){ // only Structures require processing
			msg.setPropCont(processStructure((Structure)propcont, strategy, msg.getSender()));
		}
		
	}
	

	private Structure processStructure(Structure struct, PropContProcessingStrategy strategy, String src) throws JasdlException{
		
				
		
		// recurse down terms and lists (for "bundled" SE-enriched content)
		if(struct.getArity()>0){ // if we have any terms to process!
			// Process outer SE-enriched content
			if(struct instanceof Literal){ // processing strategies only apply to Literals
				Literal l = (Literal)struct;
				try{
					struct = strategy.process(l, getAgent(), src);
				}catch(JasdlException e){
					e.printStackTrace();
				}
			}	
			
			//int i=0;
			//for(Term _term : struct.getTerms()){ - mustn't use in case struct is a list		
			if(struct.isVar()){
				struct = (Structure)((VarTerm)struct).getValue(); // resolve struct if var
			}			
			if(struct.isList()){ // special processing for lists (since we can't (or rather shouldn't)) use getTerm or setTerm)
				ListTermImpl newList = new ListTermImpl(); // a "clone"
				for(Term e : ((ListTerm)struct).getAsList()){
					if(e instanceof Structure){
						Structure alteredTerm = (Structure)processStructure((Structure)e, strategy, src);
						newList.append(alteredTerm);
					}
				}
				return newList;
			}else{	// all other structures
				for(int i=0; i<struct.getArity(); i++){
					Term _term = struct.getTerm(i);
					if(_term instanceof Structure){
						Structure term = (Structure)_term;
						Structure alteredTerm = (Structure)processStructure(term, strategy, src);						
						struct.setTerm(i, alteredTerm); // modifies original struct
					}
				}
			}
		}
		
		
		
		return struct;
	}


}
