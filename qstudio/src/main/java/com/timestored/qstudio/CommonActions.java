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

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import lombok.Getter;

import com.google.common.base.Preconditions;
import com.timestored.TimeStored;
import com.timestored.command.Command;
import com.timestored.command.CommandManager;
import com.timestored.command.CommandProvider;
import com.timestored.connections.ConnectionManager;
import com.timestored.connections.ConnectionManagerDialog;
import com.timestored.connections.ConnectionShortFormat;
import com.timestored.connections.JdbcTypes;
import com.timestored.connections.ServerConfig;
import com.timestored.connections.ServerConfigBuilder;
import com.timestored.docs.Document;
import com.timestored.docs.OpenDocumentsModel;
import com.timestored.jgrowl.Growler;
import com.timestored.messages.Msg;
import com.timestored.messages.Msg.Key;
import com.timestored.misc.IOUtils;
import com.timestored.qstudio.kdb.ModuleRunner;
import com.timestored.qstudio.model.QueryAdapter;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.QueryResult;
import com.timestored.theme.ShortcutAction;
import com.timestored.theme.Theme;
import com.timestored.theme.Theme.CIcon;

/**
 * Contains common actions for querying, editing servers etc that can be 
 * activated from various parts of qStudio.
 */
public class CommonActions implements CommandProvider {

	private static final Logger LOG = Logger.getLogger(CommonActions.class.getName());

	
	private final Action profileAction;
	private final Action copyServerHopenToClipboardAction;
	private final Action editCurrentServerAction;
	private final Action unitTestAction;
	private final Action loadScriptAction;
	
	private final Action qAction;
	private final Action qLineAction;
	private final Action qLineAndMoveAction;
	private final Action qCancelQueryAction;
	private final Action sendClipboardQuery;
	private final QueryManager queryManager;
	private final Growler growler;

	private final Action addServerAction = new AddServerAction(null);
	private final Action addServerListAction;
	private final Action removeServerAction;
	private final AbstractAction copyServerListAction;
	private final List<Action> serverActions;
	
	private final List<Action> queryActions;
	private final List<Action> proActions;

	private final OpenDocumentsModel openDocumentsModel;
	private final ConnectionManager connectionManager;
	private final Persistance persistance;
	private JFrame parent;
	@Getter private final Action watchDocExpressionAction;



	public static final KeyStroke AUTO_COMPLETE_KS = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
	
