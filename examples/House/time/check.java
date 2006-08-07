package time;

import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;

import java.util.Date;
import java.text.SimpleDateFormat;

public class check implements InternalAction {

    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String time = (new SimpleDateFormat("HH:mm:ss")).format(new Date());
        ts.getLogger().info("Check Time="+time);
        return un.unifies(args[0], new StringTermImpl(time));
    }
}

