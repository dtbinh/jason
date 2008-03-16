package arch;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/** 
 * 
 * Jason agent architecture customisation 
 * (it links the AgentSpeak interpreter to the contest simulator)
 * 
 * @author Jomi
 *
 */
public class ACArchitecture extends CowboyArch {

	private Logger logger;	

	private ACProxy       proxy;
	private List<Literal> percepts = new ArrayList<Literal>();
	
	@Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
		super.initAg(agClass, bbPars, asSrc, stts);
		logger = Logger.getLogger(ACArchitecture.class.getName()+"."+getAgName());

		String username = stts.getUserParameter("username");
        if (username.startsWith("\"")) username = username.substring(1,username.length()-1);
        String password = stts.getUserParameter("password");
        if (password.startsWith("\"")) password = password.substring(1,password.length()-1);
        
		proxy = new ACProxy( 	this, 
								stts.getUserParameter("host"), 
								Integer.parseInt(stts.getUserParameter("port")),
								username,
								password);
		new Thread(proxy,"AgentProxy"+username).start();
	}

	
	@Override
	public void stopAg() {
	    super.stopAg();
	    proxy.finish();
	}
	
	@Override
	public List<Literal> perceive() {
		return new ArrayList<Literal>(percepts);
	}

	Queue<ActionExec> toExecute = new ConcurrentLinkedQueue<ActionExec>();
    
	public void startNextStep(int step, List<Literal> p) {
		percepts = p;

		// set all actions as successfully executed
		List<ActionExec> feedback = getTS().getC().getFeedbackActions();
		while (!toExecute.isEmpty()) {
    		ActionExec action = toExecute.poll();
    		action.setResult(true);
			feedback.add(action);
		}
		
		getTS().getUserAgArch().getArchInfraTier().wake();
    	setCycle(step);
	}
	
	@Override
	public void act(final ActionExec act, List<ActionExec> feedback) {
        final Structure acTerm = act.getActionTerm();
        if (acTerm.getFunctor().equals("do")){
        	// (to not block the TS)
        	// TODO: use a thread pool
        	new Thread() {
        		public void run() {
                    proxy.sendAction(acTerm.getTerm(0).toString());
                    toExecute.offer(act);
        		}
        	}.start();
        } else {
        	logger.info("ignoring action "+acTerm+", it is not a 'do'.");
        }
	}
	
    // TODO: create a new agent and plug it on the connection
	
	/** this method is called when the agent crashes and other approaches to fix it (fix1 and fix2) does not worked */
    /*
	@Override
    protected boolean fix3() throws Exception {
        getTS().getLogger().warning("Cloning!");
        
        RuntimeServicesInfraTier services = getArchInfraTier().getRuntimeServices();

        // really stops the agent (since stop can block, use a thread to run it)
        new Thread() {   public void run() {
            getArchInfraTier().stopAg();
        }}.start();
        
        // create a new overall agent (arch, thread, etc.)
        ChangeArchFixer arch = (ChangeArchFixer)services.clone(getTS().getAg(), this.getClass().getName(), getTS().getUserAgArch().getAgName()+"_clone");
        arch.processParameters();
        arch.createCheckThread();
        arch.getTS().getC().create(); // use a new C.
        
        //arch.getTS().getLogger().info("Cloned!");
        
        // just to test, add !start
        arch.getTS().getC().addAchvGoal(Literal.parseLiteral("start"), Intention.EmptyInt);
        return false;
    }
     */

}
