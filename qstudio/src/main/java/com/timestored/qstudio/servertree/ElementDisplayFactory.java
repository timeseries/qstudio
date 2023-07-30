package com.timestored.qstudio.servertree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.swing.JLabel;
import javax.swing.JPanel;

import kx.c.KException;

import com.timestored.cstore.CAtomTypes;
import com.timestored.qstudio.model.AdminModel;
import com.timestored.qstudio.model.ServerQEntity;
import com.timestored.sqldash.chart.ChartTheme;
import com.timestored.sqldash.chart.DataTableViewStrategy;
import com.timestored.sqldash.model.ChartWidget;
import com.timestored.sqldash.model.Queryable;

/**
 * Build for displaying details about known types. It's important that the type
 * and namespace / name is known as this allows advanced handling of functions
 * and partitioned tables etc.
 */
class ElementDisplayFactory {
	
	private static final Logger LOG = Logger.getLogger(ElementDisplayFactory.class.getName());

	public static Component getPanel(AdminModel adminModel, ChartTheme chartTheme) {
		
		Component retComponent = null;
		try {
			retComponent  = getCustomizedEditor(adminModel, chartTheme);
		} catch (KException e) {
			retComponent = handleException(adminModel, chartTheme);
		} catch (IOException e) {
			retComponent = handleException(adminModel, chartTheme);
		}
	
		if(retComponent == null) {
			retComponent = DefaultElementDisplayStrategy.INSTANCE.getPanel(adminModel);
		}
		return retComponent;
	}

	private static Component handleException(AdminModel adminModel, ChartTheme chartTheme) {

		Component retComponent = null;
		// problems = refresh tree and try once more
		adminModel.refresh();
		try {
			retComponent = getCustomizedEditor(adminModel, chartTheme);
		} catch (KException e) {
			LOG.log(Level.WARNING, "error using proposed Element Display Strategy", e);
			// fall through
		} catch (IOException e) {
			LOG.log(Level.WARNING, "error using proposed Element Display Strategy", e);
			//fall through
		}
		if(retComponent == null) {
			JPanel panel = new JPanel();
			JLabel label = new JLabel("Problem viewing this element, likely that the" +
					"wrong type was assumed. Try refreshing the tree and then selecting this" +
					"element again.");
			panel.add(label);
			retComponent = panel;
		}
		return retComponent;
	}

	private static Component getCustomizedEditor(AdminModel adminModel, ChartTheme chartTheme) throws IOException,
			KException {
		ServerQEntity elementDetails = adminModel.getSelectedElement();
		String queryName = elementDetails.getFullName();
		
		if(adminModel.getServerModel().getServerConfig().isKDB()) {
			if (elementDetails.isTable()) {
					return new PagingTablePanel(adminModel, queryName);
			
			} else if(elementDetails.getType().equals(CAtomTypes.LAMBDA)) {
				return new FunctionEditingPanel(adminModel, queryName);
			}
		} else if (elementDetails.isTable()) {
			return new NonkdbTablePanel(adminModel, queryName, chartTheme);
		}
		return null;
	}

	private static class NonkdbTablePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public NonkdbTablePanel(AdminModel adminModel, String queryName, ChartTheme chartTheme) {
			setLayout(new BorderLayout());
			ChartWidget app = new ChartWidget();
			app.setChartTheme(chartTheme);
			String qsrv = adminModel.getSelectedServerName();
			String sqlQuery = "SELECT * FROM " + queryName + " LIMIT 100;";
			try {
				CachedRowSet r = adminModel.getConnectionManager().executeQuery(adminModel.getServerModel().getServerConfig(), sqlQuery );
				Queryable q = new Queryable(qsrv, sqlQuery);
				app.setQueryable(q);
				app.setViewStrategy(DataTableViewStrategy.getInstance(true));
				//  new JScrollPane()
				JPanel p = app.getPanel();
				app.tabChanged(q, r);
				add(p, BorderLayout.CENTER);
				revalidate();
			} catch (SQLException | IOException e) {
			}
		}


		
	}

}

