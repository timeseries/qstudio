package com.timestored.qstudio.servertree;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.common.base.Preconditions;

/**
 * Represents a node in a tree that has text / icon / tooltip, can action when
 * clicked and has a right click popup menu. You must call the
 * #configure function on whatever tree you have created to contain CustomNode's
 * to get the full benefit.
 */
class CustomNode {

	private String title;
	private String tooltip;
	private ImageIcon icon;
	private static final DefaultTreeCellRenderer RENDERER = new CustomNodeRenderer();

	public CustomNode(String title) {
		this(title, null, null);
	}

	public CustomNode(String title, String tooltip, ImageIcon icon) {
		this.title = Preconditions.checkNotNull(title);
		this.tooltip = tooltip;
		this.icon = icon;
	}

	protected void setText(String title) {
		this.title = title;
	}

	public String getText() {
		return title;
	}

	public String getTooltip() {
		return tooltip;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public void doSelectionAction() {
	};

	/** Add the actions this item supports to a menu */
	public void addMenuItems(JPopupMenu menu) { 	}

	
	/**
	 * Configure a jtree to display CustomNodes, ie single selection only allowed,
	 * display their menu on right click, left click to perform action.
	 * @param tree
	 */
	public static void configure(JTree tree) {

	    ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(RENDERER);
	    
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(new CustomNodeTreeSelectionListener(tree));
	    tree.addMouseListener(new CustomNodePopupMenuListener(tree));
	}
	
	
	/**
	 * Checks if node is actual a {@link CustomNode} and if so displays appropriately
	 */
	private static class CustomNodeRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		public CustomNodeRenderer() { }

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			
			setToolTipText(null); // no tool tip
			if (value instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if(node.getUserObject() instanceof CustomNode) {
					CustomNode cn = (CustomNode) node.getUserObject();
					setText(cn.getText());
					setToolTipText(cn.getTooltip());
					if(cn.getIcon()!=null) {
						setIcon(cn.getIcon());
					}
				}
			}

			return this;
		}
	}

	private static class CustomNodeTreeSelectionListener implements TreeSelectionListener {

		private final JTree tree;
		
		public CustomNodeTreeSelectionListener(JTree tree) {
			this.tree = tree;
		}
		
		@Override public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (node != null && node.getUserObject() instanceof CustomNode) {
					((CustomNode) node.getUserObject()).doSelectionAction();
			}
		}
		
	}

	/**
	 * USes the {@link CustomNode} to get get what menu should be shown.
	 */
	private static class CustomNodePopupMenuListener extends MouseAdapter {
		
		private final JTree tree;
		
		public CustomNodePopupMenuListener(JTree tree) {
			this.tree = tree;
		}
		
		@Override public void mouseClicked(MouseEvent e) {
			handle(e);
		}

		@Override public void mousePressed(MouseEvent e) {
			handle(e);
		}

		@Override public void mouseReleased(MouseEvent e) {
			handle(e);
		}

		private void handle(MouseEvent e) {
			CustomNode cNode = getCustomNodeClickedOn(e);
			if (cNode != null && e.isPopupTrigger()) {
				showPopupMenu(cNode, e);
			}
		}

		/** @return The node of the tree clicked on if any otherwise null. **/
		private CustomNode getCustomNodeClickedOn(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int selRow = tree.getRowForLocation(x,y);
			if (selRow != -1) {
				TreePath selPath = tree.getPathForLocation(x,y);
				DefaultMutableTreeNode curNode;
				curNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
				Object obj = curNode.getUserObject();
				if (obj instanceof CustomNode){
					return ((CustomNode) obj);
				}
			}
			return null;
		}
		
		private void showPopupMenu(CustomNode cNode, MouseEvent e) {
			
			JPopupMenu menu = new JPopupMenu("Popup");

			cNode.addMenuItems(menu);
			
			if(menu.getComponentCount() > 0) {
				menu.show(e.getComponent(), e.getX(), e.getY());
				menu.setVisible(true);
			}
		}
	}
}