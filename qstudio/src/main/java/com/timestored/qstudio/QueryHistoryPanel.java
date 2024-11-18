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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import com.google.common.base.Preconditions;
import com.timestored.connections.ServerConfig;
import com.timestored.qstudio.kdb.KdbHelper;
import com.timestored.qstudio.model.QueryAdapter;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.QueryResult;
import com.timestored.theme.Theme;

/**
 * Display the last {@link #FIXED_SIZE} queries sent 
 * and allow seeing old results and resending. 
 */
class QueryHistoryPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int FIXED_SIZE = 5;
	
	private final QueryManager queryManager;
	private final List<QueryResult> history = new LinkedList<QueryResult>();
	private final String[] colNames = new String[] { "Query", "Result", "Success" };
	private final DefaultTableModel tableModel;
	private final JPanel elementDetailPanel;
	private final JXTable table;
	
	private int maxRowsShown = Integer.MAX_VALUE;

	QueryHistoryPanel(QueryManager queryManager) {
		
		this.queryManager = queryManager;
		setLayout(new BorderLayout());
		
		tableModel = new DefaultTableModel(colNames, 0);

		table = Theme.getStripedTable(tableModel);
		table.setEditable(false);
		
		final JScrollPane historyScrollPane = new JScrollPane(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		historyScrollPane.setViewportView(table);

		elementDetailPanel = new JPanel(new BorderLayout());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				historyScrollPane, elementDetailPanel);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
		
		// show details of the selected history expression
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override public void valueChanged(ListSelectionEvent arg0) {
				int row = table.getSelectedRow();
				if(row >= 0 && row < history.size()) {
					display(row);
				}
			}
		});
		
		table.addMouseListener(new PopupMenuMouseListener());
		
		queryManager.addQueryListener(new QueryAdapter() {

			@Override public void queryResultReturned(ServerConfig sc, QueryResult queryResult) {
				addToHistory(queryResult);
				display(history.size()-1);
			}
		});
	}

	/** display the query result details for the selected history row **/
	private void display(int row) {
		final Component c = KDBResultPanel.getComponent(history.get(row), maxRowsShown, queryManager, false);
		
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				elementDetailPanel.removeAll();
				elementDetailPanel.add(c, BorderLayout.CENTER);
				elementDetailPanel.revalidate();
			}
		});
	}
	private class PopupMenuMouseListener extends MouseAdapter {
		
		@Override public void mouseReleased(MouseEvent e) {
			int r = table.rowAtPoint(e.getPoint());
			if (r >= 0 && r < table.getRowCount()) {
				table.setRowSelectionInterval(r, r);
			} else {
				table.clearSelection();
			}

			final int row = table.getSelectedRow();
			if (e.isPopupTrigger() && row >= 0 && 
					row < history.size()) {
				
				JPopupMenu popup = new JPopupMenu();
				JMenuItem resendExp = new JMenuItem("Resend", Theme.CIcon.SERVER_GO.get16());
				resendExp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						queryManager.sendQuery(history.get(row).query);
					}
				});
				JMenuItem copyToClipboardButton = new JMenuItem("Copy Query to Clipboard", Theme.CIcon.EDIT_COPY.get16());
				copyToClipboardButton.addActionListener(ev -> {
					Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
					clpbrd.setContents(new StringSelection(history.get(row).query), null);
				});
				popup.add(resendExp);
				popup.add(copyToClipboardButton);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			int row = table.getSelectedRow();
			if(e.getClickCount()==2 && row >= 0 &&  row < history.size()) {
				queryManager.sendQuery(history.get(row).query);
			}
			super.mouseClicked(e);
		}
	}

	private void addToHistory(QueryResult qr) {
		if(history.size() == FIXED_SIZE) {
			history.remove(0);
		}
		history.add(qr);
		
		if(tableModel.getRowCount() == FIXED_SIZE) {
			tableModel.removeRow(0);
		}
		String[] rowData = new String[] { qr.query, 
				KdbHelper.asLine(qr.k), 
				qr.getResultType() };
		tableModel.addRow(rowData);
		
		// scroll to bottom
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				table.scrollRectToVisible(table.getCellRect(table.getRowCount()-1, 0, true));
			}
		});
	}

	void setMaximumRowsShown(int maxRowsShown) {
		Preconditions.checkArgument(maxRowsShown > 0);
		this.maxRowsShown = maxRowsShown;
	}

}
