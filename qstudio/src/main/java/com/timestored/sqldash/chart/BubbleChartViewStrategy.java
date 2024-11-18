package com.timestored.sqldash.chart;

import java.sql.ResultSet;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYZDataset;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.theme.DBIcons;
import com.timestored.theme.Icon;

/**
 * Strategy for displaying {@link ResultSet}'s as bubble charts. 
 * See {@link #getFormatExplainationHtml()} for details.
 */
enum BubbleChartViewStrategy implements ViewStrategy {

	INSTANCE;

	private static final String[] FORMATA = 
		{ "The first string columns are used as category labels.",
				"There must then be 3 numeric columns which are used for " +
				"x-coord, y-coord, size in that order." };

	
	private BubbleChartViewStrategy() { }
	
	@Override public UpdateableView getView(ChartTheme theme) {

		DefaultXYZDataset dataset = new DefaultXYZDataset();
		JFreeChart chart = ChartFactory.createBubbleChart("", "", 
				"", dataset, PlotOrientation.HORIZONTAL, false, true, true);
		XYPlot xyplot = (XYPlot) chart.getPlot();
		xyplot.setForegroundAlpha(0.65F);

		ChartPanel cp = new ChartPanel(theme.apply(chart));
		chart.getXYPlot().getRenderer().setBaseToolTipGenerator(Tooltip.getXYZNumbersGenerator());
		
		return new XYZDatasetUpdateableView(cp, dataset);
	}


	@Override public String getDescription() { return "Bubble Chart"; }
	
	@Override public Icon getIcon() { return DBIcons.CHART_BUBBLE; }

	@Override public String getFormatExplainationHtml() {
		return "<ol><li>" + Joiner.on("</li><li>").join(FORMATA) + "</li></ol>";
	}
	
	@Override public String getFormatExplaination() {
		return Joiner.on("\r\n").join(FORMATA);
	}

	@Override public List<ExampleView> getExamples() {
		
		ExampleView ev = new ExampleView("Single series", 
				"The three columns are used for x-axis,y-axis and size respectively. " +
				"Notice the GdpPerCapita column has been divided to make it similar sized to the other" +
				"columns so that the bubbles are a sensible size.",
				ExampleTestCases.COUNTRY_STATS_ADJUSTED_POP);
		return ImmutableList.of(ev);
	}

	@Override public String getQueryEg(JdbcTypes jdbcType) {
		if(jdbcType.equals(JdbcTypes.KDB)) {
			return ExampleTestCases.COUNTRY_STATS_ADJUSTED_POP.getKdbQuery();
		}
		return null; 
	}
	
	@Override public String toString() {
		return BubbleChartViewStrategy.class.getSimpleName() + "[" + getDescription() + "]";
	}

	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 5_000; // 3 seconds on Ryans PC
	}
	@Override public String getPulseName() { return "bubble"; }
}
