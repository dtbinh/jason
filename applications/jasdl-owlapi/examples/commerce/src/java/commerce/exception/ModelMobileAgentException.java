package commerce.exception;

import commerce.exception.ModelAgentException;

public class ModelMobileAgentException extends ModelAgentException {

	public ModelMobileAgentException() {
	}

	public ModelMobileAgentException(String msg, Exception cause) {
		super(msg, cause);
	}

	public ModelMobileAgentException(String msg) {
		super(msg);
	}

}
