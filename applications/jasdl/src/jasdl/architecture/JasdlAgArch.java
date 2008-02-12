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

import static jasdl.util.Common.DEFINED_BY_ANNOTATION;
import static jasdl.util.Common.EXPR_ANNOTATION;
import static jasdl.util.Common.ONTOLOGY_ANNOTATION;
import static jasdl.util.Common.getAnnot;
import static jasdl.util.Common.hasAnnot;
import static jasdl.util.Common.strip;
import jasdl.ontology.Alias;
import jasdl.ontology.DefinedAlias;
import jasdl.ontology.JasdlOntology;
import jasdl.ontology.OntologyManager;
import jasdl.util.JasdlException;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Queue;
import java.util.logging.Logger;


/**
 * TODO: Requires optimisation (re-use of ontology annotations, etc). Left as is for clarity.
 * 
 * @author Tom Klapiscak
 *
 */
public class JasdlAgArch extends AgArch {
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger("arch init");	
	
	private OntologyManager manager;
	
	public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
		super.initAg(agClass, bbPars, asSrc, stts);
		manager = OntologyManager.getOntologyManager(getTS().getAg());
		logger = Logger.getLogger(this.getAgName());	
	}
	
	/**
	 * Translates alias->physical namespace for outgoing messages
	 * Adds expr annotation containing class expression referred to by alias
	 */
	public void sendMsg(Message m) throws Exception{	
		m.setPropCont(prepareOutgoingMessageContent(m.getPropCont()));
		manager.getLogger().finest("Sending message: "+m);
		super.sendMsg(m);
	}
	
	/**
	 * Translates alias->physical namespace for outgoing messages
	 * Adds expr annotation containing class expression referred to by alias
	 */	
	public void broadcast(Message m) throws Exception{
		m.setPropCont(prepareOutgoingMessageContent(m.getPropCont()));
		manager.getLogger().finest("Broadcasting message: "+m);
		super.broadcast(m);
	}
	
	/**
	 * Recursively prepares annotations and terms of a nesting of semantic literals for sending to another agent
	 * 
	 * Note: Sending with an unground ontology annotation will cause it to be grounded (if possible?)
	 * @param __propcont
	 * @return
	 * @throws JasdlException
	 */
	private Object prepareOutgoingMessageContent(Object _propcont) throws JasdlException{
		if(_propcont == null){
			return null;
		}			
		if(!(_propcont instanceof Structure)){ // not a structure, doesn't require any processing
			return _propcont;
		}
		Structure propcont = (Structure)_propcont;
		logger.finest("processing structure: "+_propcont+" (arity "+propcont.getArity()+")");
		Term ontologyAnnotation = getAnnot(propcont, ONTOLOGY_ANNOTATION);	
		if(ontologyAnnotation != null){ // we have a semantically enriched literal		
			prepareOutgoingMessageAnnotations((Literal)_propcont);
		}		
		return prepareOutgoingMessageTerms(propcont);		
	}
	
	/**
	 * 
	 * @param struct the structure to be affected - note: pass by reference, not copy!
	 * @return
	 * @throws JasdlException
	 */
	private Term prepareOutgoingMessageTerms(Structure struct) throws JasdlException{
		if(struct.getArity()>0){ // if we have any terms to process!
			//int i=0;
			//for(Term _term : struct.getTerms()){ - mustn't use in case struct is a list		
			if(struct.isVar()){
				struct = (Structure)((VarTerm)struct).getValue(); // resolve struct if var
			}			
			if(struct.isList()){ // special processing for lists (since we can't (or rather shouldn't) use getTerm or setTerm)
				ListTermImpl newList = new ListTermImpl(); // a "clone"
				for(Term e : ((ListTerm)struct).getAsList()){
					logger.finest("processing term: "+e+" in "+struct);
					if(e instanceof Structure){
						Structure alteredTerm = (Structure)prepareOutgoingMessageContent((Structure)e);
						logger.finest("altered: "+alteredTerm);
						newList.append(alteredTerm);
					}
				}
				return newList;
			}else{	// all other structures
				for(int i=0; i<struct.getArity(); i++){
					Term _term = struct.getTerm(i);
					if(_term instanceof Structure){
						Structure term = (Structure)_term;
						Structure alteredTerm = (Structure)prepareOutgoingMessageContent(term);						
						struct.setTerm(i, alteredTerm); // modifies original struct
					}
				}
			}
		}
		return struct;
	}
	
	/**
	 * Prepares outer annotations of a semantically enriched literal by:
	 * 1. setting the ontology alias to its physical namespace uri
	 * 2. adding expr and defined_by annotations as appropriate (behaviour depends on whether or not this literal corresponds to a runtime defined class)
	 * @param literal	the literal to be affected - note: pass by reference, not copy!
	 * @return			the affected literal (for convenience only since parameter is passed by reference)
	 * @throws JasdlException
	 */
	private Literal prepareOutgoingMessageAnnotations(Literal literal) throws JasdlException{

		JasdlOntology ont = manager.getJasdlOntology(literal);

		
		Structure ontologyAnnotation = getAnnot(literal, ONTOLOGY_ANNOTATION);
		
		// set ontology alias to physical namespace
		ontologyAnnotation.setTerm(0, new StringTermImpl(ont.getPhysicalNs().toString()));
		
		// add expression annotation
		Alias alias = ont.getAliasFromPred(literal);
		String expr = null;
		if(alias instanceof DefinedAlias){
			// we have a runtime defined class: expression is full (postcompiled) class expression and we need to add defined_by annotation
			expr = ((DefinedAlias)alias).getExpr();					
			literal.addAnnot(Literal.parseLiteral(DEFINED_BY_ANNOTATION+"("+((DefinedAlias)alias).getDefinedBy()+")"));// need to check its present? nah			
		}else{
			expr = ont.transposeToReal(alias).toString();
		}
		literal.addAnnot(Literal.parseLiteral(EXPR_ANNOTATION+"(\""+expr+"\")"));
		
		return literal;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Cloning necessary so as to not affect global mail box
	 */
	public void checkMail(){
		super.checkMail();
		
		Queue<Message> mail = getTS().getC().getMailBox();
		HashSet<Message> messages = new HashSet<Message>();
		messages.addAll(mail);
		mail.clear();
		
		for(Message message : messages){			
			try {
				Message clone = (Message)message.clone();
				clone.setPropCont(prepareIncomingMessageContent(clone.getPropCont()));
				mail.add(clone);
			} catch (JasdlException e) {
			}
		}
		
		if(!messages.isEmpty()){
			manager.getLogger().finest("Pending messages: "+mail);
		}
		
	}
	
	
	
	/**
	 * DOES NOT side-affect referenced message object
	 * @param _propcont
	 * @return
	 * @throws JasdlException
	 */
	private Object prepareIncomingMessageContent(Object _propcont) throws JasdlException{
		if(_propcont == null){
			return null;
		}
		if(!(_propcont instanceof Structure)){ // not a structure, doesn't require any processing
			return _propcont;
		}
		Structure propcont = (Structure)((Structure)_propcont).clone();
		manager.getLogger().finest("preparing incoming structure: "+_propcont+" (arity "+propcont.getArity()+")");
		Term ontologyAnnotation = getAnnot(propcont, ONTOLOGY_ANNOTATION);	
		if(ontologyAnnotation != null){ // we have a semantically enriched literal		
			prepareIncomingMessageAnnotations((Literal)propcont);
		}		
		return prepareIncomingMessageTerms(propcont);// recursively process terms			
	}	
	
	
	
	/**
	 * Prepares outer annotations of an incoming semantically enriched literal by:
	 * 1. setting the physical namespace uri to ontology alias, instantiating ontology if unknown
	 * @param literal	the literal to be affected - note: pass by reference, not copy!
	 * @return			the affected literal (for convenience only since parameter is passed by reference)
	 * @throws JasdlException
	 */
	private Literal prepareIncomingMessageAnnotations(Literal literal) throws JasdlException{
		//JasdlOntology ont = manager.getJasdlOntology(literal);
		Structure ontologyAnnotation = getAnnot(literal, ONTOLOGY_ANNOTATION);
		
		Structure exprAnnot = getAnnot(literal, EXPR_ANNOTATION);
		
		if(exprAnnot == null){
			return literal; // already processed since it lacks expr annotation
		}
		
		Term physicalNs = ontologyAnnotation.getTerm(0);
		if(!physicalNs.isString()){
			//return literal;
			throw new JasdlException("Incoming ontology annotation must contain a string representing a physical namespace URI");
		}
		
		
		URI uri;
		try {
			uri = new URI(strip(physicalNs.toString(), "\""));
		} catch (URISyntaxException e) {
			throw new JasdlException("Invalid physical namespace URI: "+physicalNs+". Reason: "+e);
		}	
		JasdlOntology ont = manager.getJasdlOntology(uri);
		if(ont == null){ // (1) attempt to instantiate unknown ontology
			ont = manager.createJasdlOntology(uri);							
		}
		
		
		// (2) substitute physical namespace for aliases
		ontologyAnnotation.setTerm(0, new Atom(ont.getAlias()));
		
		// process expression annotation, instantating any new concepts and mapping predefined ones
		if(exprAnnot==null){
			throw new JasdlException("Incoming semantically enriched propositional content must be annotation with a valid class expression");
		}

		Term _expr = exprAnnot.getTerm(0);
		if(!_expr.isString()){
			throw new JasdlException("Invalid expression annotation argument: "+_expr);
		}
		String expr = strip(_expr.toString(), "\"");
		
		// remove the unneeded expr annotation
		literal.delAnnot(exprAnnot);
		
		String entityAlias = null;
		// just need to check for defined_by annotation!
		// assume that defined_by term is truthful, or use sender name as defined_by?							
		if(hasAnnot(literal, DEFINED_BY_ANNOTATION)){ // we have a runtime defined class expression
			entityAlias = literal.getFunctor();// take on their functor as alias
			String definedBy = ont.getDefinedBy(literal);
			ont.defineClass(entityAlias, expr, definedBy); // very trusting!								
		}else{ // we have a base class reference in expression
			// take on my alias as functor								
			entityAlias = ont.transposeToAlias(URI.create(expr)).getName();
		}
		
		
		Literal newLiteral = new Literal(literal.negated(), entityAlias);
		// add all annotations (minus expr, o modified)
		newLiteral.addAnnots(literal.getAnnots());
		
		
		return newLiteral;
	}	
	
	
	
	/**
	 * 
	 * @param struct the structure to be affected - note: pass by reference, not copy!
	 * @return
	 * @throws JasdlException
	 */
	private Term prepareIncomingMessageTerms(Structure struct) throws JasdlException{
		if(struct.getArity()>0){ // if we have any terms to process!
			//int i=0;
			//for(Term _term : struct.getTerms()){ - mustn't use in case struct is a list		
			if(struct.isVar()){
				struct = (Structure)((VarTerm)struct).getValue(); // resolve struct if var
			}			
			if(struct.isList()){ // special processing for lists (since we can't getTerm or setTerm)
				ListTermImpl newList = new ListTermImpl(); // a "clone"
				for(Term e : ((ListTerm)struct).getAsList()){
					logger.finest("processing term: "+e+" in "+struct);
					if(e instanceof Structure){
						Structure alteredTerm = (Structure)prepareIncomingMessageContent((Structure)e);
						logger.finest("altered: "+alteredTerm);
						newList.append(alteredTerm);
					}
				}
				return newList;
			}else{	// all other structures
				for(int i=0; i<struct.getArity(); i++){
					Term _term = struct.getTerm(i);
					if(_term instanceof Structure){
						Structure term = (Structure)_term;
						Structure alteredTerm = (Structure)prepareIncomingMessageContent(term);						
						struct.setTerm(i, alteredTerm); // modifies original struct
					}
				}
			}
		}
		return struct;
	}		
	
	
}