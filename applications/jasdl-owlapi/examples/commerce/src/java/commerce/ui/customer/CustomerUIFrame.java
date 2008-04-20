package commerce.ui.customer;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import commerce.env.CommerceEnvironment;
import commerce.env.model.ModelCustomer;

public class CustomerUIFrame extends JFrame{

	private JTabbedPane tabs;
	private CommerceEnvironment env;

	public CustomerUIFrame(CommerceEnvironment env){	
		super("Commerce: Customer UI");
		this.env = env;
		
		setPreferredSize(new Dimension(220, 320));
		
		tabs = new JTabbedPane();
		add(tabs);		
		tabs.setVisible(true);
		
		pack();
		setVisible(true);
	}
	
	public void addCustomer(ModelCustomer customer){
		CustomerUIPanel panel = new CustomerUIPanel(env, customer);
		tabs.addTab(customer.getLabel(), panel);		
	}
	

}
