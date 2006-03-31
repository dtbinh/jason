package jason.infra.centralised;

import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

// TODO ask for source, browse button

public class StartNewAgentGUI extends BaseDialogGUI {

	protected JTextField agName;
	protected JTextField agSource;
	protected JTextField archClass;
	protected JTextField agClass;
	protected JTextField nbAgs;
	protected JTextField agHost;
	protected JComboBox  verbose;

	public StartNewAgentGUI(Frame f, String title) {
		super(f, title);
	}
	
	protected void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		
		// Fields
		
		agName = new JTextField(10);
		createField("Agent name", agName, "The agent name");
		
		agSource = new JTextField(10);
		createField("Source", agSource, "The path for the agent's source file (e.g. ../asl/code.asl). If left empty, the file will be the agent's name + .asl.");

		agClass = new JTextField(20);
		createField("Agent class", agClass, "The customisation class for the agent (<package.classname>). If not filled, the default agent class will be used.");
		
		archClass = new JTextField(20);
		createField("Architecture class", archClass, "The customisation class for the agent architecture (<package.classname>). If not filled, the default architecture will be used.");

		nbAgs = new JTextField(4);
		nbAgs.setText("1");
		createField("Number of agents", nbAgs, "The number of agents that will be instantiated from this declaration.");

		verbose = new JComboBox(new String[] { "no output", "normal", "debug" });
		verbose.setSelectedIndex(1);
		createField("Verbose", verbose, "Set the verbose level");

		agHost = new JTextField(10);
		agHost.setText("localhost");
		createField( "Host to run", agHost, "The host where this agent will run. The infrastructure must support distributed launching.");
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Agent parameters", TitledBorder.LEFT, TitledBorder.TOP));
		p.add(pLabels, BorderLayout.CENTER);
		p.add(pFields, BorderLayout.EAST);
		
		getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	
	protected boolean ok() {
		final AgentParameters ap = getAgDecl();
		if  (ap == null) {
			JOptionPane.showMessageDialog(this, "An agent name must be informed.");
			return false;
		}
		if (ap.asSource == null) {
			ap.asSource = new File(ap.name + ".asl");
		}
		if (!ap.asSource.exists()) {
			JOptionPane.showMessageDialog(this, "The source file '"+ap.asSource+"' does not exist!");
			return false;			
		}
		new Thread() {
			public void run() {
				boolean debug = RunCentralisedMAS.isDebug();
				boolean fs    = RunCentralisedMAS.getRunner().getControllerInfraTier() != null;
				RuntimeServicesInfraTier services = RunCentralisedMAS.getRunner().getEnvironmentInfraTier().getRuntimeServices();
				try {
					for (int i=0; i<ap.qty; i++) {
						String name = ap.name;
						if (ap.qty > 1) {
							name = name + (i+1);
						}
						services.createAgent(name, ap.asSource.getName(), ap.agClass, ap.archClass, ap.getAsSetts(debug, fs));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		return true;
	}
	
	protected AgentParameters getAgDecl() {
		if  (agName.getText().trim().length() == 0) {
			return null;
		}
		AgentParameters ap = new AgentParameters();
		ap.name = agName.getText().trim();
		if (verbose.getSelectedIndex() != 1) {
			ap.options = new HashMap();
			ap.options.put("verbose", verbose.getSelectedIndex()+"");
		}
		
		if (agSource.getText().trim().length() > 0) {
			ap.asSource = new File(agSource.getText().trim());
		}

		if (archClass.getText().trim().length() > 0) {
			ap.archClass = archClass.getText().trim();
		}

		if (agClass.getText().trim().length() > 0) {
			ap.agClass = agClass.getText().trim();
		}
		if (!nbAgs.getText().trim().equals("1")) {
			try {
				ap.qty = Integer.parseInt(nbAgs.getText().trim());
			} catch (Exception e) {
				System.err.println("Number of hosts is not a number!");
			}
		}
		if (!agHost.getText().trim().equals("localhost")) {
			ap.host = agHost.getText().trim();			
		}
		return ap;
	}
	
}
