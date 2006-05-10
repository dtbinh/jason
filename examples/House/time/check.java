package time;

import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;

import java.util.Date;
import java.text.SimpleDateFormat;

public class check implements InternalAction {

    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
	String time = (new SimpleDateFormat("HH:mm")).format(new Date());
	return un.unifies((Term)args[0], new StringTermImpl("\""+time+"\""));
    }
}
