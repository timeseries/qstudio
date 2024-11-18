package com.timestored.sqldash;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.jdesktop.swingx.combobox.MapComboBoxModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.timestored.TimeStored;
import com.timestored.misc.HtmlUtils;
import com.timestored.qstudio.UpdateHelper;
import com.timestored.sqldash.chart.ChartTheme;
import com.timestored.sqldash.chart.ViewStrategy;
import com.timestored.sqldash.chart.ViewStrategyFactory;
import com.timestored.theme.Theme;
import com.timestored.theme.Theme.InputLabeller;

/**
 * Allows configuring all aspects of chart (type, color themes, refresh period etc.),
 * does not however prodive access to server selection.
 */
public class ChartControlPanel extends JPanel implements Widget.Listener {

	private static final long serialVersionUID = 1L;

	private static final TimeStored.Page helpPage = TimeStored.Page.SQLDASH_HELP_EG;
	private static final String DEFAULT_TOOLTIP = "<html><div width='350px'><b>Charts</b>"
					+ "<br><a href='" + helpPage.url() + "'>" 
			+  helpPage.niceUrl() + "</a></div></html>";
	
//	private final JTextField refreshTextField;
	private final JComboBox chartComboBox;
	private final MapComboBoxModel<String, ViewStrategy> chartStratComboBoxModel;
	private final MapComboBoxModel<String, ChartTheme> chartThemeComboBoxModel;
	
	private final ChartWidget app;

