/*
 * generated by Xtext
 */
package jasonide.xtext.mas2j.ui;

import jasonide.xtext.mas2j.ui.outline.Mas2jOutlineTreeProvider;



import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.outline.IOutlineTreeProvider;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

/**
 * Use this class to register components to be used within the IDE.
 */
public class Mas2jUiModule extends jasonide.xtext.mas2j.ui.AbstractMas2jUiModule {
	public Mas2jUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}
	
	public Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration () {
	    return Mas2jHighlightingConfiguration.class;
	}
	
	public Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator(){
		return Mas2jHighlightingCalculator.class;
	}
	
	public Class<? extends IOutlineTreeProvider> bindIOutlineTreeProvider() {
	    return Mas2jOutlineTreeProvider.class;
	}
}
