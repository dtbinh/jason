package net.sourceforge.jasonide.wizards;

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import net.sourceforge.jasonide.Activator;
import net.sourceforge.jasonide.core.JasonPluginConstants;
import net.sourceforge.jasonide.core.JasonPluginException;
import net.sourceforge.jasonide.core.MAS2JHandler;
import net.sourceforge.jasonide.core.PluginTemplates;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Wizard for create a new agent with default contents.
 * @author Germano
 */
public class NewAgentWizard extends Wizard implements INewWizard {
	private NewAgentWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewFileAgentWizard.
	 */
	public NewAgentWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new NewAgentWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
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
	private void doFinish(String containerName, String fileName, IProgressMonitor monitor) throws CoreException {
		
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(containerName, fileName);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
				registerNewAgentInMas2JProjectFile(resource, file);
			}
			stream.close();
		} catch (IOException e) {
		}
		
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * Register the new created agent in Mas2j file project.
	 * @param resource
	 * @param file
	 * @throws CoreException
	 */
	private void registerNewAgentInMas2JProjectFile(IResource resource, IFile file) throws CoreException {
		try {
			// parse the mas2j file project.
			MAS2JProject project2 = MAS2JHandler.parse(MAS2JHandler.getMas2JFileName(file.getProject()));
			
			AgentParameters ag = new AgentParameters();
			ag.name = file.getName().replace(MAS2JHandler.AS_EXT, ""); // removes the file extension.
			
			project2.addAgent(ag);

			MAS2JHandler.persistMas2JFile(file.getProject(), MAS2JHandler.mas2jProjectToString(project2));
		} catch (ParseException e1) {
			throwCoreException(e1.getMessage());
		} catch (FileNotFoundException e1) {
			throwCoreException(e1.getMessage());
		} catch (JasonPluginException e1) {
			throwCoreException(e1.getMessage());
		}
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream(String containerName, String fileName) {
		try {
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
			
			String agentFileContents = buffer.toString();
			
			fileName = fileName.replace(".", ">");
			String agentName = fileName.split(">")[0];
			String projectName = containerName.split("/")[1];
			
			agentFileContents = agentFileContents.replace("<AG_NAME>", agentName);
			agentFileContents = agentFileContents.replace("<PROJECT_NAME>", projectName);
			
			return new ByteArrayInputStream(agentFileContents.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Throw CoreException for a given message.
	 * @param message
	 * @throws CoreException
	 */
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, Activator.getPluginId(), IStatus.OK, message, null);
		throw new CoreException(status);
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