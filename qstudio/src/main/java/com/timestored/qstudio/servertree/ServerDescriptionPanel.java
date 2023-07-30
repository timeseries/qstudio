package com.timestored.qstudio.servertree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.timestored.TimeStored;
import com.timestored.misc.InfoLink;
import com.timestored.qstudio.BenchmarkPanel;
import com.timestored.qstudio.QStudioLauncher;
import com.timestored.qstudio.kdb.KdbTableFactory;
import com.timestored.qstudio.kdb.SysCommand;
import com.timestored.qstudio.model.ServerModel;
import com.timestored.qstudio.model.ServerReport;
import com.timestored.qstudio.model.ServerSlashConfig;
import com.timestored.theme.Theme;

/**
 * Displays server properties, configuration, event handlers, open connections.
 * Also allows editing of the same.
 */
class ServerDescriptionPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final ServerReportpanel serverReportPanel;
	private final ServerConfigPanel serverConfigPanel;
	private JTabbedPane tabpane;

	private final ServerModel serverModel;

	/**
	 * Display the {@link ServerModel} as it is. If you want this to display the latest, you should
	 * send it a refresh request after creating this panel.
	 * @param serverModel
	 */
	ServerDescriptionPanel(final ServerModel serverModel) {
		
		this.serverModel = serverModel;
		setLayout(new BorderLayout());
		tabpane = new JTabbedPane();
		
		serverReportPanel = new ServerReportpanel();
		serverConfigPanel = new ServerConfigPanel();
		
		tabpane.addTab("Server Info", scrollWrap(serverReportPanel));
		tabpane.addTab("Configuration", scrollWrap(serverConfigPanel));
		tabpane.addTab("Benchmark", scrollWrap(new BenchmarkPanel(serverModel)));
		
		add(tabpane, BorderLayout.CENTER);
		
		serverModel.addListener(new ServerModel.Listener() {
			@Override public void changeOccurred() {
				refreshGui();
			}
		});

		
		// show what we had before but in background ask for refresh model.
		refreshGui();
	}

	
	private void refreshGui() {
		EventQueue.invokeLater(new Runnable() {
			
			@Override public void run() {
				removeAll();
				if(serverModel.getServerConfig().isKDB() && serverModel.isConnected()) {
					serverReportPanel.display(serverModel.getServerReport());
					serverConfigPanel.display(serverModel.getSlashConfig());
					add(tabpane, BorderLayout.CENTER);
				} else {
					String msg = "Server: " + serverModel.getName() + (serverModel.getServerConfig().isKDB() ? " not connected" : "");
					JPanel p = new JPanel(new BorderLayout());
					p.add(Theme.getHeader(msg));
					add(p, BorderLayout.CENTER);
				}
			}
		});
	}
	
	private static JScrollPane scrollWrap(JPanel panel) {
		return new JScrollPane(panel, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	}
	
	/** Panel displaying memory / server info */
	private static class ServerReportpanel extends Theme.BoxyPanel {
		
		private static final long serialVersionUID = 1L;

		private static final String MEM_DESCRIPTION = "<html><b>used</b> - number of bytes allocated. \r\n"
				+ "<br><b>heap</b> - bytes available in heap. \r\n"
				+ "<br><b>peak</b> - maximum heap size so far. \r\n"
				+ "<br><b>wmax</b> - maximum bytes available, given in -w command line parameter. \r\n"
				+ "<br><b>wmap</b> - mapped bytes. \r\n"
				+ "<br><b>mphy</b> - physical memory.";
		
		public ServerReportpanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
		}
		
		public void display(ServerReport serverReport) {
			removeAll();
			if(serverReport != null) {
				JPanel tabPanel = KdbTableFactory.getPlainReadonlyTable(serverReport.getGeneralInfoTable());
		    	add(Theme.wrap("Server Report", tabPanel));

				JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				infoPanel.add(InfoLink.getButton("How Memory Management Works", MEM_DESCRIPTION, 
						TimeStored.Page.TUTE_MEM));
				tabPanel = KdbTableFactory.getPlainReadonlyTable(serverReport.getMemoryTab());
				add(Theme.wrap("Memory Usage", tabPanel, infoPanel));

				tabPanel = KdbTableFactory.getPlainReadonlyTable(serverReport.getDiskTab());
				add(Theme.wrap("Table Storage", tabPanel));
				add(Box.createGlue());
			} else {
				String msg = "Could not retrieve Server Info, check server security settings.";
				add(QStudioLauncher.ERR_REPORTER.getErrorReportLink(msg, msg));
			}
		}
	}


	private static class ServerConfigPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		private final JPanel conPanel;
		
		public ServerConfigPanel() {
			setLayout(new BorderLayout());
			JPanel headerPanel = Theme.getSubHeader("System Commands");
			add(headerPanel, BorderLayout.NORTH);
			
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			conPanel = new JPanel(new GridLayout(12, 2));
			p.add(conPanel);
			add(p, BorderLayout.CENTER);
		}
		
		public void display(ServerSlashConfig serverConfig) {
			conPanel.removeAll();
			
			if(serverConfig != null) {

				conPanel.removeAll();
				List<String> sysStr = Arrays.asList(new String[] {"c","C","e","g","o","p","P","s","t","T","W","z" });
				
				for(String sysCmd : sysStr) {
					addRow(conPanel, SysCommand.get(sysCmd), serverConfig);
				}
				
			} else {
				String msg = "Could not retrieve Server Config, check server security settings.";
				conPanel.add(QStudioLauncher.ERR_REPORTER.getErrorReportLink(msg, msg));
			}
		}
		
		private static void addRow(JPanel conPanel, final SysCommand scmd, 
				final ServerSlashConfig servCfg) {
			
			if(scmd != null) {

				JLabel descLabel = new JLabel("\\" + scmd.getCommand() 
						+ "    " + scmd.getShortDesc());
				conPanel.add(descLabel);
				
				JPanel panel = new JPanel(new BorderLayout());
				String curVal = servCfg.getVal(scmd);
				final JTextField curValTextField = new JTextField(curVal == null ? "" : curVal);
				curValTextField.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						servCfg.setVal(scmd, curValTextField.getText());
					}
				});
				curValTextField.setEditable(scmd.isWritable() && curVal!=null);
				panel.add(curValTextField, BorderLayout.CENTER);
				
				String title = "\\" + scmd.getCommand() + " " + scmd.getArgs();
				Component infoLink = InfoLink.getLabel(title, scmd.getLongDesc(), scmd.getUrl(), false);
				
				
				panel.add(infoLink, BorderLayout.EAST);
				panel.setAlignmentX(Component.LEFT_ALIGNMENT);
				conPanel.add(panel);
			}
		}
	}




	

}
