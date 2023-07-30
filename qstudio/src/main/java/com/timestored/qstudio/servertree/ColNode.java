package com.timestored.qstudio.servertree;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.timestored.TimeStored;
import com.timestored.misc.HtmlUtils;
import com.timestored.qstudio.CommonActions;
import com.timestored.qstudio.Persistance;
import com.timestored.qstudio.Persistance.Key;
import com.timestored.qstudio.QLicenser;
import com.timestored.qstudio.QLicenser.Section;
import com.timestored.qstudio.kdb.DatabaseManager;
import com.timestored.qstudio.model.AdminModel;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.ServerModel;
import com.timestored.qstudio.model.TableSQE;
import com.timestored.qstudio.qtheme.QTheme;
import com.timestored.qstudio.qtheme.QTheme.QIcon;
import com.timestored.theme.Icon;
import com.timestored.theme.Theme;
import com.timestored.tscore.persistance.PersistanceInterface;

/** 
 * Represents a single column on a given server/table.
 * Allows actions to modify columns.
 */
class ColNode extends CustomNode {

	private static final Logger LOG = Logger.getLogger(CustomNode.class.getName());

	private final QueryManager queryManager;
	private final AdminModel adminModel;
	
	private final ServerModel serverModel;
	private final TableSQE table;
	private final String column;
	private final boolean partitionColumn;
	
	public ColNode(QueryManager queryManager, AdminModel adminModel,
			ServerModel serverModel, TableSQE table, 
			String column, boolean partitionColumn) {
		
		super(column);
		this.queryManager = queryManager;
		this.adminModel = adminModel;
		this.serverModel = serverModel;
		this.table = table;
		this.column = column;
		this.partitionColumn = partitionColumn;
	}
	
	@Override public void addMenuItems(JPopupMenu menu) {
		if(partitionColumn) {
			JMenuItem mi = new JMenuItem("This is the partition column.");
			mi.setEnabled(false);
			menu.add(mi);
		} else {
			
			// title at top hack
			menu.add(new AbstractAction("Column: " + column, Theme.CIcon.TREE_ELEMENT.get16()) {
					private static final long serialVersionUID = 1L;
				@Override public void actionPerformed(ActionEvent e) { }
				@Override public boolean isEnabled() { return false; }
			});
			
			menu.addSeparator();
			
			if(QLicenser.isPermissioned(Section.DBMAINTENANCE)) {
				menu.add(ww(new DeleteAction()));
				menu.add(ww(new CopyAction()));
				menu.add(ww(new RenameAction()));
				
				JMenu attribsMenu = new JMenu("Set Attribute");
				attribsMenu.add(ww(new AttributeAction("Clear", " ",QIcon.ATTRIB_N)));
				attribsMenu.add(ww(new AttributeAction("Grouped", "g", QIcon.ATTRIB_G)));
				attribsMenu.add(ww(new AttributeAction("Parted", "p", QIcon.ATTRIB_P)));
				attribsMenu.add(ww(new AttributeAction("Sorted", "s", QIcon.ATTRIB_S)));
				attribsMenu.add(ww(new AttributeAction("Unique", "u", QIcon.ATTRIB_U)));
				menu.add(attribsMenu);
			} else {
				addFakeActions(menu);
			}
			
			menu.addSeparator();
			
			menu.add(HtmlUtils.getWWWaction("DB Management Help", 
					TimeStored.Page.QSTUDIO_HELP_DBMANAGE.url()));
		}
	}
	
	/**
	 * Wrap an action to check its license and display informational message
	 * related to db management. 
	 */
	private Action ww(final Action a) {
		javax.swing.Icon i = (javax.swing.Icon) a.getValue(Action.SMALL_ICON);
		String nm = (String) a.getValue(Action.NAME);
		AbstractAction wrapAct = new AbstractAction(nm, i) {
			@Override public void actionPerformed(ActionEvent e) {
				if(QLicenser.requestPermission(Section.DBMAINTENANCE)) {


					final Key warningKey = Persistance.Key.SHOW_DBM_WARNING;
					String msgHtml = "<a href='" + TimeStored.Page.QSTUDIO_HELP_DBMANAGE.url() + "'>Database Management</a> requires loading a module into the .dbm namespace of the selected server.<br/> <br/>" +
							"You are advised to read the help pages before making major database changes, <br/>" +
							"Some important advice includes:<br/>" +
							"1. Make a backup before performing any changes.<br/>" +
							"2. Expect on-disk changes to take a long time, see the console for output on work progress<br/>" +
							"3. Make sure the database is freshly loaded to prevent files being locked by queries or variables.<br/>" +
							"4. If the database is part of a tick setup, remember to change the config files.<br/>" +
							"<br/>" +
							"More help can be found on the " +
							"<a href='" + TimeStored.Page.QSTUDIO_HELP_DBMANAGE.url() + "'>database management help page</a><br/><br/>" +
									"<b>Perform the database operation?</b>";
					final String title = "Load and Run .dbm module";
					
					int choice = CommonActions.showDismissibleWarning(Persistance.INSTANCE, warningKey, msgHtml, title);
					
					if(choice == JOptionPane.OK_OPTION) {
						a.actionPerformed(e);
					}
				}
			}
		}; 
		return wrapAct;
	}

