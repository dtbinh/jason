package net.sourceforge.jasonide.wizards;

import net.sourceforge.jasonide.Activator;
import net.sourceforge.jasonide.core.JasonPluginConstants;
import net.sourceforge.jasonide.core.PluginTemplates;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "java". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */
public class NewInternalActionWizard extends Wizard implements INewWizard {
	private NewInternalActionWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewInternalActionWizard.
	 */
	public NewInternalActionWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new NewInternalActionWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		String tmpContainerName = page.getContainerName();
		final String packageName = page.getPackageName();
		
		if (packageName != null) {
			tmpContainerName += "/" + packageName.replace(".", "/");
		}
		
		final String containerName = tmpContainerName;
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, packageName, fileName, monitor);
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

	private void doFinish(String containerName, String packageName, String fileName, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName + ".java"));
		try {
			InputStream stream = openContentStream(containerName, packageName, fileName);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
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
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream(String containerName, String packageName, String fileName) {
		try {
			String jasonHome = JasonPluginConstants.JASON_HOME;
			String iaTempl = jasonHome + 
			                  File.separator + 
			                  PluginTemplates.TEMPLATE_DIR +
			                  File.separator + 
			                  PluginTemplates.INTERNAL_ACTION;
			
			StringBuffer buffer = new StringBuffer();
			Scanner s = new Scanner(new File(iaTempl));
			while (s.hasNextLine()) {
				buffer.append(s.nextLine().concat("\r\n"));
			}
			
			String agentFileContents = buffer.toString();
			
			String iaName = fileName;
			String projectName = containerName.split("/")[1];
			
			agentFileContents = agentFileContents.replace("<IA_NAME>", iaName);
			agentFileContents = agentFileContents.replace("<PROJECT_NAME>", projectName);
			
			if (packageName != null) {
				agentFileContents = agentFileContents.replace("<PCK>", packageName);
			}
			
			return new ByteArrayInputStream(agentFileContents.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

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