	private JComboBox chartThemeComboBox;
	private JCheckBox veryLargeChartsCheckBox;

	
	/**
	 * Display an editor for a given app or if null app passed in show disabled controls
	 */
	public ChartControlPanel(final ChartWidget app) {
		
		this.app = app;
		
		// create map of titles->viewStrategies for display in comboBox
		List<ViewStrategy> chartStrats = ViewStrategyFactory.getStrategies();
		Map<String, ViewStrategy> descToChartStrat = Maps.newLinkedHashMap();
		for(ViewStrategy cs : chartStrats) {
			descToChartStrat.put(cs.getDescription(), cs);
		}
		chartStratComboBoxModel = new MapComboBoxModel<String, ViewStrategy>(descToChartStrat);	
							

		// create map of titles->themes for display in comboBox
		List<ChartTheme> chartThemes = ViewStrategyFactory.getThemes();
		Map<String, ChartTheme> descToChartTheme = Maps.newLinkedHashMap();
		for(ChartTheme ct : chartThemes) {
			descToChartTheme.put(ct.getTitle(), ct);
		}
		chartThemeComboBoxModel = new MapComboBoxModel<String, ChartTheme>(descToChartTheme);	
		
		setBorder(BorderFactory.createTitledBorder("Control Panel"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Chart Type panel
		InputLabeller IL = Theme.getInputLabeller();
		chartComboBox = new JComboBox(chartStratComboBoxModel);
		chartComboBox.setRenderer(new IconListRenderer(chartStratComboBoxModel, (Integer hoveredIndex) -> {
			if(hoveredIndex >= 0 && hoveredIndex < descToChartStrat.size()) {
				ViewStrategy vs = chartStratComboBoxModel.getValue((int) hoveredIndex);
				if(vs != null && !app.getViewStrategy().equals(vs)) {
					app.setViewStrategy(vs);	
				}
			}
		}));

		// Chart Type panel
		chartThemeComboBox = new JComboBox(chartThemeComboBoxModel);
		veryLargeChartsCheckBox = new JCheckBox("Render Very Large Charts");

		// Chart Refresh panel
//		JPanel refreshPanel = new JPanel();
//		JLabel refreshLabel = new JLabel("Auto Refresh");
//		refreshPanel.add(refreshLabel);
//		refreshTextField = new JFormattedTextField(NumberFormat.getInstance());
//		refreshPanel.add(refreshTextField);
//		refreshTextField.setColumns(10);
//		JLabel lblNewLabel = new JLabel("ms");
//		refreshPanel.add(lblNewLabel);
		
		if(app!=null) {
			chartComboBox.setSelectedItem(app.getViewStrategy().getDescription());
			
			chartComboBox.addActionListener(as -> {
				int i = chartComboBox.getSelectedIndex();
				ViewStrategy vs = chartStratComboBoxModel.getValue(i);
				if(vs != null) {
					UpdateHelper.registerEvent("cha_select"+vs.getPulseName());
				}
				if(!app.getViewStrategy().equals(vs)) {
					app.setViewStrategy(vs);
				}
			});
			
			chartThemeComboBox.addActionListener(ae -> {
				int i = chartThemeComboBox.getSelectedIndex();
				ChartTheme th = chartThemeComboBoxModel.getValue(i);
				if(!app.getChartTheme().equals(th)) {
					app.setChartTheme(th);	
				}
			});
			
			veryLargeChartsCheckBox.addActionListener(ae -> {
				app.setRenderLargeDataSets(veryLargeChartsCheckBox.isSelected());
			});
			
		} else {
			chartComboBox.setEnabled(false);
			chartThemeComboBox.setEnabled(false);
			veryLargeChartsCheckBox.setEnabled(false);
		}

		JLabel largeLabel = new JLabel(Theme.CIcon.INFO.get());
		largeLabel.setToolTipText("<html>If the data has many rows or columns it won't automatically render to prevent slowness."
				+ "<br />Check this box to try and render all data of any size.</html>");
		add(IL.get("Type:", chartComboBox, "chartComboBox", new CurrentViewInfoLinkLabel()));
		add(IL.get("Theme:", chartThemeComboBox, "chartThemeComboBox"));
		add(IL.get("", veryLargeChartsCheckBox, "veryLargeChartsCheckBox"));
		
		if(app != null) {
			app.addListener(this);
		}
		refreshGui();
	}

	
	
	private void refreshGui() {

		boolean enabled = app != null;
		chartComboBox.setEnabled(enabled);
		chartThemeComboBox.setEnabled(enabled);
//		refreshTextField.setEnabled(enabled);
		if(enabled) {
			if(!chartComboBox.isFocusOwner()) {
				ViewStrategy vs = app.getViewStrategy();
				chartComboBox.setSelectedItem(vs.getDescription());
			}
			
			if(!chartThemeComboBox.isFocusOwner()) {
				ChartTheme ct = app.getChartTheme();
				chartThemeComboBox.setSelectedItem(ct.getTitle());
			}
		}
	}

	@Override public void configChanged(Widget app) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				refreshGui();
			}
		});
	}
	
	/** Displays format explanation for current view strategy in tooltip. */
	public class CurrentViewInfoLinkLabel extends JLabel {

		private static final long serialVersionUID = 1L;

		public CurrentViewInfoLinkLabel() {
			
			super(Theme.CIcon.INFO.get());
			// to force call to getToolTipText...
			ToolTipManager.sharedInstance().registerComponent(this);
			
			if(HtmlUtils.isBrowseSupported()) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override public void mouseClicked(MouseEvent e) {
						HtmlUtils.browse(helpPage.url());
					}
				});
			} 
		}
		

		@Override public String getToolTipText() {
			String tooltip = DEFAULT_TOOLTIP;
			if(app != null) {
				ViewStrategy vs = app.getViewStrategy();
				tooltip = "<html><div width='350px'><b>" + vs.getDescription() + "</b>"
						+ "<br>" + vs.getFormatExplainationHtml()
						+ "<br><a href='" + helpPage.url() + "'>" 
						+ helpPage.niceUrl() + "</a></div></html>";
			}
			return tooltip;
		}
		
	}
	


	class IconListRenderer extends DefaultListCellRenderer{ 
	    private static final long serialVersionUID = 1L;
	    private final MapComboBoxModel<String, ViewStrategy> chartStratComboBoxModel;
		private final Consumer<Integer> callback;
	    public IconListRenderer(MapComboBoxModel<String, ViewStrategy> chartStratComboBoxModel, Consumer<Integer> callback) {
			this.chartStratComboBoxModel = chartStratComboBoxModel;
			this.callback = Preconditions.checkNotNull(callback);
		}
		@Override
	    public Component getListCellRendererComponent(JList list, Object value, int index,boolean isSelected, boolean cellHasFocus) { 
	        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        if(value instanceof String) {
	        	ViewStrategy vs = chartStratComboBoxModel.getValue(value);
		        if(vs != null && vs.getIcon() != null) {
			        label.setIcon(vs.getIcon().get16());
		        }
	        }
	        if(isSelected) {
	        	callback.accept(index);
	        }
	        return label; 
	    } 
	}

}