	private class CopyAction extends AbstractAction {
		public CopyAction() {super("Copy Column", Theme.CIcon.COPY.get16());	}

		@Override public void actionPerformed(ActionEvent e) {
			String newCol = JOptionPane.showInputDialog("Enter name for new column:");
			if(newCol != null) {
				String q = DatabaseManager.getCopyColumnQuery(table.getName(), column, newCol);
				sendWaitRefresh("Copy Column: " + column, q);
			}
		}
	}

	private class RenameAction extends AbstractAction {
		public RenameAction() {super("Rename Column", Theme.CIcon.RENAME.get16());	}

		@Override public void actionPerformed(ActionEvent e) {
			String newCol = JOptionPane.showInputDialog("Enter name for new column:");
			if(newCol != null) {
				String q = DatabaseManager.getRenameColumnQuery(table.getName(), column, newCol);
				sendWaitRefresh("Rename Column: " + column, q);
			}
		}
	}

	private class AttributeAction extends AbstractAction {
		private final String attrib;
		public AttributeAction(String label, String attrib, QTheme.QIcon icon) {
			super(label, icon.get16());
			this.attrib = attrib;
		}

		@Override public void actionPerformed(ActionEvent e) {
				String q = DatabaseManager.getSetAttributeColumnQuery(table.getName(), column, attrib);
				sendWaitRefresh("Set Attribute " + getText() + ": " + column, q);
		}
	}

	private class DeleteAction extends AbstractAction {

		final String query;
		
		public DeleteAction() {
			super("Delete Column", Theme.CIcon.DELETE.get16());
			query = DatabaseManager.getDeleteColumnQuery(table.getName(), column);
		}
		
		@Override public boolean isEnabled() { return query != null;	}
		
		@Override public void actionPerformed(ActionEvent e) {
			sendWaitRefresh("Delete Column: " + column, query);
		}
		
	}


	private void sendWaitRefresh(String actionTitle, String query) {
		if(query == null) {
			JOptionPane.showMessageDialog(null, actionTitle + " is not possible.");
		} else {
			queryManager.setSelectedServerName(serverModel.getName());
			queryManager.sendQuery(query, actionTitle);
			// todo remove this hack and put in completion listener
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e1) {
				LOG.warning("problem waiting 1354");
			}
			adminModel.refresh(serverModel.getServerConfig());
		}
	}

	
	private static void addFakeActions(JPopupMenu menu) {
		menu.add(getProAction("Delete Column", Theme.CIcon.DELETE));
		menu.add(getProAction("Copy Column", Theme.CIcon.COPY));
		menu.add(getProAction("Rename Column", Theme.CIcon.RENAME));
		JMenu attribsMenu = new JMenu("Set Attribute");
		attribsMenu.add(getProAction("Clear", QIcon.ATTRIB_N));
		attribsMenu.add(getProAction("Grouped", QIcon.ATTRIB_G));
		attribsMenu.add(getProAction("Parted", QIcon.ATTRIB_P));
		attribsMenu.add(getProAction("Sorted", QIcon.ATTRIB_S));
		attribsMenu.add(getProAction("Unique", QIcon.ATTRIB_U));
		menu.add(attribsMenu);
	}



	/** Get an action that is only implemented in the pro/paid for version of qStudio */
	private static JMenuItem getProAction(String name, Icon icon) {
		return new JMenuItem(new AbstractAction(name, icon.get16()) {
			@Override public void actionPerformed(ActionEvent e) {
				HtmlUtils.browse(TimeStored.URL + "/qStudio/pro");
			}
		});
	}
}