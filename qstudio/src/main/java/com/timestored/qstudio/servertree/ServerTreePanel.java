package com.timestored.qstudio.servertree;


import static com.timestored.theme.Theme.CENTRE_BORDER;
import static com.timestored.theme.Theme.GAP;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.collect.ImmutableList;
import com.timestored.connections.ConnectionManager;
import com.timestored.connections.ServerConfig;
import com.timestored.docs.OpenDocumentsModel;
import com.timestored.qstudio.CommonActions;
import com.timestored.qstudio.model.AdminModel;
import com.timestored.qstudio.model.AdminModel.Category;
import com.timestored.qstudio.model.QEntity;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.ServerModel;
import com.timestored.qstudio.model.ServerObjectTree;
import com.timestored.qstudio.model.ServerQEntity;
import com.timestored.qstudio.model.ServerQEntity.QQuery;
import com.timestored.qstudio.model.TableSQE;
import com.timestored.theme.Icon;
import com.timestored.theme.Theme;

/**
 * Displays all servers in a {@link ConnectionManager} and their tables / functions / variables
 * as items in sub trees. The user can select items or right click and take actions on certain
 * elements.
 */
public class ServerTreePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final ObjectTreePanel objectTreePanel;
	private final ServerListPanel serverListPanel;
	
	public ServerTreePanel(final AdminModel adminModel, 
			final QueryManager queryManager,
			final CommonActions commonActions,
			OpenDocumentsModel openDocumentsModel, JFrame parentFrame) {

		serverListPanel = new ServerListPanel(adminModel, commonActions, parentFrame);
		objectTreePanel = new ObjectTreePanel(adminModel, queryManager, openDocumentsModel);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				serverListPanel, objectTreePanel);
		splitPane.setResizeWeight(0.4);
		
		setLayout(new BorderLayout());
		add(splitPane);
		
	}
	
	@Override public void setTransferHandler(TransferHandler newHandler) {
		serverListPanel.setTransferHandler(newHandler);
		super.setTransferHandler(newHandler);
	}

	public void setHiddenNamespaces(Set<String> hiddenNS) {
		objectTreePanel.setHiddenNamespaces(hiddenNS);
	}
}

/**
 * Displays the objects that exist on a single {@link ServerModel} to update 
 * the display you must manually call {@link #refreshGui()}.
 */
class ObjectTreePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JTree tree;
	private Set<String> hiddenNS;

	private final AdminModel adminModel;
	private final QueryManager queryManager;
	private final OpenDocumentsModel openDocumentsModel;
	// variable used to make expanding default namespace nodes easier
	private final List<DefaultMutableTreeNode> defaultNSnodes = new ArrayList<DefaultMutableTreeNode>();

	
	private ServerModel curServerModel;

	public ObjectTreePanel(AdminModel adminModel, QueryManager queryManager,
			OpenDocumentsModel openDocumentsModel) {
		
		this.adminModel = adminModel;
		this.queryManager = queryManager;
		this.openDocumentsModel = openDocumentsModel;
		setLayout(new BorderLayout(GAP,GAP));
		
		adminModel.addListener(new AdminModel.Listener() {
			
			@Override public void selectionChanged(ServerModel serverModel, Category category,
					String namespace, QEntity element) { 
				if(serverModel != curServerModel) { refreshGui(); }
			}
			
			@Override public void modelChanged() { refreshGui();  }
			@Override public void modelChanged(ServerModel sm) {  refreshGui(); }
		});
		refreshGui();
	}
	
	private void refreshGui() {
		
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				DefaultMutableTreeNode top = new DefaultMutableTreeNode();
				defaultNSnodes.clear();
				curServerModel = adminModel.getServerModel();
				if(curServerModel!=null) {
					ServerObjectTree soTree = curServerModel.getServerObjectTree();
					if (soTree != null) {
						addNodes(top, curServerModel, soTree, hiddenNS);
					}
				}
				
				tree = new JTree(top);   
				JScrollPane treeView = new JScrollPane(tree); 
				tree.setBorder(CENTRE_BORDER);
				CustomNode.configure(tree);

				// expand the default node folders
				for (DefaultMutableTreeNode dmtNode : defaultNSnodes) {
					// within default namespace expand functions / variables.
					tree.expandPath(new TreePath(dmtNode.getPath()));
				}
				
				removeAll();
				add(treeView, BorderLayout.CENTER);
				revalidate();
			}
		});
		
	}
	

	
	public void setHiddenNamespaces(Set<String> hiddenNS) {
		this.hiddenNS = hiddenNS;
		refreshGui();
	}


	private void addNodes(DefaultMutableTreeNode top, ServerModel sm,
			ServerObjectTree kdbServerObjects, Set<String> hiddenNS) {

		String serverName = sm.getName();
		String[] namespaces = kdbServerObjects.getNamespaces().toArray(new String[]{});
		Arrays.sort(namespaces);
		
		for(String ns : namespaces) {
			
			if(!hiddenNS.contains(ns)) {
			
				NamespaceNode nsNode = new NamespaceNode(ns, sm.getServerConfig());
				DefaultMutableTreeNode nsTree = new DefaultMutableTreeNode(nsNode, true);
				if(ns.equals(".")) {
					defaultNSnodes.add(nsTree);
				}
				for(TableSQE ed : kdbServerObjects.getTables(ns)) {

					DefaultMutableTreeNode branch = new DefaultMutableTreeNode(new ServerQEntityNode(serverName, ed));
					int i = ed.isPartitioned() ? 0 : 1;
					for(String cn : ed.getColNames()) {
						ColNode colNode = new ColNode(queryManager, adminModel, sm, ed, cn, i++==0);
						branch.add(new DefaultMutableTreeNode(colNode));
					}
					nsTree.add(branch);
				}
				for(ServerQEntity ed : kdbServerObjects.getViews(ns)) {
					nsTree.add(new DefaultMutableTreeNode(new ServerQEntityNode(serverName, ed)));
				}
				nsTree.add(getBranch(serverName, "Functions", kdbServerObjects.getFunctions(ns)));
				nsTree.add(getBranch(serverName, "Variables", kdbServerObjects.getVariables(ns)));
				top.add(nsTree);
			}
		}
	}
	

	/** Represents KDB namespace */
	private class NamespaceNode extends CustomNode {
		private final String ns;
		private final ServerConfig serverConfig;

		public NamespaceNode(String ns, ServerConfig serverConfig) {
			super(ns);
			this.ns = ns;
			this.serverConfig = serverConfig;
		}

		@Override public void doSelectionAction() {
			adminModel.setSelectedNamespace(serverConfig.getName(), ns);
		}
		
		@Override public void addMenuItems(JPopupMenu menu) {
			QQuery qq = new QQuery("Delete all variables from Namespace", 
					Theme.CIcon.DELETE, "delete from `" + ns);
			addScriptMenu(menu, ImmutableList.of(qq));
		}
	}

	
	/** Tree node for an object that exists on the server */
	private class ServerQEntityNode extends CustomNode {

		private final ServerQEntity sqe;
		private final String serverName;

		public ServerQEntityNode(String serverName, ServerQEntity sqe) {
			super(sqe.getName(), sqe.getHtmlDoc(true), sqe.getIcon());
			this.sqe = sqe;
			this.serverName = serverName;
			if(sqe.getType().isList()) {
				setText("<html><b>" + sqe.getName() + "</b></html>");
			}
		}

		@Override public void doSelectionAction() {
			adminModel.setSelectedElement(serverName, sqe.getNamespace(), sqe);
		}
		
		@Override public void addMenuItems(JPopupMenu menu) {
			addScriptMenu(menu, sqe.getQQueries());
		}
	}
	

	/**
	 * Add a menu with the qquery actions and a separate sub menu that scripts them qQueries
	 * to the current editor.
	 * @param qQueries The q queries that will each have a menuItem added.
	 */
	private void addScriptMenu(JPopupMenu menu, List<QQuery> qQueries) {
		if(qQueries.size()>0) {
			JMenu scriptToWindowMenu = new JMenu("Script to Editor");
			scriptToWindowMenu.setIcon(Theme.CIcon.PAGE_CODE.get16());
			for(QQuery qquery : qQueries) {
				menu.add(new JMenuItem(new ElementAction(qquery, false)));
				scriptToWindowMenu.add(new JMenuItem(new ElementAction(qquery, true)));
			}
			menu.add(scriptToWindowMenu);
		}
	}

	private DefaultMutableTreeNode getBranch(String serverName, String branchTitle, 
			List<ServerQEntity> elements) {
		
		DefaultMutableTreeNode branch = new DefaultMutableTreeNode(branchTitle);
		for(ServerQEntity ed : elements) {
			branch.add(new DefaultMutableTreeNode(new ServerQEntityNode(serverName, ed)));
		}
		return branch;
	}

	
	private class ElementAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private final String query;
		private final boolean insertText;
		
		public ElementAction(QQuery qquery, boolean insertText) {
			
			super(qquery.getTitle());
			Icon ic = qquery.getIcon();
			if(ic!=null) {
				this.putValue(Action.SMALL_ICON, ic.get16());
			}
			query = qquery.getQuery();
			this.insertText = insertText;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(insertText) {
				openDocumentsModel.insertSelectedText(query);
			} else {
				queryManager.sendQuery(query);
			}
		}
	}
}





//private static class UserPermissionsNode extends CustomNode {
//
//	private static final String name = "User Permisions";
//	private static final String desc = "Configure which users can access which data / functions";
//	private static final ImageIcon icon =  QTheme.Icon.USER.get16();
//
//	private final ServerModel serverModel;
//	private final Component parentFrame;
//	
//	/**
//	 * Create a tree node that allows editing a servers user permissions.
//	 * @param parentFrame User for positioning any popup dialogs.
//	 */
//	public UserPermissionsNode(ServerModel serverModel, Component parentFrame) {
//		super(name, desc, icon);
//		this.serverModel = serverModel;
//		this.parentFrame = parentFrame;
//	}
//
//	@Override public void doSelectionAction() {
////		adminModel.setSelectedCategory(soTree.getName(), ".", 
////				AdminModel.Category.UNSELECTED);
//		showPermissionsDialog();
//	}
//	
//	@Override public void addMenuItems(JPopupMenu menu) {
//		menu.add(new JMenuItem(new AbstractAction(name, icon) {
//			@Override public void actionPerformed(ActionEvent e) {
//				showPermissionsDialog();
//			}
//		}));
//	}
//
//	private void showPermissionsDialog() {
//		JPanel p = new UserPermissionsPanel(new UserPermissionsModel(serverModel));
//		SwingUtils.showAppDialog(parentFrame, "User Permissions", p, 
//				QTheme.Icon.USER_SUIT.get().getImage());
//	}
//}

