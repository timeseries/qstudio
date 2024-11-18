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
package com.timestored.qstudio;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.timestored.connections.ConnectionManager;
import com.timestored.connections.ConnectionShortFormat;
import com.timestored.connections.ConnectionShortFormat.ParseResult;
import com.timestored.connections.JdbcTypes;
import com.timestored.connections.ServerConfig;
import com.timestored.swingxx.SwingUtils;
import com.timestored.theme.Theme;

/**
 * Action that shows dialog allowing entry of many servers using {@link ConnectionShortFormat}.
 */
class AddServerListAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private final JFrame parent;
		private final ConnectionManager connectionManager;

		public AddServerListAction(final JFrame parent, ConnectionManager connectionManager) {
			
			super("Add Server List...", Theme.CIcon.SERVER_ADD.get());
			putValue(SHORT_DESCRIPTION, "Add multiple servers to the list of possible connections");
			this.parent = parent;
			this.connectionManager = connectionManager;
		}
		
		@Override public void actionPerformed(ActionEvent arg0) {
//			new AddServerListDialog(connectionManager, parent).setVisible(true);

			final String DEFAULT_SERVER = "folder/subf/my-server-name@localhost:5000\r\nfolder/subf/pro-server@localhost:5002:username:password\r\n127.0.0.1:5001";
			String helpMsg = "Enter your server list below, one per row.\r\n" +
					"Two examples of the format expected are given.\r\n";
			String title = "Add Server List";
			
			String txt = Theme.getTextFromDialog(parent, title, DEFAULT_SERVER, helpMsg);
			
			final String textEntered = txt;
			if(textEntered!=null) {
				BackgroundExecutor.EXECUTOR.execute(new Runnable() {
					
					@Override public void run() {
						
						List<ParseResult> r = ConnectionShortFormat.parse(textEntered, 
								JdbcTypes.KDB, new JdbcTypes[] {JdbcTypes.KDB});
						StringBuilder errorSb = new StringBuilder();
						
						final String SEP = "\r\n\r\n";
						
						// get those conns that parsed ok
						ArrayList<ServerConfig> connections = new ArrayList<ServerConfig>();
						for(ParseResult pr : r) {
							if(pr.serverConfig != null) {
								connections.add(pr.serverConfig);
							} else {
								errorSb.append("Failed Line: ").append(pr.originalLine)
									.append(" - ").append(pr.report).append(SEP);
							}
						}
						// try adding the ok conns
						List<ServerConfig> failedConns = connectionManager.addServer(connections);
						for(ParseResult pr : r) {
							ServerConfig sc = pr.serverConfig;
							if(sc!=null && failedConns.contains(sc)) {
								errorSb.append("Could not add sc: " + sc.getName() + SEP);
							}
						}
						
						// report any problems
						if(errorSb.length()>0) {
							SwingUtils.showMessageDialog(parent, errorSb.toString(), 
									"Error Parsing Servers", JOptionPane.WARNING_MESSAGE);
						}
					}
				});
			}
		}

	}