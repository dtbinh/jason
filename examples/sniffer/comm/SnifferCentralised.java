package comm;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.MsgListener;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** 
 * Customisation of an agent architecture to sniff the MAS with 
 * Centralised infrastructure. 
 * 
 * @author Jomi
 */
public class SnifferCentralised extends AgArch implements MsgListener {

    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
    
        if (getArchInfraTier() instanceof CentralisedAgArch) {
			CentralisedAgArch.addMsgListener(this);
		}
    }
    
    // method called-back when some message is exchanged
    public void msgSent(Message m) {
        //getTS().getLogger().fine("Message:"+m);
	
		// add a belief in the agent mind 
		// format: msgSent(time(YY,MM,DD,HH,MM,SS),id,irt,ilf,sender,receiver,content)
		Literal e = new Literal("msg_sent");

        Calendar now = new GregorianCalendar();
		Structure p = new Structure("time");
		p.addTerm(new NumberTermImpl(now.get(Calendar.YEAR)));
		p.addTerm(new NumberTermImpl(now.get(Calendar.MONTH)));
		p.addTerm(new NumberTermImpl(now.get(Calendar.DAY_OF_MONTH)));
		p.addTerm(new NumberTermImpl(now.get(Calendar.HOUR)));
		p.addTerm(new NumberTermImpl(now.get(Calendar.MINUTE)));
		p.addTerm(new NumberTermImpl(now.get(Calendar.SECOND)));
		e.addTerm(p);
	
	
		e.addTerm(new StringTermImpl(m.getMsgId()));
		if (m.getInReplyTo() == null) {
			e.addTerm(new Atom("nirt"));
		} else {
			e.addTerm(new StringTermImpl(m.getInReplyTo()));
		}
		e.addTerm(new Atom(m.getIlForce()));
		e.addTerm(new Atom(m.getSender()));
		e.addTerm(new Atom(m.getReceiver()));
		e.addTerm(new StringTermImpl(m.getPropCont().toString()));
		getTS().getAg().addBel(e);
    }    
}