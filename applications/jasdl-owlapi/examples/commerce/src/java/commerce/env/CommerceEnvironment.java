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
package commerce.env;
// Environment code for project commerce.mas2j

import static jasdl.util.JASDLCommon.getCurrentDir;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.mapping.aliasing.DecapitaliseMappingStrategy;
import jasdl.bridge.mapping.aliasing.MappingStrategy;
import jasdl.env.JASDLEnvironment;
import jasdl.util.JASDLCommon;
import jasdl.util.exception.JASDLException;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import commerce.env.model.CommerceModel;
import commerce.env.model.ModelAgent;
import commerce.env.model.ModelCustomer;
import commerce.env.model.ModelDeliveryVan;
import commerce.env.model.ModelObject;
import commerce.env.model.ModelShop;
import commerce.exception.ModelAgentException;



/**
 * 
 * 
 * @author tom
 *
 */
public class CommerceEnvironment extends JASDLEnvironment{


	public Atom s = new Atom("s");
	public Atom c = new Atom("c");
	private List<MappingStrategy> mappingStrategies = Arrays.asList( new MappingStrategy[] { new DecapitaliseMappingStrategy()} );
	
	private Alias inVicinityOf = AliasFactory.INSTANCE.create(new Atom("inVicinityOf"), s);
	
	private CommerceModel model;
	private CommerceView view;

    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
    	super.init(args);
    	
    	System.out.println("Agents loading... please wait.");
    	
    	model = new CommerceModel(new Dimension(20, 20), this);
    	
    	view = new CommerceView(model, this);
    	   
    	// for each customer set-up a UI Panel
    	for(ModelObject o : model.getObjects()){
    		if(o instanceof ModelCustomer){
    			view.addCustomer((ModelCustomer)o);
    		}
    	}
    	
    	try {
			getJom().loadOntology(c, JASDLCommon.getRelativeLocalURI("/onts/commerce.owl"), mappingStrategies);
			getJom().loadOntology(s, JASDLCommon.getRelativeLocalURI("/onts/society.owl"), mappingStrategies);
			
		} catch (JASDLException e) {
			e.printStackTrace();
		}
		
    	    	
    	updatePercepts();
    	
    }   
    

    private void updatePercepts(){
    	synchronized(model.getObjects()){ 
	    	try{    	  	
	    		model.updateObjects();	    		
	    			
		    	for(ModelObject o : model.getObjects()){
		    		if(o instanceof ModelAgent){
		    			clearPercepts(o.getId().toString());	
		    		}
		    	}
		    	clearPercepts();
		    	
		    	// Add individual percepts for each ModelAgent (determined by ModelAgent#getPercepts)
		    	for(ModelObject o : model.getObjects()){
		    		if(o instanceof ModelAgent){
		    			ModelAgent agent = (ModelAgent)o;
		    			agent.addPercepts();
		    		}
		    	}   
		
		    	
	    	}catch(JASDLException e){
	    		e.printStackTrace();
	    		
	    	}
    	}    	
    }

    
    
    
    
	@Override
	public List<Literal> getPercepts(String agName) {
		synchronized (model.getObjects()) {
			return super.getPercepts(agName);
		}		
	}


	/* NOTE: ADDING SE-PERCEPTS DIRECTLY BETWEEN AGENTS CIRCUMVENTS SYNTACTIC-TRANSLATION AND SO CANNOT BE PERFORMED HERE */
    @Override
    public boolean executeAction(String agName, Structure action) {
    	Logger agentLogger = Logger.getLogger(agName);
    	try{
    		Term[] terms = new Term[action.getTerms().size()];
    		int i=0;
    		for(Term term : action.getTerms()){
    			if(term instanceof VarTerm && term.isGround()){
    				terms[i] = ((VarTerm)term).getValue();
    			}else{
    				terms[i] = term;
    			}
    			i++;
    		}
    		
	    	ModelAgent agent = (ModelAgent)model.getObjectById(new Atom(agName));    	
	    	
	    	// Actions that can be performed by delivery vans
	    	if(agent instanceof ModelDeliveryVan){
	    		ModelDeliveryVan van = (ModelDeliveryVan)agent;
	    		if(action.getFunctor().equals("load")){
	    			van.load((Atom)terms[0]);
	    		}else if(action.getFunctor().equals("unload")){
	    			van.unload((Atom)terms[0]);			
	    		}else if(action.getFunctor().equals("move_towards")){
	    			van.moveTowards(
	    					new Point(
	    							(int)((NumberTermImpl)terms[0]).solve(),
	    							(int)((NumberTermImpl)terms[1]).solve()));
	    		}else{
	    			throw new ModelAgentException("Unknown action");
	    		}
	    	}
	    	
	    	if(agent instanceof ModelShop){
	    		ModelShop shop = (ModelShop)agent;
	    		if(action.getFunctor().equals("deploy")){
	    			shop.deploy((Atom)terms[0], shop.getProductByBrand(terms[1].toString()), (int)((NumberTerm)terms[2]).solve());
	    		}
	    	}
	    	
	    	if(agent instanceof ModelCustomer){
	    		ModelCustomer customer = (ModelCustomer)agent;
	    		if(action.getFunctor().equals("request_product")){
	    			customer.request(terms[0].toString(), terms[1].toString(), (int)((NumberTerm)terms[2]).solve());
	    		}
	    		if(action.getFunctor().equals("confirm_order")){
	    			customer.confirm_order();
	    		}
	    		if(action.getFunctor().equals("approve")){
	    			return customer.approve(terms[0].toString());
	    		}
	    		if(action.getFunctor().equals("message")){
	    			customer.message(terms[0].toString());
	    		}
	    	}
	    	
	    	
	    	
    	}catch(ModelAgentException e){
    		agentLogger.info("Unable to complete action "+action+". Reason: ");
    		e.printStackTrace();    		
    		return false;    
    	}finally{
    		updatePercepts();
    	}
		agentLogger.fine("Completed action "+action);
		return true;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
    
}

