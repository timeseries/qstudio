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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.timestored.qstudio.kdb.KdbHelper;
import com.timestored.qstudio.model.QueryAdapter;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.WatchedExpression;
import com.timestored.theme.Theme;

/**
 * Displays watched expressions, database queries in dense table format
 * and updates them any time the user sends a query.
 */
class WatchedExpressionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final QueryManager queryManager;
	private final JPanel elementListPanel;
	private final JPanel elementDetailPanel;


	WatchedExpressionPanel(QueryManager queryManager) {
		this.queryManager = queryManager;
		setLayout(new BorderLayout());
		
		elementListPanel = new JPanel(new BorderLayout());
		elementDetailPanel = new JPanel(new BorderLayout());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				elementListPanel, elementDetailPanel);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
		
		// we only care about watched expressions
		queryManager.addQueryListener(new QueryAdapter() {
			@Override public void watchedExpressionsModified() {
				refreshExpressions();
			}

			@Override public void watchedExpressionsRefreshed() {
				refreshExpressions();
			}
		});
		refreshExpressions();
	}

	/**
	 * Recreate table listing watchables etc.
	 */
	private void refreshExpressions() {
		
		final List<WatchedExpression> watchedExps = queryManager.getWatchedExpressions();

		TableModel model = new WatchedExpressionTableModel(queryManager);
		final JXTable table = new JXTable(model);

		if(!watchedExps.isEmpty()) {
			// allow deletion upon right click
			table.addMouseListener(new DeleteRowMouseAdapter(table, queryManager));
			
			// show details of the selected watched expression
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override public void valueChanged(ListSelectionEvent arg0) {
					int row = table.getSelectedRow();
					if(row >= 0 && row < watchedExps.size()) {
						Object res = watchedExps.get(row).getLastResult();
						Component resultComponent = KdbHelper.getComponent(res);
						
						elementDetailPanel.removeAll();
						elementDetailPanel.add(resultComponent, BorderLayout.CENTER);
						elementDetailPanel.revalidate();
					}
				}
			});
		}
		
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				elementListPanel.removeAll();
				elementListPanel.add(new JScrollPane(table), BorderLayout.CENTER);
				elementListPanel.revalidate();
			}
		});
		
	}

	/**
	 * table with one watched expression per line, that also allows adding more.
	 */
	private static class WatchedExpressionTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private static final String[] colNames = new String[] { "Expression", "value" };
		private static final String DEF_TEXT = "Add Expression +"; 
		
		private final QueryManager queryManager;
		private final List<WatchedExpression> watchedExps;
		 
		
		public WatchedExpressionTableModel(QueryManager queryManager) {
			this.queryManager = queryManager;
			watchedExps = queryManager.getWatchedExpressions();
		}
		
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}
		 
		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			return watchedExps.size() + 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if(row < watchedExps.size()) {
				WatchedExpression we = watchedExps.get(row);
				if(col == 0) {
					return we.getExpression();
				} else if(col == 1) {
			    	String res = KdbHelper.asLine(we.getLastResult());
					return res==null ? "" : res;
				}
			} else {
				if(col == 0) {
					return DEF_TEXT;
				}
			}
			return "";
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			String exp = ((String) value).trim();
			if(exp.length()>0 && !exp.equals(DEF_TEXT)) {
				if(row < watchedExps.size()) {
					queryManager.setWatchedExpression(row, (String) value);
				} else if(row == watchedExps.size()) {
					queryManager.addWatchedExpression((String)value);
				}
			}
			super.setValueAt(value, row, col);
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			return col < 1;
		}
	}	
	
	
	/**
	 * Allow removing a watched expression from right click popup menu on table.
	 */
	private static class DeleteRowMouseAdapter extends MouseAdapter {

		private final JXTable table;
		private final QueryManager queryManager;

		public DeleteRowMouseAdapter(JXTable table, QueryManager queryManager) {
			this.table = table;
			this.queryManager = queryManager;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			int r = table.rowAtPoint(e.getPoint());
			if (r >= 0 && r < table.getRowCount()) {
				table.setRowSelectionInterval(r, r);
			} else {
				table.clearSelection();
			}

			final int rowindex = table.getSelectedRow();
			if (e.isPopupTrigger() && rowindex >= 0
					&& rowindex < queryManager.getWatchedExpressions().size()) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem deleteExp = new JMenuItem("Remove", Theme.CIcon.TABLE_ROW_DELETE.get16());
				deleteExp.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						queryManager.removeWatchedExpression(rowindex);
					}
				});
				JMenuItem deleteAll = new JMenuItem("Remove All", Theme.CIcon.DELETE.get16());
				deleteAll.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						queryManager.clearWatchedExpressions();
					}
				});
				popup.add(deleteExp);
				popup.add(deleteAll);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	

}
