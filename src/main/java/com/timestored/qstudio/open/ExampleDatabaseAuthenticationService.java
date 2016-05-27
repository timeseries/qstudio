package com.timestored.qstudio.open;

import javax.swing.JOptionPane; 

/**
 * This example prompts the user for a password every time a connection is attempted,
 * this is NOT recommended. We recommend some form of caching with timeout etc.
 */
public class ExampleDatabaseAuthenticationService implements DatabaseAuthenticationService {

	public ConnectionDetails getonConnectionDetails(ConnectionDetails cd) {
		String password = JOptionPane.showInputDialog("password:");
		return new ConnectionDetails(cd.getHost(), cd.getPort(), cd.getDatabase(), cd.getUsername(), password);
	}

	@Override public String getName() {
		return "PasswordPopup";
	}

}
