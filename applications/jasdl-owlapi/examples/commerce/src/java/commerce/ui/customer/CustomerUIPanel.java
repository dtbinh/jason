package commerce.ui.customer;

import jasdl.util.JASDLCommon;
import jason.asSyntax.Literal;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import commerce.env.CommerceEnvironment;
import commerce.env.model.ModelCustomer;
import commerce.env.model.ModelCustomerListener;

public class CustomerUIPanel extends JPanel implements ModelCustomerListener{
	private JTextField productDescriptionField;
	private JButton submitButton;
	private ModelCustomer customer;
	private CommerceEnvironment env;
	private JSpinner qty;
	private JTextField shopDescriptionField;
	private JButton confirmButton;
	private JList orders;
	private DefaultListModel ordersModel;
	private Object lastQty;
	
	private List<ExampleRequest> examples = Arrays.asList(new ExampleRequest[] {
			new ExampleRequest("c:bread", "{s:shop1}", "Bread from shop2 (will propagate to shop1)")
		});
	private JTextArea englishDescriptionField;
	
	
	public CustomerUIPanel(CommerceEnvironment env, ModelCustomer customer){
		super();
		this.customer = customer;
		
		customer.addListener(this);
		
		this.env = env;
		setupUI();
	}
	
	
	private void setupUI(){		
		JPanel productRequestPane = new JPanel();						
			//productRequestPane.setPreferredSize(new Dimension(400, 300));
			productRequestPane.setLayout(new BoxLayout(productRequestPane, BoxLayout.Y_AXIS));
			add(productRequestPane);
			
			JPanel productDescriptionFieldPane = new JPanel();
				productDescriptionFieldPane.setPreferredSize(new Dimension(300, 65));
				productDescriptionFieldPane.setBorder(BorderFactory.createTitledBorder("Product Description"));
				productRequestPane.add(productDescriptionFieldPane);
				
				productDescriptionField = new JTextField(20);	
					//productDescriptionField.setSize(new Dimension(280, 80));
					//productDescriptionField.setMaximumSize(new Dimension(280, 80));
					productDescriptionFieldPane.add(productDescriptionField);
			
			JPanel shopDescriptionFieldPane = new JPanel();
				shopDescriptionFieldPane.setPreferredSize(new Dimension(300, 65));
				shopDescriptionFieldPane.setBorder(BorderFactory.createTitledBorder("Shop Description"));
				productRequestPane.add(shopDescriptionFieldPane);				
				
				shopDescriptionField = new JTextField(20);
					//shopDescriptionField.setSize(new Dimension(280, 80));
					//shopDescriptionField.setMaximumSize(new Dimension(280, 80));
					shopDescriptionFieldPane.add(shopDescriptionField);	
					
			JPanel qtyPane = new JPanel();				
				qtyPane.setLayout(new FlowLayout());
				productRequestPane.add(qtyPane);
				
				JLabel qtyLabel = new JLabel("Quantity");
					qtyPane.add(qtyLabel);
				
				qty = new JSpinner(new SpinnerNumberModel(1, 0, 200, 1));
					qtyPane.add(qty);
				
				
			JPanel submitButtonPane = new JPanel();
				submitButtonPane.setPreferredSize(new Dimension(300, 70));
				submitButtonPane.setLayout(new FlowLayout());
				productRequestPane.add(submitButtonPane);
				
				submitButton = new JButton("Submit Request");
					submitButtonPane.add(submitButton);
					submitButton.addActionListener(new ProductRequestSubmitButtonActionListener());
					
			JPanel exampleRequestsPane = new JPanel();
				exampleRequestsPane.setPreferredSize(new Dimension(300, 200));
				exampleRequestsPane.setBorder(BorderFactory.createTitledBorder("Example Requests"));
				productRequestPane.add(exampleRequestsPane);
				
				englishDescriptionField = new JTextArea(20, 7);
				
				JComboBox exampleRequests = new JComboBox();
					exampleRequestsPane.add(exampleRequests);					
					exampleRequests.addActionListener(new ExampleRequestsActionListener());
					for(int i=0; i<examples.size(); i++){
						exampleRequests.addItem("Example "+i);
					}
										
					JScrollPane englishDescriptionFieldScroll = new JScrollPane(englishDescriptionField);
						englishDescriptionFieldScroll.setPreferredSize(new Dimension(200, 120));
						exampleRequestsPane.add(englishDescriptionFieldScroll);
			
			JPanel ordersPane = new JPanel();
				ordersPane.setPreferredSize(new Dimension(300, 200));
				ordersPane.setBorder(BorderFactory.createTitledBorder("Order"));
				productRequestPane.add(ordersPane);
				
				ordersModel = new DefaultListModel();
				orders = new JList(ordersModel);
					ordersPane.add(orders);
					orders.setPreferredSize(new Dimension(200, 150));
					orders.setEnabled(false);
					
					
					
			JPanel confirmButtonPane = new JPanel();
				confirmButtonPane.setPreferredSize(new Dimension(300, 70));
				confirmButtonPane.setLayout(new FlowLayout());
				productRequestPane.add(confirmButtonPane);	
				
				confirmButton = new JButton("Confirm Order");
					confirmButtonPane.add(confirmButton);
					confirmButton.addActionListener(new ConfirmOrderButtonActionListener());
					
					
					
				
		
	}
	
	
	class ExampleRequest{
		public String productDescription;
		public String shopDescription;
		public String englishDescription;
		public ExampleRequest(String productDescription, String shopDescription, String englishDescription) {
			super();
			this.productDescription = productDescription;
			this.shopDescription = shopDescription;
			this.englishDescription = englishDescription;
		}		
		
	}


	public boolean approve(String brand) {
		int n = JOptionPane.showConfirmDialog(
			    this,
			    "Does the following product meet your requirements?\n"+brand,
			    "Your personal agent seeks your approval",
			    JOptionPane.YES_NO_OPTION);		
		
		if(n == JOptionPane.YES_OPTION){
			ordersModel.addElement(brand+" ("+lastQty+")");
			return true;
		}else{
			return false;
		}
		
	}
	
	public void message(String message) {
		JOptionPane.showMessageDialog(
			    this,
			    JASDLCommon.strip(message, "\""),
			    "Your personal agent has a message for you",
			    JOptionPane.WARNING_MESSAGE);
	}
	
	
	/* UI Action Listeners */
	
	class ProductRequestSubmitButtonActionListener implements ActionListener{
		public void actionPerformed(ActionEvent event) {
			String productDescription = productDescriptionField.getText();	
			String shopDescription = shopDescriptionField.getText();	
			
			// strip newlines
			productDescription = productDescription.replace("\n", " ");
			shopDescription = shopDescription.replace("\n", " ");
			env.executeAction(customer.getId().toString(), Literal.parseLiteral(
					"request_product(\""+productDescription+"\","+"\""+shopDescription+"\","+qty.getValue()+")"));
			
			lastQty = qty.getValue();
		}		
	}
	
	class ConfirmOrderButtonActionListener implements ActionListener{
		public void actionPerformed(ActionEvent event) {			
			env.executeAction(customer.getId().toString(), Literal.parseLiteral("confirm_order"));
			ordersModel.clear();
		}		
	}
	
	class ExampleRequestsActionListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
	        int n = cb.getSelectedIndex();
	        
	        ExampleRequest example = examples.get(n);
	        productDescriptionField.setText(example.productDescription);
	        shopDescriptionField.setText(example.shopDescription);
	        englishDescriptionField.setText(example.englishDescription);
		}
		
	}
}
