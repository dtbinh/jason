package commerce.exception;

import jason.JasonException;

public class CommerceException extends JasonException {

	public CommerceException() {
		super();
	}

	public CommerceException(String msg, Exception cause) {
		super(msg, cause);
	}


	public CommerceException(String msg) {
		super(msg);
	}
	
	
}
