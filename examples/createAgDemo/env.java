// Environment code for project createAgDemo.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

public class env extends Environment {

	private Logger logger = Logger.getLogger("createAgDemo.mas2j."+env.class.getName());

	public env() {
	}

	public boolean executeAction(String ag, Term action) {
		try {
			if (action.getFunctor().equals("stopMAS")) {
				getEnvironmentInfraTier().getRuntimeServices().stopMAS();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}

