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

import static com.timestored.theme.Theme.CENTRE_BORDER;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.timestored.qstudio.model.AdminModel;
import com.timestored.qstudio.model.AdminModel.Category;
import com.timestored.qstudio.model.QEntity;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.ServerModel;
import com.timestored.qstudio.model.ServerObjectTree;
import com.timestored.sqldash.chart.ChartTheme;
import com.timestored.theme.Theme;

/**
 * Displays the selected component for a given {@link ServerObjectTree}.
 * in most appropriate way possible e.g. table for tables, text area for functions.
 * May also allow editing.
 */
public class SelectedServerObjectPanel extends JPanel 
	implements AdminModel.Listener {
	
	private static final long serialVersionUID = 1L;
	private final AdminModel adminModel;
	private final QueryManager queryManager;
	private ChartTheme chartTheme;
	
	public SelectedServerObjectPanel(AdminModel adminModel, QueryManager queryManager) {
		this.adminModel = adminModel;
		this.queryManager = queryManager;
		setLayout(new BorderLayout());
		adminModel.addListener(this);
		refreshGUI(false);
	}

	/**]
	 * @param forceModelUpdate if true, when server is selected the {@link ServerModel} will be 
	 * forcefully refreshed to ensure accurate data is displayed.
	 */
	private void refreshGUI(boolean forceModelUpdate) {
		
		Component p = new JLabel("nothing to see here");
		Category cat = adminModel.getSelectedCategory();
		String title = adminModel.getSelectedServerName();

		if(adminModel.getSelectedServerName() != null) {
			if(cat.equals(Category.ELEMENT)) {
				p = ElementDisplayFactory.getPanel(adminModel, queryManager, chartTheme);
				title = adminModel.getSelectedElement().getName();
			} else if(cat.equals(Category.NAMESPACE)) {
				p = getNamespaceListing(adminModel);
				title = adminModel.getSelectedNamespace();
			} else {
				ServerModel serverModel = adminModel.getServerModel();
				if(serverModel != null) {
					p = new ServerDescriptionPanel(serverModel);	
				}
			}
		}
		
		if(p == null) {
			JPanel panel = new JPanel();
			panel.add(new JLabel("Problem querying server. is it connected?"));
			p = panel;
		}
		
		// wrap up what we want to show
		final JPanel wrapPanel = new JPanel(new BorderLayout());
		wrapPanel.setBorder(CENTRE_BORDER);
		wrapPanel.add(Theme.getHeader(title), BorderLayout.NORTH);
		wrapPanel.add(p, BorderLayout.CENTER);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				removeAll();
				add(wrapPanel, BorderLayout.CENTER);
				revalidate();
			}
		});
	}



	private static Component getNamespaceListing(AdminModel adminModel) {
		// TODO Auto-generated method stub
		return new JLabel(adminModel.getSelectedNamespace());
	}

	@Override
	public void modelChanged() {
		// ignore, we only care when the servers contents change
	}

	@Override
	public void selectionChanged(ServerModel serverModel, Category category, 
			String namespace, QEntity element) {
		refreshGUI(true);
	}

	@Override public void modelChanged(ServerModel sm) {
		refreshGUI(false);
	}



	public void setChartTheme(ChartTheme chartTheme) {
		this.chartTheme = Preconditions.checkNotNull(chartTheme);
		refreshGUI(false);
	}

	
}
