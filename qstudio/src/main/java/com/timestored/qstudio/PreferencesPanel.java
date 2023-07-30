package com.timestored.qstudio;

import java.awt.Component;

import javax.swing.JPanel;

import com.google.common.base.Preconditions;

public abstract class PreferencesPanel extends JPanel {

	protected final MyPreferences myPreferences;
	protected final Component container;

	PreferencesPanel(final MyPreferences myPreferences, Component container) {
		this.myPreferences = Preconditions.checkNotNull(myPreferences);
		this.container = Preconditions.checkNotNull(container);
	}
		
	/** Take the persisted values and show them values in the GUI */
	abstract void refresh();
	
	/** Take all changes the user made and persist them.  */
	abstract void saveSettings();
	
	/** undo any live settings changes that were made **/
	abstract void cancel();
}
