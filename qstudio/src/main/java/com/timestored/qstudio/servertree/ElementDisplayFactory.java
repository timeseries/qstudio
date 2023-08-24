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
package com.timestored.qstudio.servertree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.swing.JLabel;
import javax.swing.JPanel;

import kx.c.KException;

import com.timestored.connections.JdbcTypes;
import com.timestored.connections.ServerConfig;
import com.timestored.cstore.CAtomTypes;
import com.timestored.qstudio.model.AdminModel;
import com.timestored.qstudio.model.ServerQEntity;
import com.timestored.qstudio.model.ServerQEntity.QQuery;
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
			return new NonkdbTablePanel(adminModel, elementDetails, chartTheme);
		}
		return null;
	}

	private static class NonkdbTablePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public NonkdbTablePanel(AdminModel adminModel, ServerQEntity serverEntity, ChartTheme chartTheme) {
			setLayout(new BorderLayout());
			ChartWidget app = new ChartWidget();
			app.setChartTheme(chartTheme);
			String qsrv = adminModel.getSelectedServerName();
			JdbcTypes t = adminModel.getConnectionManager().getServer(qsrv).getJdbcType();

			try {
				if(serverEntity.getQQueries().size() > 0) {
					String sqlQuery = serverEntity.getQQueries().get(0).getQuery();
					CachedRowSet r = adminModel.getConnectionManager().executeQuery(adminModel.getServerModel().getServerConfig(), sqlQuery );
					Queryable q = new Queryable(qsrv, sqlQuery);
					app.setQueryable(q);
					app.setViewStrategy(DataTableViewStrategy.getInstance(true));
					//  new JScrollPane()
					JPanel p = app.getPanel();
					app.tabChanged(q, r);
					add(p, BorderLayout.CENTER);
					revalidate();
				}
			} catch (SQLException | IOException e) {
			}
		}


		
	}

}

