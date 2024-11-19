/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2023 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timestored.connections;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;


/**
 * Display name of currently selected server and allow changing selection.
 */
public class ServerNameComboBox extends JComboBox {
	
	private static final long serialVersionUID = 1L;
	private final ConnectionManager connectionManager;
	
	public ServerNameComboBox(final ConnectionManager connectionManager) {
    	
		this.connectionManager = connectionManager;
        
        connectionManager.addListener(new ConnectionManager.Listener() {
			
			@Override public void statusChange(ServerConfig serverConfig, 
					boolean connected) {
				refresh();
			}
			
			@Override public void prefChange() {
				refresh();
			}
			@Override public void serverAdded(ServerConfig sc) {  }	
		});
			
		refresh();
	}

	private void refresh() {
		String selectedServer = (String) getSelectedItem();
		List<String> serverNames = connectionManager.getServerNames();
        String[] names = serverNames.toArray(new String[]{});
		setModel(new DefaultComboBoxModel(names));
		if(serverNames.contains(selectedServer)){
			setSelectedItem(selectedServer);
		}
	}

	/** @return Server associated with the serverName, or null if not found. */ 
	public ServerConfig getSelectedServer() {
		return connectionManager.getServer((String) getSelectedItem());
	}
}