//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.9  2006/02/17 11:45:52  jomifred
//   no message
//
//   Revision 1.8  2006/01/14 15:26:28  jomifred
//   Config and some code of RunMAS was moved to package plugin
//
//   Revision 1.7  2006/01/11 15:14:39  jomifred
//   add close all befere opening a mas2j project
//
//   Revision 1.5  2005/12/30 20:40:17  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.1  2005/12/08 20:15:00  jomifred
//   changes for JasonIDE plugin
//
//----------------------------------------------------------------------------

package jason.jeditplugin;


import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.gjt.sp.jedit.AbstractOptionPane;

public class JasonIDOptionPanel extends AbstractOptionPane  {
	
	JTextField saciTF;
	JTextField jasonTF;
	JTextField javaTF;
	JTextField shellTF;
	//JCheckBox  insideJIDECBox;
	JCheckBox  closeAllCBox;

	static Config userProperties = Config.get();

	static {
        String currJasonVersion = userProperties.getJasonRunningVersion();

		// check new version
    	//File jasonConfFile = getUserConfFile();
    	if (userProperties.getProperty("version") != null) {
    		//userProperties.load(new FileInputStream(jasonConfFile));
    		if (!userProperties.getProperty("version").equals(currJasonVersion) && !currJasonVersion.equals("?")) { 
    			// new version, set all values to default
    			System.out.println("This is a new version of Jason, reseting configuration...");
    			userProperties.clear();
    			/*
    			userProperties.remove(Config.JAVA_HOME);
    			userProperties.remove(Config.SACI_JAR);
    			userProperties.remove(Config.JASON_JAR);
    			userProperties.remove(Config.LOG4J_JAR);
    			userProperties.remove(Config.RUN_AS_THREAD);
    			*/
    		}
    	} 

    	userProperties.fix();
    	userProperties.store();
	}
	
	public JasonIDOptionPanel() {
		super("Jason");
	}

	protected void _init() {
		JPanel pop = new JPanel(new GridLayout(0,1));
    	// jason home
    	JPanel jasonHomePanel = new JPanel();
    	jasonHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "jason.jar file", TitledBorder.LEFT, TitledBorder.TOP));
    	jasonHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	jasonHomePanel.add(new JLabel("Location"));
    	jasonTF = new JTextField(30);
    	jasonHomePanel.add(jasonTF);
    	JButton setJason = new JButton("Browse");
    	setJason.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	            try {
					JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
					chooser.setDialogTitle("Select the jason.jar file");
					chooser.setFileFilter(new JarFileFilter("jason.jar", "The Jason.jar file"));
	                //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	                	String jasonJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
	                	if (userProperties.checkJar(jasonJar)) {
							jasonTF.setText(jasonJar);
	                	}
	                }
	            } catch (Exception e) {}
			}
    	});
    	jasonHomePanel.add(setJason);
    	pop.add(jasonHomePanel);
    	
    	
    	// saci home
    	JPanel saciHomePanel = new JPanel();
    	saciHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "saci.jar file", TitledBorder.LEFT, TitledBorder.TOP));
    	saciHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	saciHomePanel.add(new JLabel("Location"));
    	saciTF = new JTextField(30);
    	saciHomePanel.add(saciTF);
    	JButton setSaci = new JButton("Browse");
    	setSaci.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	            try {
	                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
					chooser.setDialogTitle("Select the Saci.jar file");
					chooser.setFileFilter(new JarFileFilter("saci.jar", "The saci.jar file"));
					//chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	                	String saciJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
	                	if (userProperties.checkJar(saciJar)) {
	                		saciTF.setText(saciJar);
	                	}
	                }
	            } catch (Exception e) {}
			}
    	});
    	saciHomePanel.add(setSaci);
    	pop.add(saciHomePanel);

    	// java home
    	JPanel javaHomePanel = new JPanel();
    	javaHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Java Home", TitledBorder.LEFT, TitledBorder.TOP));
    	javaHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	javaHomePanel.add(new JLabel("Directory"));
    	javaTF = new JTextField(30);
    	javaHomePanel.add(javaTF);
    	JButton setJava = new JButton("Browse");
    	setJava.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	            try {
	                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
					 chooser.setDialogTitle("Select the Java JDK Home directory");
	                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	                	String javaHome = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
	                	if (userProperties.checkJavaHomePath(javaHome)) {
	                		javaTF.setText(javaHome);
	                	}
	                }
	            } catch (Exception e) {}
			}
    	});
    	javaHomePanel.add(setJava);
    	pop.add(javaHomePanel);
    	
    	// shell command
    	JPanel shellPanel = new JPanel();
    	shellPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Shell command", TitledBorder.LEFT, TitledBorder.TOP));
    	shellPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	shellTF = new JTextField(30);
    	shellTF.setToolTipText("This command will be used to run the scripts that run the MAS.");
    	shellPanel.add(shellTF);
    	pop.add(shellPanel);

    	// run centralised inside jIDE
    	/*
    	JPanel insideJIDEPanel = new JPanel();
    	insideJIDEPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Centralised MAS execution mode", TitledBorder.LEFT, TitledBorder.TOP));
    	insideJIDEPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	insideJIDECBox = new JCheckBox("Run MAS as a JasonIDE internal thread instead of another process.");
    	insideJIDEPanel.add(insideJIDECBox);
    	pop.add(insideJIDEPanel);
    	*/

    	// close all before opening mas project
    	JPanel closeAllPanel = new JPanel();
    	closeAllPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "jEdit options", TitledBorder.LEFT, TitledBorder.TOP));
    	closeAllPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	closeAllCBox = new JCheckBox("Close all files before opening a new Jason Project.");
    	closeAllPanel.add(closeAllCBox);
    	pop.add(closeAllPanel);

    	addComponent(pop);
    	
    	saciTF.setText(userProperties.getSaciJar());
    	jasonTF.setText(userProperties.getJasonJar());
    	javaTF.setText(userProperties.getJavaHome());
    	shellTF.setText(userProperties.getShellCommand());
    	//insideJIDECBox.setSelected(userProperties.runAsInternalTread());
    	closeAllCBox.setSelected(userProperties.getBoolean(Config.CLOSEALL));
	}

	protected void _save() {
		if (userProperties.checkJar(saciTF.getText())) {
			userProperties.put(Config.SACI_JAR, saciTF.getText().trim());
		}
		if (userProperties.checkJar(jasonTF.getText())) {
			userProperties.put(Config.JASON_JAR, jasonTF.getText().trim());
		}
		if (userProperties.checkJavaHomePath(javaTF.getText())) {
			userProperties.setJavaHome(javaTF.getText().trim());
		}
		userProperties.put(Config.SHELL_CMD, shellTF.getText().trim());
		//userProperties.put(Config.RUN_AS_THREAD, insideJIDECBox.isSelected()+"");
		userProperties.put(Config.CLOSEALL, closeAllCBox.isSelected()+"");
		userProperties.store();
	}

    class JarFileFilter extends FileFilter {
		String jar,ds;
		public JarFileFilter(String jar, String ds) {
			this.jar = jar;
			this.ds  = ds;
		}
        public boolean accept(File f) {
            if (f.getName().endsWith(jar) || f.isDirectory()) {
				return true;
            } else {
				return false;
            }
        }
        
        public String getDescription() {
            return ds;
        }
    }
}
