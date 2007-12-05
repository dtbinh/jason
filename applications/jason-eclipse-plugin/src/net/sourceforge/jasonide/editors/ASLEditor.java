package net.sourceforge.jasonide.editors;

import jason.JasonException;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.jasonide.Activator;
import net.sourceforge.jasonide.core.MAS2JHandler;
import net.sourceforge.jasonide.core.PluginMarkerUtils;
import net.sourceforge.jasonide.editors.cbg.ColoringEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;

/**
 * ASL Text Editor.
 * @author Germano
 */
public class ASLEditor extends ColoringEditor {
	
	public ASLEditor() {
		super();
	}
	public void dispose() {
		super.dispose();
	}
	
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		
		compileInput();
	}
	
	private void compileInput() {		
		IDocument document = getDocumentProvider().getDocument(getEditorInput());
		IFileEditorInput iei = (IFileEditorInput)getEditorInput();
		IFile ifile = iei.getFile();
		IProject iproject = ifile.getProject();
		
		// delete old markers
		PluginMarkerUtils.deleteOldMarkers(ifile);
		
		String projectLocation = iproject.getLocation().toString();
		try {
			String mas2jFileName = MAS2JHandler.getMas2JFileName(iproject);
			MAS2JProject project = MAS2JHandler.parseForRun(mas2jFileName, projectLocation);
		
			List srcPaths = project.getSourcePaths();
			
			 // check if agent source file exists in actual sourcepath
            for (Iterator iter = srcPaths.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				String asFileName = element + File.separator + ifile.getName();
				
				File file = new File(asFileName);
				if (file.exists()) {
					jason.asSyntax.parser.as2j parser = new jason.asSyntax.parser.as2j(new FileReader(file));
					if (parser != null) {
						parser.agent(null);
					}
					break;
				} 
			}
			
		} catch (FileNotFoundException e) {
			showError(e.getMessage(), e);
		} catch (ParseException e) {
			String msg = e.getMessage();
			
			int lineError = PluginMarkerUtils.getLineNumberFromMsg(msg);
			PluginMarkerUtils.createMarker(ifile, e.getMessage().replace("\r", "").replace("\n", ""), 
						 lineError,
						 PluginMarkerUtils.getCharStart(document.get(), lineError, msg),
						 PluginMarkerUtils.getCharEnd(document.get(), lineError, msg));
		} catch (JasonException e) {
			String msg = "Problems:" + "\n\n" + e.getMessage();
			showError(msg, e);
		} catch (jason.asSyntax.parser.ParseException e) {
			String msg = e.getMessage();
			
			int lineError = PluginMarkerUtils.getLineNumberFromMsg(msg);
			PluginMarkerUtils.createMarker(ifile, e.getMessage().replace("\r", "").replace("\n", ""), 
						 lineError,
						 PluginMarkerUtils.getCharStart(document.get(), lineError, msg),
						 PluginMarkerUtils.getCharEnd(document.get(), lineError, msg));
		} catch (jason.asSyntax.parser.TokenMgrError e) {
			String msg = e.getMessage();
			
			int lineError = PluginMarkerUtils.getLineNumberFromMsg(msg);
			int charStart = PluginMarkerUtils.getCharStartFromLexicalError(document.get(), lineError, msg);
			int charEnd = PluginMarkerUtils.getCharEndLexicalError(document.get(), lineError, msg);
			PluginMarkerUtils.createMarker(ifile, e.getMessage().replace("\r", "").replace("\n", ""), 
						 lineError,
						 charStart,
						 charEnd);
		} 
	}
	
	private void showError(String msg, Throwable e) {
		ErrorDialog.openError(
				getSite().getShell(),
				"Error",
				null,
				new Status(IStatus.ERROR, Activator.getPluginId(), IStatus.ERROR, msg, e));
	}
}
