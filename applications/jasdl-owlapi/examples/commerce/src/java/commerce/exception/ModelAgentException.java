package commerce.exception;

public class ModelAgentException extends CommerceException{

	public ModelAgentException() {
		super();
	}

	public ModelAgentException(String msg, Exception cause) {
		super(msg, cause);
	}
	
	public ModelAgentException(String msg) {
		super(msg);
	}

}
