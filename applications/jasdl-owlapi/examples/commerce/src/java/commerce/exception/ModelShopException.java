package commerce.exception;

public class ModelShopException extends ModelAgentException {

	public ModelShopException() {
	}

	public ModelShopException(String msg, Exception cause) {
		super(msg, cause);
	}

	public ModelShopException(String msg) {
		super(msg);
	}

}
