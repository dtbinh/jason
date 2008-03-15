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
package jasdl.test;

// Environment code for project jasdl

import jason.asSyntax.Structure;
import jason.environment.Environment;

public class TestEnv extends Environment {
	
	private boolean success;
	private boolean failure;
	private String failureMsg;


    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
    	success = false;
    	failure = false;
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        if(action.getFunctor().equals("success") && !failure){
        	success = true;
        }
        if(action.getFunctor().equals("failure") && !success){
        	failureMsg = action.getTerm(0).toString();
        	failure = true;
        }
        return true;
    }
    
    

    public String getFailureMsg() {
		return failureMsg;
	}

	/** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }

	public boolean isSuccess() {
		return success;
	}

	public boolean isFailure() {
		return failure;
	}
    
    
}
