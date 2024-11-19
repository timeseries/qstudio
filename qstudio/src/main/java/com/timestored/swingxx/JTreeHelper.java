package com.timestored.swingxx;

import java.util.Enumeration;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * JTree utility class for performing common actions.
 */
public class JTreeHelper {
	
	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public static void expandAll(JTree tree, boolean expand) {
	    TreeNode root = (TreeNode) tree.getModel().getRoot();
	 
	    // Traverse tree from root
	    expandAll(tree, new TreePath(root), expand);
	}
	 
	public static void expandAll(JTree tree, TreePath parent, boolean expand) {
	    // Traverse children
	    TreeNode node = (TreeNode) parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	        for (Enumeration e=node.children(); e.hasMoreElements();) {
	            TreeNode n = (TreeNode) e.nextElement();
	            TreePath path = parent.pathByAddingChild(n);
	            expandAll(tree, path, expand);
	        }
	    }
	 
	    // Expansion or collapse must be done bottom-up
	    if (expand) {
	        tree.expandPath(parent);
	    } else {
	        tree.collapsePath(parent);
	    }
	}
	

	/** 
	 * Expand all IdentifiableNode withing the tree contained in expandedFolders.
	 * For this to work, the nodes withing JTree that should be expanded MUST implement {@link IdentifiableNode}. 
	 **/
	public static void setFolderExpansions(JTree tree, Set<String> curExpandedFolders) {
		Preconditions.checkNotNull(tree);
		Preconditions.checkNotNull(curExpandedFolders);
		if(curExpandedFolders.size() > 0) {
			for(int row = tree.getRowCount(); row>=0; row--) {
				TreePath path = tree.getPathForRow(row);
				if(path!=null) {
					Object o = path.getLastPathComponent();
					if(o != null && o instanceof DefaultMutableTreeNode) {
						o = ((DefaultMutableTreeNode)o).getUserObject();
					}
					if(o!=null && o instanceof IdentifiableNode) {
						IdentifiableNode fn = (IdentifiableNode)o;
						String id = (fn).getId();
						if(id!=null && id.length()>0 && curExpandedFolders.contains(id)) {
							tree.expandRow(row);
						} else {
							tree.collapseRow(row);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Uniquely identifies a node so as to allow saving and restoring those nodes that were
	 * expanded, while allowing full refreshes. 
	 */
	public static interface IdentifiableNode {
		public String getId();
	}
	
	/** 
	 * @return List of ids within the tree that are expanded 
	 **/
	public static Set<String> getExpandedFolders(JTree tree) {
		Set<String> curExpandedFolders = Sets.newHashSet();
		if(tree!=null) {
			for(int row = tree.getRowCount(); row>=0; row--) {
				TreePath path = tree.getPathForRow(row);
				if(path!=null) {
					Object lpc = path.getLastPathComponent();
					if(lpc != null && lpc instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)path.getLastPathComponent();
						Object o = mtn!=null ? mtn.getUserObject() : null;
						if(o!=null && o instanceof IdentifiableNode && tree.isExpanded(row)) {
							curExpandedFolders.add(((IdentifiableNode)o).getId());
						}
					} else if(lpc != null && lpc instanceof IdentifiableNode) {
						curExpandedFolders.add(((IdentifiableNode)lpc).getId());
					}
				}
			}
		}
		return curExpandedFolders;
	}
}
