package com.timestored.sqldash.chart;

import static com.timestored.sqldash.chart.KdbFunctions.cos;
import static com.timestored.sqldash.chart.KdbFunctions.mul;
import static com.timestored.sqldash.chart.KdbFunctions.sin;
import static com.timestored.sqldash.chart.KdbFunctions.til;

import java.awt.Component;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.chart.ChartResultSet.NumericCol;
import com.timestored.sqldash.chart.ChartResultSet.TimeCol;
import com.timestored.theme.Icon;
import com.timestored.theme.Theme.CIcon;


/**
 * Strategy for displaying {@link ResultSet}'s as a time series.
 * See {@link #getFormatExplainationHtml()} for details.
 */
public enum TimeseriesViewStrategy implements ViewStrategy {

	INSTANCE;

	private static final String KDB_QUERY = "([] dt:2013.01.01+til 21; cosineWave:cos a; \r\n\t sineWave:sin a:0.6*til 21)";
	private static final String TOOLTIP_FORMAT = "<html><b>{0}:</b><br>{1}<br>{2}</html>";
	private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#,###.##");

	
	@Override
	public 	UpdateableView getView(final ChartTheme theme) {

		return new HardRefreshUpdateableView(new HardRefreshUpdateableView.ViewGetter() {

			@Override public Component getView(ResultSet rs, ChartResultSet colResultSet) 
					throws ChartFormatException {

				TimeSeriesCollection dataset = generateTimeSeries(colResultSet);

				JFreeChart chart = ChartFactory.createTimeSeriesChart(
						"", "Time", "Value", dataset, true, true, false);
				ChartPanel cp = new ChartPanel(theme.apply(chart), false, true, true, false, true);
				setTimeTooltipRenderer(colResultSet, cp.getChart().getXYPlot().getRenderer());
				
				return cp;
			}
				
		});
			
	}


	static void setTimeTooltipRenderer(ChartResultSet colResultSet, XYItemRenderer renderer) {
		// once we have data set the tooltip appropriately
		// set tooltip specific to date type
		TimeCol timeCol = colResultSet.getTimeCol();
		if(timeCol != null) {
			SimpleDateFormat dateFormat = getDateFormat(timeCol.getType());
			if(dateFormat != null) {
				StandardXYToolTipGenerator ttg;
				ttg = new StandardXYToolTipGenerator(TOOLTIP_FORMAT, dateFormat, DEC_FORMAT);
				renderer.setBaseToolTipGenerator(ttg);
			}
		}
	}
	
	static TimeSeriesCollection generateTimeSeries(ChartResultSet colResultSet) throws ChartFormatException {

		if(colResultSet==null) {
			throw new ChartFormatException("Could not create chart result set.");
		}
		
		TimeCol timeCol = colResultSet.getTimeCol();
		if(timeCol==null) {
			throw new ChartFormatException("No Time Column Found.");
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
			
		// time series chart
		if(timeCol != null) {
			RegularTimePeriod[] timePeriods = null;
			try {
				timePeriods = timeCol.getRegularTimePeriods();
			} catch(IllegalArgumentException iae) {
				throw new ChartFormatException(iae.toString());
			}
	    	// create time series for each column
	    	for(NumericCol nc : colResultSet.getNumericColumns()) {
    	    	TimeSeries tSeries = new TimeSeries("" + nc.getLabel());
    	    	int row = 0;
		        for(double d : nc.getDoubles()) {
		        	if(!Double.isNaN(d)) {
		        		tSeries.addOrUpdate(timePeriods[row], d);
		        	}
		        	row++;
		        }
	    		if(!tSeries.isEmpty()) {
	    			dataset.addSeries(tSeries);
	    		}
	    	}
		}
		return dataset;
	}
	
	public static SimpleDateFormat getDateFormat(int timeType) {
		SimpleDateFormat dateFormat = null;
		if (timeType == java.sql.Types.DATE) {
			dateFormat = new SimpleDateFormat("d-MMM-yyyy hh:mm:ss");
		} else if (timeType == java.sql.Types.TIME) {
			dateFormat = new SimpleDateFormat("hh:mm:ss");
		}
		return dateFormat;
	}
	
	@Override public String getDescription() { return "Time Series"; }

	@Override public Icon getIcon() { return CIcon.CHART_CURVE; }
	
	@Override public String getQueryEg(JdbcTypes jdbcType) {
		return jdbcType.equals(JdbcTypes.KDB) ? KDB_QUERY : null; 
	}
	
	@Override
	public String toString() {
		return TimeseriesViewStrategy.class.getSimpleName() 
				+ "[" + getDescription() + "]";
	}

	@Override public List<ExampleView> getExamples() {
		return ImmutableList.of(getSineWave());
	}


	public static ExampleView getSineWave() {
		String description = "A sine/cosine wave over a period of days.";
		String name = "Day Sines";
		String[] colNames = new String[] { "dt", "cosineWave", "sineWave" };
		double[] a = mul(til(21), 0.6);
		Date[] dt = ExampleTestCases.getDays(2013, 1, 1, 21);
		Object[] colValues = new Object[] { dt, cos(a), sin(a) };
		ResultSet resultSet = new SimpleResultSet(colNames, colValues);
		ExampleView sineEV = new ExampleView(name, description, new TestCase(name, resultSet, KDB_QUERY));
		return sineEV;
	}

	private static final String[] FORMATA = 
		{ "The first date/time column found will be used for the x-axis.",
				"Each numerical column represents one time series line on the chart." };


	@Override public String getFormatExplainationHtml() {
		return "<ol><li>" + Joiner.on("</li><li>").join(FORMATA) + "</li></ol>";
	}
	
	@Override public String getFormatExplaination() {
		return Joiner.on("\r\n").join(FORMATA);
	}
	
	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 211_000; // 2 seconds on Ryans PC
	}
	@Override public String getPulseName() { return "timeseries"; }
}
