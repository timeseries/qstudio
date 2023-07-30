package com.timestored.qstudio;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import com.timestored.qstudio.model.QueryAdapter;
import com.timestored.qstudio.model.QueryManager;

/**
 * Display name of currently selected server and allow changing selection.
 */
class ServerNameComboBox extends JComboBox {
	
	private static final long serialVersionUID = 1L;
	private final QueryManager queryManager;
	
	ServerNameComboBox(final QueryManager queryManager) {
    	
		this.queryManager = queryManager;
        addActionListener(this);
        
        queryManager.addQueryListener(new QueryAdapter() {
			@Override public void selectedServerChanged(String server) {
				refresh();
			}

			@Override
			public void serverListingChanged(List<String> serverNames) {
		        String[] names = serverNames.toArray(new String[]{});
				setModel(new DefaultComboBoxModel(names));
				refresh();
			}
			
		});
        
		setModel(new DefaultComboBoxModel(queryManager.getServerNames().toArray(new String[]{})));
		refresh();
	}

	private void refresh() {
		String selServer = queryManager.getSelectedServerName(); 
		String mySel = (String) getSelectedItem();
		if(selServer != null && !selServer.equals(mySel)) {
			setSelectedItem(selServer);
		}
	}
	
	@Override public void actionPerformed(ActionEvent e) {
		queryManager.setSelectedServerName((String) getSelectedItem());
	}
}