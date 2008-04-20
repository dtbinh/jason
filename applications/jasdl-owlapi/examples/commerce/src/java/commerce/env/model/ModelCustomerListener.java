package commerce.env.model;

/**
 * Classes interested in events from a ModelCustomer should implement this class and add themselves as a listener to ModelCustomer
 * @author tom
 *
 */
public interface ModelCustomerListener {
	
	/**
	 * Listener implementations should return true iff they approve of this brand as a purchase.
	 * All listeners must approve for the choice to be accepted
	 * @param brand
	 * @return
	 */	
	public boolean approve(String brand);
}
