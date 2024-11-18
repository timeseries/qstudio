package com.timestored.sqldash.chart;

import static com.timestored.sqldash.chart.KdbFunctions.cos;
import static com.timestored.sqldash.chart.KdbFunctions.mul;
import static com.timestored.sqldash.chart.KdbFunctions.til;

import java.awt.Component;
import java.sql.ResultSet;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.chart.ChartResultSet.NumericCol;
import com.timestored.sqldash.theme.DBIcons;
import com.timestored.theme.Icon;


/**
 * Strategy for displaying {@link ResultSet}'s as a Histogram. 
 * See {@link #getFormatExplainationHtml()} for details.
 */
public enum HistogramViewStrategy implements ViewStrategy {

	INSTANCE;
	
	public static final int NUMBER_BINS = 100;
	private static final String KDB_QUERY = "([] Returns:cos 0.0015*til 500; Losses:cos 0.002*til 500)";
	private static final String FORMAT = "Each Numeric column represents a separate series in the histogram." +
	" The series values are placed into buckets and their frquency tallied.";

	@Override public UpdateableView getView(ChartTheme theme) {
	        return new HistogramUpdateableView(theme);
	}

	@Override public String getDescription() { return "Histogram"; }

	@Override
	public List<ExampleView> getExamples() {
		String description = "Distribution of Returns and Losses";
		String name = "Profit Distribution";
		
		String[] colNames = new String[] { "Returns", "Losses"};
		double[] returns = cos(mul(til(500), 0.0015));
		double[] losses = cos(mul(til(500), 0.002));
		Object[] colValues = new Object[] { returns, losses };
		ResultSet resultSet = new SimpleResultSet(colNames, colValues);
		
		TestCase testCase = new TestCase(name, resultSet, KDB_QUERY);
		return ImmutableList.of(new ExampleView(name , description, testCase));
	}

	@Override public String getFormatExplainationHtml() { return FORMAT; }
	@Override public String getFormatExplaination() { return FORMAT;	}

	@Override public String getQueryEg(JdbcTypes jdbcType) { 
		return jdbcType.equals(JdbcTypes.KDB) ? KDB_QUERY : null; 
	}

	@Override public Icon getIcon() { return DBIcons.CHART_HISTOGRAM; }
	
	private static class HistogramUpdateableView implements UpdateableView {

		private final ChartPanel chartPanel;

		public HistogramUpdateableView(ChartTheme theme) {

			Preconditions.checkNotNull(theme);
			JFreeChart chart = ChartFactory.createHistogram("", 
					null, "Frequency", null, PlotOrientation.VERTICAL, true, true, false);
			
			chart.getXYPlot().getRenderer().setBaseToolTipGenerator(Tooltip.getXYNumbersGenerator());
			
			chartPanel = new ChartPanel(theme.apply(chart));
		}
		
		@Override public void update(ResultSet rs, ChartResultSet chartRS) throws ChartFormatException {

	        if(chartRS.getNumericColumns().size() < 1) {
	        	throw new ChartFormatException("There must be atleast one number column.");
	        }
			
			HistogramDataset dataset = new HistogramDataset();
			for (NumericCol numCol : chartRS.getNumericColumns()) {
				dataset.addSeries(numCol.getLabel(), numCol.getDoubles(), NUMBER_BINS);
			}
			XYPlot xyplot = ((XYPlot) chartPanel.getChart().getPlot());
			xyplot.setDataset(dataset);
			xyplot.getDomainAxis().setLabel(chartRS.getRowTitle());
		}

		@Override public Component getComponent() {
			return chartPanel;
		}

		
	}

	@Override public String toString() {
		return HistogramViewStrategy.class.getSimpleName() + "[" + getDescription() + "]";
	}
	
	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 211_000; // 1 seconds on Ryans PC
	}
	@Override public String getPulseName() { return null; }
}
