package com.timestored.qstudio;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import com.timestored.TimeStored.Page;
import com.timestored.misc.InfoLink;
import com.timestored.qstudio.kdb.KdbTableFactory;
import com.timestored.qstudio.model.BenchmarkReport;
import com.timestored.qstudio.model.ServerModel;
import com.timestored.theme.Theme;


/**
 * Displays button to allow running benchmarks against given KDB server and updates
 * panel to show results when they are received.
 */
public class BenchmarkPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(BenchmarkPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private final JPanel resPanel;
	private final ServerModel serverModel;
	
	private SwingWorker<BenchmarkReport, Void> worker;
			
	public BenchmarkPanel(ServerModel serverModel) {
		
		this.serverModel = serverModel;
		setLayout(new BorderLayout());
		
		JPanel headerPanel = Theme.getVerticalBoxPanel();

		headerPanel.add(Theme.getSubHeader("Benchmark Reports"));
		String description = "You can run a number of benchmarks testing inserts, " +
				"file read/write and table read speeds.";
		headerPanel.add(Theme.getTextArea("benchDesc", description));

		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String tooltip = "How to decide on what hardware for given requirements";
		infoPanel.add(InfoLink.getButton("Hardware Planning Advice", tooltip, 
				Page.TUTE_HARDWARE));
		headerPanel.add(infoPanel);
		
		add(headerPanel, BorderLayout.NORTH);

		
		JPanel p = new JPanel(new BorderLayout());
		JButton refreshButton = new JButton("Run Report", Theme.CIcon.SERVER_GO.get16());
		refreshButton.addActionListener(new ActionListener() {
			
			@Override public void actionPerformed(ActionEvent e) {
				String msg = "Benchmarking may lock the server up for a few minutes, ok to continue?";
				int choice = JOptionPane.showConfirmDialog(BenchmarkPanel.this, msg, 
						null, JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION) {
					refresh();
				} 
			}
		});
		
		
		p.add(refreshButton, BorderLayout.NORTH);
		
		resPanel = Theme.getVerticalBoxPanel();
		p.add(resPanel, BorderLayout.CENTER);
		add(p, BorderLayout.CENTER);
	}

	
	private void refresh() {
		
		if(worker!=null) {
			worker.cancel(true);
		}
		
		// show loading screen
		resPanel.removeAll();
		JProgressBar pbar = new JProgressBar();
		pbar.setIndeterminate(true);
		resPanel.add(pbar);
		BenchmarkPanel.this.revalidate();
		resPanel.repaint();
		
		worker = new SwingWorker<BenchmarkReport, Void>() {
		    
			@Override public BenchmarkReport doInBackground() {
		        try {
					return serverModel.runBenchmark();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		    }

		    @Override public void done() {
				BenchmarkReport bReport = null;
				Exception e = null;
				try {
					bReport = get();
				} catch (InterruptedException ie) {
					e = ie;
					LOG.log(Level.WARNING, "benchmark Interrupted", e);
				} catch (ExecutionException ee) {
					e = ee;
					LOG.log(Level.WARNING, "benchmark execution problem", e);
				}
				resPanel.removeAll();
				if(bReport != null) {
					
					Box b = Box.createVerticalBox();
					String description = "This version of qStudio cannot run all tests";
					b.add(Box.createVerticalStrut(5));
					b.add(InfoLink.getButton(description, "", Page.QSTUDIO_PRO));
					b.add(Box.createVerticalStrut(5));
					
					resPanel.add(b);
					resPanel.add(wrapTable("Throughput Report", bReport.getThroughputReport()));
					resPanel.add(wrapTable("IO Report", bReport.getIOReport()));
					resPanel.add(wrapTable("Table Read Report", bReport.getTableReadReport()));
					
				} else {
					String txt = "Problem Running Benchmark. Ensure you have adequate permissions on the server";
					resPanel.add(QStudioLauncher.ERR_REPORTER.getErrorReportLink(e, txt));
				}
				BenchmarkPanel.this.revalidate();
				BenchmarkPanel.this.repaint();
		    }
		};
		BackgroundExecutor.EXECUTOR.execute(worker);
		
	}
	
	public static Component wrapTable(String title, TableModel tm) {
		return Theme.wrap(title, KdbTableFactory.getPlainReadonlyTable(tm));
	}
}