	/**
	 * @param parent the frame that popup dialogs will be centred to.
	 */
	CommonActions(final OpenDocumentsModel openDocumentsModel, 
			final QueryManager queryManager,
			final ConnectionManager connectionManager, 
			final Persistance persistance, 
			final JFrame parent, final Growler growler) {

		this.openDocumentsModel = openDocumentsModel;
		this.queryManager = queryManager;
		this.connectionManager = connectionManager;
		this.persistance = persistance;
		this.parent = parent;
		this.growler = growler;
		
		/*
		 * Construct all of our actions
		 */        
		qAction = new ShortcutAction(Msg.get(Key.QUERY_SELECTION),
        		Theme.CIcon.SERVER_GO, "Send highlighted Q query to current server.",
        		KeyEvent.VK_E, KeyEvent.VK_E) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				String qry = openDocumentsModel.getSelectedDocument().getSelectedText();
				if(qry.length()>0) {
					sendQuery(openDocumentsModel.getSelectedDocument().getSelectedText());	
				} else {
					sendQuery(openDocumentsModel.getSelectedDocument().getContent());
				}
			}
		};
		
		editCurrentServerAction = new ShortcutAction("Edit current server",
        		Theme.CIcon.SERVER_EDIT, "Edit the currently selected server.") {
			@Override public void actionPerformed(ActionEvent e) {
				String serverName = queryManager.getSelectedServerName();
				if(serverName != null) {
					new ConnectionManagerDialog(connectionManager, parent, serverName).setVisible(true);
				}
			}
			
			@Override public boolean isEnabled() {
				return queryManager.getSelectedServerName() != null;
			}
		};
		
		copyServerHopenToClipboardAction = new ShortcutAction("Copy hopen `:Server",
        		Theme.CIcon.EDIT_COPY, "Copy an hopen command for the current server to the clipboard") {
			@Override public void actionPerformed(ActionEvent e) {
				String serverName = queryManager.getSelectedServerName();
				if(serverName != null) {
					ServerConfig server = connectionManager.getServer(serverName);
					if(server != null) {
						Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
						String hopenCommand = "hopen `$\":" + server.getHost() + ":" + server.getPort() + ":" + server.getUsername() + ":\"";
						clpbrd.setContents(new StringSelection(hopenCommand ), null);
						growler.showInfo("Copied to clipboard:\r\n" + hopenCommand, "Clipboard Set");
					}
				}
				
			}
		};
		
		profileAction = new ShortcutAction(Msg.get(Key.PROFILE_SELECTION),
        		Theme.CIcon.CLOCK_GO, "Profile highlighted Q query on current server.",
        		KeyEvent.VK_Q, KeyEvent.VK_Q) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent ae) {

				if(QLicenser.requestPermission(QLicenser.Section.UI_NICETIES)) {
					String qry = openDocumentsModel.getSelectedDocument().getSelectedText();
					if(qry.length()<=0) {
						qry = openDocumentsModel.getSelectedDocument().getContent();
					}
					if(qry.length() > 0) {
						try {
							String prof = IOUtils.toString(CommonActions.class, "profile.q");
							String fullQuery = prof + "@[;1] .prof.profile \"" +
									qry.replace("\"", "\\\"") + "\""; 
							sendQuery(fullQuery, "PROFILE -> " + qry);
						} catch (IOException e) {
							LOG.log(Level.SEVERE, "Problem loading profile.q", e);
						}
					}
				}
				
			}
		};

		
		unitTestAction = new ShortcutAction("Unit Test Current Script",
        		Theme.CIcon.SCRIPT_GO, "Unit test all namespaces within the current script.",
        		KeyEvent.VK_T, KeyEvent.VK_T) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent ae) {
				runQUnitTests();
			}
		};

		 
		loadScriptAction = new ShortcutAction(Msg.get(Key.LOAD_SCRIPT_MODULE),
        		Theme.CIcon.SERVER_GO, "Load this q script onto current server.",
        		KeyEvent.VK_L, KeyEvent.VK_L) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				runScriptModuleLoad();
			}
		};
		
        qLineAction = new ShortcutAction(Msg.get(Key.QUERY_LINE),
        		Theme.CIcon.SERVER_GO, "Send current line as query",
        		KeyEvent.VK_ENTER, KeyEvent.VK_ENTER) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				sendQuery(openDocumentsModel.getSelectedDocument().getCurrentLine());
			}
		};

		String qlDesc = "Query Line and Move";
		qLineAndMoveAction = new AbstractAction(qlDesc, Theme.CIcon.SERVER_GO.get()) {
			@Override public void actionPerformed(ActionEvent e) {
				Document doc = openDocumentsModel.getSelectedDocument();
				String qry = doc.getCurrentLine();
				sendQuery("{x}"+qry, qry);
				doc.gotoNextLine();
			}
		};
		qLineAndMoveAction.putValue(AbstractAction.SHORT_DESCRIPTION, qlDesc);
		int modifier = InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK;
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, modifier);
//		qLineAndMoveAction.putValue(AbstractAction.MNEMONIC_KEY, mnemonic);	
		qLineAndMoveAction.putValue(AbstractAction.ACCELERATOR_KEY, k);

        qCancelQueryAction = new ShortcutAction(Msg.get(Key.CANCEL_QUERY),
        		Theme.CIcon.CANCEL, "Cancel the latest query",
        		KeyEvent.VK_C, KeyEvent.VK_K) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				queryManager.cancelQuery();
			}
		};

        sendClipboardQuery = new ShortcutAction("Send clipboard as query",
        		Theme.CIcon.EDIT_PASTE, "Send clipboard as query",
        		KeyEvent.VK_B, KeyEvent.VK_B) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				String qry;
				try {
					qry = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
					if(qry != null && qry.length() > 0) {
						sendQuery(qry);
					}
				} catch (IOException e1) {
					LOG.log(Level.WARNING, "Send Query Error", e1);
					growler.showSevere(e1.getMessage(), Msg.get(Key.ERROR));
				} catch (HeadlessException e1) {
					LOG.log(Level.WARNING, "Send Query Error", e1);
					growler.showSevere(e1.getMessage(), Msg.get(Key.ERROR));
				} catch (UnsupportedFlavorException e1) {
					LOG.log(Level.WARNING, "Send Query Error", e1);
					growler.showSevere(e1.getMessage(), Msg.get(Key.ERROR));
				}
			}
		};
		
		watchDocExpressionAction = new ShortcutAction("Add as Watched Expression",
						CIcon.EYE, "Add the selected text as a watched expression.") {
							private static final long serialVersionUID = 1L;

					@Override public void actionPerformed(ActionEvent e) {
						String qry = openDocumentsModel.getSelectedDocument()
								.getSelectedText();
						queryManager.addWatchedExpression(qry);
					}
				};				
				
		queryActions = Collections.unmodifiableList(Arrays.asList(qAction, 
				qLineAction, qLineAndMoveAction, sendClipboardQuery, qCancelQueryAction));

		
		proActions = Collections.unmodifiableList(Arrays.asList(loadScriptAction, profileAction, unitTestAction));
		
		queryManager.addQueryListener(new QueryAdapter() {
			@Override public void selectedServerChanged(String server) {
				refreshQButtonEnabledFlags();
			}
			
			@Override public void sendingQuery(ServerConfig sc, String query) {
				qCancelQueryAction.setEnabled(true);
			}
			
			@Override public void queryResultReturned(ServerConfig sc, QueryResult queryResult) {
				qCancelQueryAction.setEnabled(false);
			}
		});
		
		/******************************************************************************
		 * Server Actions
		 */
		addServerListAction = new AddServerListAction(parent, connectionManager);
		 
		removeServerAction = new AbstractAction(Msg.get(Key.REMOVE_ALL_SERVERS), Theme.CIcon.DELETE.get16()) {
				
			@Override public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog(parent, "Delete all Server Connections?",
						Msg.get(Key.WARNING), JOptionPane.WARNING_MESSAGE);
				if(choice == JOptionPane.YES_OPTION) {
					connectionManager.removeServers();
				}
			}
		};
		
		copyServerListAction = new AbstractAction(Msg.get(Key.COPY_SERVER_LIST_TO_CLIPBOARD), CIcon.EDIT_COPY.get16()) {
			
			@Override public void actionPerformed(ActionEvent e) {
				String s = ConnectionShortFormat.compose(connectionManager.getServerConnections(), 
						JdbcTypes.KDB);
				StringSelection sel = new StringSelection(s);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
			}
		};

		serverActions = Collections.unmodifiableList(Arrays.asList(addServerAction, 
				addServerListAction, copyServerListAction, removeServerAction, copyServerHopenToClipboardAction, editCurrentServerAction));
	}

	/** 
	 * Execute the entire text of the current document, then fix the namespace
	 * of all functions. Bug in Kdb that remotely executed code leaves
	 * functions defined in the global namespace. 
	 */
	private void runScriptModuleLoad() {
		String qry = null;
		try {
			Document doc = openDocumentsModel.getSelectedDocument();
			qry = ModuleRunner.getRunScriptModuleLoad(doc.getContent());
			sendQuery(qry, "Loading -> " + doc.getTitle());
		} catch(IOException io) {
			growler.showWarning("Could not load script loader module", "Module Load Fail"); 
		}
	}

	private void runQUnitTests() {
		// check license, show warning if necessary then perform action
		if(QLicenser.requestPermission(QLicenser.Section.UNIT_TEST)) {
			
			final Persistance.Key warningKey = Persistance.Key.SHOW_QDOC_WARNING;
			String msgHtml = "<a href='" + TimeStored.Page.QUNIT_HELP.url() + "'>QUnit</a> is a framework for implementing testing in kdb. <br/> <br/>" + 
					"Running qunit tests involves: <br/>" +
					"1. Loading the qunit module into the .qunit namespace. <br/>" +
					"2. Loading all test functions within the currently selected document. <br/>" +
					"3. Running the tests, checking all assertions are met. <br/>" +
					"4. Reporting a table of results. <br/>" +
					"<br/>" +
					"Tests must be properly structured and more help can be found on the " +
					"<a href='" + TimeStored.Page.QSTUDIO_HELP_QUNIT.url() + "'>qunit help page</a><br/><br/>" + 
							"<b>Run the qunit tests?</b>";
			final String title = "Load and Run .qunit module";
			
			int choice = showDismissibleWarning(persistance, warningKey, msgHtml, title);
			
			if(choice == JOptionPane.OK_OPTION) {
				String qry = null;
				try {
					String testq = openDocumentsModel.getSelectedDocument().getContent();
					try {
						qry = ModuleRunner.getRunQUnitQuery(testq);
					} catch(IllegalArgumentException iae) {
						String message = "no namespace found in this file";
						JOptionPane.showMessageDialog(null, message);
					}
				} catch(IOException io) {
					growler.showWarning("Could not load testing module", "Module Load Fail"); 
				}
				if(qry != null) {
					String t = openDocumentsModel.getSelectedDocument().getTitle();
					sendQuery(qry, "TEST file -> " + t);
				}
			}
		}
	}

	/**
	 * Show the user a warning about carrying out this particular action.
	 * A checkbox is present to not show the warning again by storing a key
	 * in the persistance.
	 * @return JOptionPane constant as returned by { @link {@link JOptionPane#showOptionDialog}
	 * 		e.g. JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
	 */
	public static int showDismissibleWarning(final Persistance persistance, 
			final Persistance.Key warningKey, String msgHtml, final String title) {
		
		int choice = JOptionPane.OK_OPTION;
		if(persistance.getBoolean(warningKey, true)) {
		
			final JCheckBox showWarningCheckBox = new JCheckBox("Do not show this information again");
			showWarningCheckBox.setName("checky");
			showWarningCheckBox.addActionListener(new ActionListener() {
				
				@Override public void actionPerformed(ActionEvent e) {
					persistance.putBoolean(warningKey, !showWarningCheckBox.isSelected());
				}
			});
			JPanel msg = new JPanel(new BorderLayout());
			msg.add(Theme.getHtmlText(msgHtml), BorderLayout.CENTER);
			msg.add(showWarningCheckBox, BorderLayout.SOUTH);
			
			choice = JOptionPane.showConfirmDialog(null, 
					msg, title, 
					JOptionPane.OK_CANCEL_OPTION);
		}
		return choice;
	}
	
	private void refreshQButtonEnabledFlags() {
		
		boolean serverAvailable = queryManager.getSelectedServerName()!=null;
		qAction.setEnabled(serverAvailable);
		qLineAction.setEnabled(serverAvailable);
		qCancelQueryAction.setEnabled(serverAvailable);
		watchDocExpressionAction.setEnabled(serverAvailable);
		copyServerHopenToClipboardAction.setEnabled(serverAvailable);
		editCurrentServerAction.setEnabled(serverAvailable);
	}
	
	/** attempt to send query, if it fails, report to user */
	private void sendQuery(String qry) {
		sendQuery(qry, null);
	}


	/**
	 * Send a query to the currently selected server and notify listeners of the result.
	 * Watched expressions will also be updated.
	 * @param qry The actual kdb query that is sent.
	 * @param queryTitle The query reported as being sent, useful for hiding long internal queries.
	 * 	null makes this default to actual query.
	 */
	public synchronized void sendQuery(final String qry, final String queryTitle) {
		if(connectionManager.isEmpty()) {
			JOptionPane.showMessageDialog(parent, Msg.get(Key.NO_CONNECTIONS_DEFINED) +
					Msg.get(Key.PLEASE_ADD_SERVER_CONNECTION),
					Msg.get(Key.NO_CONNECTIONS),
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(queryManager.isQuerying()) {
			String msg = "A query is currently in progress. \r\nYou must cancel that query before sending another."
					+ "\r\n\r\nIf you want to query multiple servers simultaneously, open multiple qStudio instances.";
			String title = "Query in progress";
			JOptionPane.showMessageDialog(parent, msg,
					title, JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		try {
			LOG.info("Send Query->" + qry);
			queryManager.sendQuery(qry, queryTitle);
		} catch(IllegalStateException ia) {
			LOG.log(Level.WARNING, "Send Query Error", ia);
			growler.showSevere(ia.getMessage(), Msg.get(Key.ERROR));
		} catch(Exception ex) {
			LOG.log(Level.WARNING, "Send Query Error", ex);
			growler.showSevere("Problem sending query to server", Msg.get(Key.ERROR)); 
		}
	}

	/**
	 * @return all actions related to sending queries.
	 */
	public List<Action> getQueryActions() {
		return queryActions;
	}
	
	/** get actions only supported by pro version of qStudio - unit testing, csv loading etc. **/
	public List<Action> getProActions() { return proActions; }
	
	/**
	 * @return action that sends the currently highlighted text to q server.
	 */
	Action getqAction() { return qAction; }
	

	/** @return action that sends the current text line to q server. */
	public Action getqLineAction() { return qLineAction; }
	
	/** @return Action to cancel currently sent query. */
	public Action getqCancelQueryAction() { return qCancelQueryAction; }
	

	/**
	 * @return action that shows dialogue to allow editing a selected {@link ServerConfig}.
	 */
	public Action getEditServerAction(ServerConfig sc) {
		return new EditServerAction(sc);
	}
	
	/**
	 * @return action that shows dialogue to allow adding a {@link ServerConfig}.
	 */
	public Action getAddServerAction() { return addServerAction; }
	
	public Action getCloseConnServerAction(ServerConfig sc) { return new CloseConnServerAction(sc); }

	/**
	 * @return action that shows dialogue to allow adding a {@link ServerConfig}
	 * 	that is a copy of an existing sc. (name will be postfixed with ' copy')
	 */
	public Action getCloneServerAction(ServerConfig sc) {
		return new CloneServerAction(sc);
	}

	/**
	 * @return action that shows dialogue to allow adding a {@link ServerConfig}.
	 */
	public Action getAddServerAction(String folder) {
		Preconditions.checkNotNull(folder);
		return new AddServerAction(folder);
	}

	/** @return action that copies hopen command for current server to clipboard */
	public Action getCopyServerHopenToClipboardAction() {
		return copyServerHopenToClipboardAction;
	}

	/** @return action that edits the currently selected server */
	public Action getEditCurrentServerAction() {
		return editCurrentServerAction;
	}
	
	
	/**
	 * @return all actions related to modifying server connections.
	 */
	public List<Action> getServerActions() {
		return serverActions;
	}
	
	/** add a server to the {@link ConnectionManager} */
	private class EditServerAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private final ServerConfig sc;

		public EditServerAction(ServerConfig sc) {
			super(Msg.get(Key.EDIT) + sc.getName() + Msg.get(Key.CONNECTION)); 
			if(!connectionManager.isConnected(sc)) {
				ImageIcon ii = CIcon.SERVER_ERROR.get16();
				this.putValue(SMALL_ICON, ii);
				this.putValue(LARGE_ICON_KEY, ii);
			}
			
			this.sc = sc;
			
			putValue(SHORT_DESCRIPTION,	"Edit the server connection " + sc.getName());
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			new ConnectionManagerDialog(connectionManager, parent, sc.getName()).setVisible(true);
		}
	}

	/** add a server to the {@link ConnectionManager} */
	private class AddServerAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private final String folder;

		public AddServerAction(String folder) {
			super(Msg.get(Key.ADD_SERVER), Theme.CIcon.SERVER_ADD.get());
			putValue(SHORT_DESCRIPTION,
					"Add a server to the list of possible connections");
			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
			this.folder = folder;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ConnectionManagerDialog cmd = new ConnectionManagerDialog(connectionManager, parent, null);
			if(folder!=null) {
				cmd.setFolder(folder);
			}
			cmd.setVisible(true);
		}
	}
	
	/** Close all server connections */
	private class CloseConnServerAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private final ServerConfig sc;

		public CloseConnServerAction(ServerConfig sc) {
			super(Msg.get(Key.CLOSE_CONNECTION), Theme.CIcon.SERVER_LIGHTNING.get());
			putValue(SHORT_DESCRIPTION,
					"Close all connections to this database.");
			this.sc = sc;
		}
		
		@Override public void actionPerformed(ActionEvent arg0) {
			connectionManager.closePool(sc);
		}
	}
	

	/** Clone a server to the {@link ConnectionManager} */
	private class CloneServerAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private final ServerConfig sc;

		public CloneServerAction(ServerConfig sc) {
			super(Msg.get(Key.CLONE_SERVER), Theme.CIcon.COPY.get());
			putValue(SHORT_DESCRIPTION,
					"Create a new connection with the same settings as this one.");
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			this.sc = sc;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String newName = sc.getName() + " " + Msg.get(Key.COPY);
			ServerConfig sc2 = new ServerConfigBuilder(sc).setName(newName).build();
			connectionManager.addServer(sc2);
			ConnectionManagerDialog cmd = new ConnectionManagerDialog(connectionManager, parent, newName);
			cmd.setVisible(true);
		}
	}
	
	
	/**
	 * @param serverConfig The server that should be removed
	 * @return Action to remove a selected server from the connectionManager
	 */
	public Action getRemoveServerAction(final ServerConfig serverConfig) {
		return new AbstractAction(Msg.get(Key.DELETE_CONNECTION),
				CIcon.DELETE.get16()) {
					private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				connectionManager.removeServer(serverConfig);
			}

			@Override public boolean isEnabled() {
				return connectionManager.contains(serverConfig);
			}
		};
	}


	/**
	 * @return Common commands including querying, tools, server actions.
	 */
	@Override public Collection<Command> getCommands() {
		List<Command> a = new ArrayList<Command>();
		a.addAll(CommandManager.toCommands(queryActions));
		a.addAll(CommandManager.toCommands(proActions));
		a.addAll(CommandManager.toCommands(serverActions));
		return a;
	}

}
