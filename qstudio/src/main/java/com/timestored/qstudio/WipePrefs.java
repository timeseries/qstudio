package com.timestored.qstudio;

import java.util.prefs.BackingStoreException;

import javax.swing.JOptionPane;

import com.timestored.connections.ConnectionManager;

/** Wipes qStudio preferences and resets defaults to allow testing newly installed behaviour. */
public class WipePrefs {

	public static void main(String... args) throws BackingStoreException {
		
		String message = "First make sure qStudio is closed!" +
				"\r\n\r\nThen clicking yes below will remove:" +
				"\r\n- all saved connections" +
				"\r\n- stored settings" +
				"\r\n- open file history. \r\nAre you sure you want to continue?";
		int choice = JOptionPane.showConfirmDialog(null, message, 
				"Delete all settings?", JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		
		if(choice == JOptionPane.YES_OPTION) {
			try {
				QStudioFrame.resetDefaults(false);
				ConnectionManager.wipePreferences(Persistance.INSTANCE.getPref(), Persistance.Key.CONNECTIONS.name());
			} catch (BackingStoreException e1) {
				String errMsg = "Problem accessing registry, please report as bug";
				JOptionPane.showMessageDialog(null, errMsg);
			}
		} else if(choice == JOptionPane.NO_OPTION) {
			System.out.println("no");
		}
		
	}
}
