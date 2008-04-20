package commerce.ui.customer;

import jason.asSyntax.Literal;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import commerce.env.CommerceEnvironment;
import commerce.env.model.ModelCustomer;
import commerce.env.model.ModelCustomerListener;

public class CustomerUIPanel extends JPanel implements ModelCustomerListener{
	private JTextArea productDescriptionField;
	private JButton submitButton;
	private ModelCustomer customer;
	private CommerceEnvironment env;
	private JSpinner qty;
	private JTextArea shopDescriptionField;
	
	public CustomerUIPanel(CommerceEnvironment env, ModelCustomer customer){
		super();
		this.customer = customer;
		
		customer.addListener(this);
		
		this.env = env;
		setupUI();
	}
	
	private void setupUI(){		
		JPanel productRequestPane = new JPanel();		
			add(productRequestPane);			
			productRequestPane.setPreferredSize(new Dimension(200, 200));
			productRequestPane.setLayout(new BoxLayout(productRequestPane, BoxLayout.Y_AXIS));
			
			productDescriptionField = new JTextArea(20, 8);
				productRequestPane.add(productDescriptionField);
				
			shopDescriptionField = new JTextArea(20, 8);
				productRequestPane.add(shopDescriptionField);	
				
			JPanel buttonPane = new JPanel();
				buttonPane.setPreferredSize(new Dimension(200, 100));
				buttonPane.setLayout(new FlowLayout());
				productRequestPane.add(buttonPane);
				
				submitButton = new JButton("Submit");
					buttonPane.add(submitButton);
					submitButton.addActionListener(new ProductRequestSubmitButtonActionListener());
					
				qty = new JSpinner(new SpinnerNumberModel(1, 0, 200, 1));
					buttonPane.add(qty);
		
	}
	
	class ProductRequestSubmitButtonActionListener implements ActionListener{
		public void actionPerformed(ActionEvent event) {
			String productDescription = productDescriptionField.getText();	
			String shopDescription = shopDescriptionField.getText();	
			
			// strip new lines
			productDescription = productDescription.replace("\n", " ");
			shopDescription = shopDescription.replace("\n", " ");
			env.executeAction(customer.getId().toString(), Literal.parseLiteral(
					"request_product(\""+productDescription+"\","+"\""+shopDescription+"\","+qty.getValue()+")"));
		}		
	}

	public boolean approve(String brand) {
		int n = JOptionPane.showConfirmDialog(
			    this,
			    "Does the following product meet your requirements?\n"+brand,
			    "Your personal agent seeks your approval",
			    JOptionPane.YES_NO_OPTION);
		
		return n == JOptionPane.YES_OPTION;
	}
}
