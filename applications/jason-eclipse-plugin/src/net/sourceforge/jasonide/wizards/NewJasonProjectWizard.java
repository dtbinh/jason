package net.sourceforge.jasonide.wizards;

import jason.jeditplugin.Config;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Formatter;
import java.util.Scanner;

import net.sourceforge.jasonide.core.JasonPluginConstants;
import net.sourceforge.jasonide.core.PluginTemplates;
import net.sourceforge.jasonide.core.MAS2JHandler;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * This wizard creates a new Jason project.
 * Jason project contains two initial files:<br>
 *  - <b>project_name.mas2j</b> - Main Jason Project file.<br>
 *  - <b>environment_classname.java</b> - Java based environment class.
 * 
 * @author Germano
 * @version 1.0.0
 */
public class NewJasonProjectWizard extends BasicNewProjectResourceWizard implements INewWizard {
	
	private NewJasonProjectWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewJasonProjectWizard.
	 */
	public NewJasonProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("Jason Project");		
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new NewJasonProjectWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		
		final String classFileName = page.getEnvironmentClassFileName();
		final String infrastructure = page.getInfrastructure();		
		final IProject newProject = page.getProjectHandle();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(newProject, classFileName, infrastructure, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(IProject newProject, String envClassName, String infraStructure, IProgressMonitor monitor) throws CoreException {		
		monitor.beginTask("Creating the project " + newProject.getName(), 2);
		
		try {
			// create a new jason project.
			newProject.create(monitor);
			String projectRootDir = newProject.getLocation().toString();
			String projectName = newProject.getName();
			
			// create the project directories for sources.
			createProjectDirs(projectRootDir);
			
			// create the settings directory and core/ui prefs.
			createSettingsDir(projectRootDir);

			// create .classpath file
			createClassPathFile(projectRootDir);
			
			// contains environment?
			if (envClassName.length() > 0) {
				createEnvironmentClass(projectRootDir, envClassName, projectName);
			}
			
			// create initial files.
			createSampleAgentFile(projectRootDir, projectName);
			createMas2JFile(projectRootDir, projectName, infraStructure, envClassName);
			
			// open project and refesh local.
			newProject.open(monitor);
			
			// create the build.properties of the project.
			createBuildPropertiesFile(projectRootDir);
			
			// configure the .project file.
			configureProjectEnvironment(newProject, monitor);
			
			// create the project launchers (Run e Debug).
			createProjectLaunchConfiguration(projectRootDir, projectName, newProject, false);
			createProjectLaunchConfiguration(projectRootDir, projectName, newProject, true);
			
			// refresh the project after all these changes.
			newProject.getWorkspace().getRoot().refreshLocal(IWorkspaceRoot.DEPTH_INFINITE, monitor);
			monitor.beginTask("Project created in current workspace", 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the empty directories for the new Jason project.
	 */
	private void createProjectDirs(String projectRootDir) {
		new File(projectRootDir + File.separator + "src").mkdir();
		new File(projectRootDir + File.separator + "src" + File.separator + "java").mkdir();
		new File(projectRootDir + File.separator + "src" + File.separator + "asl").mkdir();
		new File(projectRootDir + File.separator + "bin").mkdir();
		new File(projectRootDir + File.separator + "bin" + File.separator + "classes").mkdir();
	}
	
	/**
	 * Creates the .classpath file for Jason Project.
	 */
	private void createClassPathFile(String projectRootDir) {
		String classPathContent = PluginTemplates.getProjectPropertiesContents();

		String jasonJarInProject = Config.get().getJasonJar();
		classPathContent = new Formatter().format(classPathContent, new Object[] {jasonJarInProject}).toString();
		
		try {
			FileWriter fw = new FileWriter(new File(projectRootDir + File.separator + ".classpath"));
			fw.write(classPathContent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Configures de .project file.
	 * @param project
	 * @param monitor
	 */
	private void configureProjectEnvironment(IProject project, IProgressMonitor monitor) {
		try {
			IProjectDescription pd = project.getDescription();
			
			ICommand c1 = pd.newCommand();
			c1.setBuilderName("org.eclipse.jdt.core.javabuilder");
			
			ICommand c2 = pd.newCommand();
			c2.setBuilderName("org.eclipse.pde.ManifestBuilder");
			
			pd.setBuildSpec(new ICommand[] {c1, c2});
			pd.setNatureIds(new String[] {"org.eclipse.jdt.core.javanature"});
		
			project.setDescription(pd, monitor);
		} catch (CoreException e) { 
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the build.properties of the new Jason Project.
	 */
	private void createBuildPropertiesFile(String projectRootDir) {
		try {
			FileWriter fw = new FileWriter(new File(projectRootDir + File.separator + "build.properties"));
			fw.write(PluginTemplates.getBuildPropertiesContents());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the .settings directory for Jason Project.
	 */
	private void createSettingsDir(String projectRootDir) {
		String corePrefsContent = PluginTemplates.getProjectCorePrefsContents();
		String uiPrefsContent = PluginTemplates.getProjectUiPrefsContents();

		try {
			// make .settings folder
			new File(projectRootDir + File.separator + ".settings").mkdir();
			
			// core prefs			
			FileWriter fw = new FileWriter(new File(projectRootDir + File.separator + ".settings" + File.separator + PluginTemplates.CORE_PREFS));
			fw.write(corePrefsContent);
			fw.flush();
			fw.close();
			
			// ui prefs
			fw = new FileWriter(new File(projectRootDir + File.separator + ".settings" + File.separator + PluginTemplates.UI_PREFS));
			fw.write(uiPrefsContent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the Java based Environment.
	 * @param projectRootDir
	 * @param envClassName
	 * @param projectName
	 */
	private void createEnvironmentClass(String dirRoot, String envClassName, String projectName) {
		try {
			// replace "." because it's special symbol in regular expressions.
			envClassName = envClassName.replace(".", "&");
			
			String[] items = envClassName.split("&");
			String className = null;
			String actualPackage = dirRoot + File.separator + "src" + File.separator + "java";
			String packages = "";
			
			if (items.length > 0) {
				className = items[items.length-1];
				
				for (int i = 0; i < items.length-1; i++) {
					File f = new File(actualPackage + File.separator + items[i]);
					f.mkdir();
					actualPackage = actualPackage + File.separator + items[i];
					
					if (packages.length() != 0) {
						packages = packages.concat(".");
					}
					packages = packages.concat(items[i]);
				}
			}
			else {
				className = "src" + File.separator + "java" + File.separator + envClassName;
			}
			
			// is Java code then must be placed in the source Java folder of the project.
			actualPackage += File.separator;

			File f = new File(actualPackage + File.separator + className + ".java");
			f.createNewFile();
		
			String jasonHome = JasonPluginConstants.JASON_HOME;
			String envTempl = jasonHome + 
			                  File.separator + 
			                  PluginTemplates.TEMPLATE_DIR +
			                  File.separator + 
			                  PluginTemplates.ENVIRONMENT;
			
			StringBuffer buffer = new StringBuffer();
			Scanner s = new Scanner(new File(envTempl));
			while (s.hasNextLine()) {
				buffer.append(s.nextLine().concat("\r\n"));
			}
			
			String templateContent = buffer.toString();
			
			// contains package(s)
			if (packages.length() != 0) {
				templateContent = "package ".concat(packages).concat(";\r\n\r\n").concat(templateContent);
			}
			
			templateContent = templateContent.replace("<PROJECT_NAME>", projectName);
			templateContent = templateContent.replace("<ENV_NAME>", className);
			
			FileWriter fw = new FileWriter(f);
			fw.write(templateContent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a sample agent named sample.asl.
	 * @param projectRootDir
	 */
	private void createSampleAgentFile(String projectRootDir, String projectName) {
		try {
			File f = new File(projectRootDir + File.separator + JasonPluginConstants.AGENT_DEFAULT_REPOS + File.separator + "sample.asl");
			f.createNewFile();
		
			String jasonHome = JasonPluginConstants.JASON_HOME;
			String envTempl = jasonHome + 
			                  File.separator + 
			                  PluginTemplates.TEMPLATE_DIR +
			                  File.separator + 
			                  PluginTemplates.AGENT;
			
			StringBuffer buffer = new StringBuffer();
			Scanner s = new Scanner(new File(envTempl));
			while (s.hasNextLine()) {
				buffer.append(s.nextLine().concat("\r\n"));
			}
			
			String templateContent = buffer.toString();
			
			templateContent = templateContent.replace("<AG_NAME>", "sample");
			templateContent = templateContent.replace("<PROJECT_NAME>", projectName);
			
			FileWriter fw = new FileWriter(f);
			fw.write(templateContent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the Mas2J file. This file represents a Jason project.
	 * @param projectRootDir
	 * @param projectName
	 * @param infrastructure
	 */
	private void createMas2JFile(String projectRootDir, String projectName, String infrastructure, String envClassName) {
		MAS2JProject mas2jProject = new MAS2JProject();
		mas2jProject.setSocName(projectName.toLowerCase());
		mas2jProject.setInfrastructure(new ClassParameters(infrastructure));
		mas2jProject.addSourcePath(JasonPluginConstants.AGENT_DEFAULT_REPOS_MAS2J);
		
		AgentParameters ag = new AgentParameters();
		ag.name = JasonPluginConstants.SAMPLE_AGENT_NAME;
		//ag.setupDefault();
		mas2jProject.addAgent(ag);
		mas2jProject.setupDefault();
		
		if (envClassName.length() > 0) {
			mas2jProject.setEnvClass(new ClassParameters(envClassName));
		}
		
		try {
			File f = new File(projectRootDir + File.separator + projectName + MAS2JHandler.MAS2J_EXT);
			f.createNewFile();
			
			FileWriter fw = new FileWriter(f);
			fw.write(MAS2JHandler.mas2jProjectToString(mas2jProject));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the Launcher for the MAS Project.
	 * @param projectRootDir
	 * @param projectName
	 * @param newProject
	 * @param debug
	 */
	private void createProjectLaunchConfiguration(String projectRootDir, String projectName, IProject newProject, boolean debug) {
		try {
			String mas2jFileName = projectRootDir + File.separator + projectName + MAS2JHandler.MAS2J_EXT;
			String launcherConfigName;
			String secondParameter;
			String supportsMode;
			if (debug) {
				launcherConfigName = "Debug MAS - " + projectName;
				secondParameter = "debug";
				supportsMode = ILaunchManager.DEBUG_MODE;
			}
			else {
				launcherConfigName = "Run MAS - " + projectName;
				secondParameter = "run";
				supportsMode = ILaunchManager.RUN_MODE;
			}
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
			for (int i = 0; i < configurations.length; i++) {
				ILaunchConfiguration configuration = configurations[i];
				if (configuration.getName().equals(launcherConfigName)) {
					configuration.delete();
					break;
				}
			}
			
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, launcherConfigName);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "jason.mas2j.parser.mas2j");
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, mas2jFileName + " " + secondParameter);
			workingCopy.supportsMode(supportsMode);

			/*ILaunchConfiguration config = */
			workingCopy.doSave();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}