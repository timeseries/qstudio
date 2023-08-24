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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.timestored.StringUtils;
import com.timestored.connections.ServerConfig;
import com.timestored.qstudio.model.QueryAdapter;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.QueryResult;
import com.timestored.sqldash.ChartControlPanel;
import com.timestored.sqldash.chart.ChartTheme;
import com.timestored.sqldash.chart.ViewStrategyFactory;
import com.timestored.sqldash.model.ChartWidget;
import com.timestored.sqldash.model.Queryable;
import com.timestored.swingxx.SwingUtils;
import com.timestored.theme.Theme;

/**
 * Display chart and control panel that allows configuring chart.
 */
class ChartResultPanel extends JPanel implements GrabableContainer {
	
	private static final long serialVersionUID = 1L;
	private static final int PADDING = 10;
	private ChartWidget app;
	private final QStudioFrame qstudioFrame;
    final Random R = new Random();

	private QueryResult latestQueryResult;
	private ExportPanel exportPanel;

	public ChartResultPanel(final QueryManager adminModel, QStudioFrame qstudioFrame) {
		
		this.qstudioFrame = qstudioFrame;
		
		app = new ChartWidget();
		
		// adapts queryManager events to trigger tabChange events for kdbChartPanel
		adminModel.addQueryListener(new QueryAdapter() {

			@Override public void sendingQuery(ServerConfig sc, String query) {}

			@Override public void queryResultReturned(ServerConfig sc, QueryResult qr) {

				// Really annoying but they haven't paid
        		if(qr.k != null && !QLicenser.isPermissioned(QLicenser.Section.UI_NICETIES) && R.nextInt(10) > 7) {
        			exportPanel = null;
        			removeAll();
        	        add(KDBResultPanel.getAdvert(true), BorderLayout.CENTER);
        	        repaint();
        	        return;
        		}
        		
				if(exportPanel == null) {
					resetContent();
				}
				// ignore as we will send tab soon
				app.setIgnoreConfigChanges(true); 
				String qsrv = adminModel.getSelectedServerName();
				Queryable q = new Queryable(qsrv, qr.query);
				app.setQueryable(q);
				app.setIgnoreConfigChanges(false);
				exportPanel.setEnabled(false);
				
				// ok now send new data for redraw
				latestQueryResult = qr;
				if(qr.isException()) {
					app.queryError(q, qr.e);
				} else if (qr.isCancelled()) {
					app.queryError(q, new IOException("Query Cancelled"));
				} else {
					app.tabChanged(q, qr.rs);
					exportPanel.setEnabled(qr.rs != null);
				}
			}
			
		});

        add(KDBResultPanel.getAdvert(false), BorderLayout.CENTER);
	}

	public void setChartTheme(ChartTheme chartTheme) {
		if(app != null) {
			app.setChartTheme(Preconditions.checkNotNull(chartTheme));
		}
	}
	
	private void resetContent() {
		removeAll();
		JPanel configPanel = Theme.getVerticalBoxPanel();
		configPanel.add(new ChartControlPanel(app));
		exportPanel = new ExportPanel();
		exportPanel.setEnabled(false);
		configPanel.add(exportPanel);
		
		setLayout(new BorderLayout());
        add(configPanel, BorderLayout.WEST);

		JPanel p = new JPanel(new BorderLayout(PADDING, PADDING));
		p.add(app.getPanel(), BorderLayout.CENTER);
        add(p, BorderLayout.CENTER);
        repaint();
	}

	private class ExportPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private final JButton popoutButton;
		private JButton exportButton;

		@Override public void setEnabled(boolean enabled) {
			popoutButton.setEnabled(enabled);
			exportButton.setEnabled(enabled);
			super.setEnabled(enabled);
		}
		
		public ExportPanel() {
			
//			exportButton = new JButton("Export to sqlDashboards", Theme.CIcon.SQLDASH_LOGO.get16());
//			exportButton.addActionListener(new ActionListener() {
//				
//				@Override public void actionPerformed(ActionEvent e) {
//					SqlDashFrame dbvisFrame = qstudioFrame.showLatestDbVisFrame();
//					AppModel appModel = dbvisFrame.getAppModel();
//					if(appModel.getSelectedDesktopModel()==null) {
//						appModel.newDesktop();
//					}
//					
//					// add the necessary connection from qStudio to sqlDashboards
//					// silently failing if already present.
//					ConnectionManager sqldConnMan = appModel.getConnectionManager();
//					ConnectionManager qsConnMan = qstudioFrame.getConnectionManager();
//					sqldConnMan.addServer(Arrays.asList(qsConnMan.getServer(app.getQ().getServerName())));
//					
//					DesktopModel dm = appModel.getSelectedDesktopModel();
//					dm.add(new ChartWidget(dm, app));
//				}
//			});

			exportButton = new JButton("Export to Pulse", Theme.CIcon.SQLDASH_LOGO.get16());
			exportButton.addActionListener(new ActionListener() {
				
				@Override public void actionPerformed(ActionEvent e) {
					try {
						Queryable q = app.getQ();
						String chartVS = app.getViewStrategy().getDescription();
						String chartType =  URLEncoder.encode(chartVS, "UTF-8"); // TODO translate
						String qry = URLEncoder.encode(q.getQuery(), "UTF-8");
						String srvr = URLEncoder.encode(q.getServerName(), "UTF-8");
						String url = "http://localhost:8080/sqleditor?chart=" + chartType + "&qry=" + qry + "&server=" + srvr;
						java.awt.Desktop.getDesktop().browse(new URI(url));
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});


			popoutButton = new JButton("Open in New Window", Theme.CIcon.POPUP_WINDOW.get16());
			popoutButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {

					if(latestQueryResult != null && latestQueryResult.rs != null) {
						ChartWidget cw  = new ChartWidget(app);
						Queryable q = cw.getQ();
						String title = q.getQuery() + " - " + q.getServerName();
						JPanel p = cw.getPanel();
						cw.tabChanged(q, latestQueryResult.rs);
						
						JFrame f =  SwingUtils.getPopupFrame(ChartResultPanel.this, 
								title, cw.getPanel(), 
								cw.getViewStrategy().getIcon().getBufferedImage());
						f.setVisible(true);
					}
				}
			});

			setLayout(new BorderLayout());
			add(exportButton, BorderLayout.NORTH);
			add(popoutButton, BorderLayout.SOUTH);
		}
		
	}

	@Override public GrabItem grab() {
		if(latestQueryResult != null) {
			ChartWidget cw  = new ChartWidget(app);
			Queryable q = cw.getQ();
			String title = q.getQuery() + " - " + q.getServerName();
			JPanel p = cw.getPanel();
			cw.tabChanged(q, latestQueryResult.rs);
			
			return new GrabItem(p, StringUtils.abbreviate(title, 50), cw.getTSIcon());
		}
		return null;
	}

}
