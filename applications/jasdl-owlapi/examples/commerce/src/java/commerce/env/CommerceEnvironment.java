package commerce.env;
// Environment code for project commerce.mas2j

import static jasdl.util.Common.getCurrentDir;
import jasdl.bridge.factory.AliasFactory;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.bridge.mapping.aliasing.DecapitaliseMappingStrategy;
import jasdl.bridge.mapping.aliasing.MappingStrategy;
import jasdl.env.JASDLEnvironment;
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
public class CommerceEnvironment extends JASDLEnvironment {


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
    	
    	model = new CommerceModel(new Dimension(20, 20), this);
    	view = new CommerceView(model);
    	
    	try {
			getJasdlOntologyManager().loadOntology(c, URI.create("file://"+getCurrentDir()+"/onts/commerce.owl"), mappingStrategies);
			getJasdlOntologyManager().loadOntology(s, URI.create("file://"+getCurrentDir()+"/onts/society.owl"), mappingStrategies);
			
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

