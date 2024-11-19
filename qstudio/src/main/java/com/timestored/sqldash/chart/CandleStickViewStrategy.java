package com.timestored.sqldash.chart;

import static com.timestored.sqldash.chart.KdbFunctions.add;
import static com.timestored.sqldash.chart.KdbFunctions.mul;
import static com.timestored.sqldash.chart.KdbFunctions.til;

import java.awt.Component;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.chart.ChartResultSet.NumericCol;
import com.timestored.sqldash.chart.ChartResultSet.TimeCol;
import com.timestored.sqldash.theme.DBIcons;
import com.timestored.theme.Icon;

/**
 * Strategy for displaying {@link ResultSet}'s as candle stick charts. 
 * See {@link #getFormatExplainationHtml()} for details.
 */
public enum CandleStickViewStrategy implements ViewStrategy {

	INSTANCE;
	
	private static final String queryTHL = "{ c:55+2*til 30; ([] t:raze 2014.03.17+(7*til 6)+\\:til 5; high:c+30; low:c-20";
	private static final String queryTHLOC = queryTHL + "; open:60+til 30; close:c";
	private static final String queryTHLOCV = queryTHLOC + "; volume:30#3 9 6 5 4 7 8 2 13";
	private static final String END = ") }[]";
	
	private static final String TOOLTIP_FORMAT = "<html><b>{0}:</b><br>{1}<br>{2}</html>";
	private static final String[] COL_TITLES = { "high","low","open","close"};

	private static final String[] FORMATA = 
		{ "The table should contain columns labelled open/high/low/close/volume",
				"<br/>but must atleast contain high/low to allow it to be drawn.",
				"<br/>Only weekday values are shown." };
	

	@Override public UpdateableView getView(final ChartTheme theme) {
		Preconditions.checkNotNull(theme);
		
		return new HardRefreshUpdateableView(new HardRefreshUpdateableView.ViewGetter() {

			@Override public Component getView(ResultSet resultSet, ChartResultSet chartResultSet) 
					throws ChartFormatException {

				if(chartResultSet == null) {
					throw new ChartFormatException("Could not construct ResultSet.");
				}
				OHLCDataset dataset = createOHLCDataset(chartResultSet);
				
				DateAxis timeAxis = getTimeAxis(chartResultSet);

				NumberAxis valueAxis1 = new NumberAxis("Price");
				valueAxis1.setAutoRangeIncludesZero(false); // override default

				NumberAxis valueAxis2 = new NumberAxis("Volume");
				valueAxis2.setAutoRangeIncludesZero(false); // override default
				valueAxis2.setNumberFormatOverride(new DecimalFormat("0"));


				CandlestickRenderer candle = new CandlestickRenderer(4,false,new HighLowItemLabelGenerator());

				CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);
				XYPlot subplot1 = new XYPlot(dataset, timeAxis, valueAxis1, candle);
				plot.add(subplot1, 3);
				
				//creates dataset for volume chart
				TimeSeriesCollection dataset2 = createVolumeDataset(chartResultSet);
				if(dataset2 != null) {
					XYBarRenderer rr2 = new XYBarRenderer();
					rr2.setToolTipGenerator(new StandardXYToolTipGenerator(TOOLTIP_FORMAT, 
							new SimpleDateFormat("yyyy-MM-dd"), new DecimalFormat("#,###.00")));
					XYPlot subplot2 = new XYPlot(dataset2, timeAxis, valueAxis2, rr2);
					plot.add(subplot2, 1);
				}

				plot.setOrientation(PlotOrientation.VERTICAL);

				JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
				ChartPanel cp = new ChartPanel(theme.apply(chart));
				
				return cp;
			}
		});
		
	}


	private static DateAxis getTimeAxis(ChartResultSet chartResultSet) {
		DateAxis timeAxis = new DateAxis("Date");
		
		timeAxis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
		timeAxis.setLowerMargin(0.02); // reduce the default margins on the time axis
		timeAxis.setUpperMargin(0.02);

		// bug in jfree causes infinite loop if I let it use auto
		// see: http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=26926&start=15#p177518
		// so I try to calculate my own rough tick marks
        timeAxis.setAutoTickUnitSelection(false);
		// calc the largest date range
		TimeCol tc = chartResultSet.getTimeCol();
		if(tc != null) {
			Date[] dts = tc.getDates();
			if(dts.length > 0) {
				Date minDate = dts[0];
				Date maxDate = dts[0];
				for(Date d : dts) {
					if(d.after(maxDate)) {
						maxDate = d;
					} else if(d.before(minDate)) {
						minDate = d;
					}
				}	
				long diff = maxDate.getTime() - minDate.getTime();
				long diffDays = diff / (24 * 60 * 60 * 1000);
				DateTickUnit dtu;
				if(diffDays < 1) {
					dtu = new DateTickUnit(DateTickUnitType.HOUR, 1);
					timeAxis.setDateFormatOverride(new SimpleDateFormat("h:mm"));
				} else if(diffDays < 10) {
					dtu = new DateTickUnit(DateTickUnitType.DAY, 1);
					timeAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM"));
				} else if(diffDays < 60) {
					dtu = new DateTickUnit(DateTickUnitType.DAY, 7);
					timeAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM"));
				} else {
					dtu = new DateTickUnit(DateTickUnitType.MONTH, 1);
					timeAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM"));
				}
		        timeAxis.setTickUnit(dtu);
			}
		}
		return timeAxis;
	}



	/**
	 * @return A dataset of volume/date where possible, otherwise null.
	 */
	private static TimeSeriesCollection createVolumeDataset(ChartResultSet colResultSet) {

		TimeCol timeCol = colResultSet.getTimeCol();
		NumericCol nc = colResultSet.getNumericalColumn("volume");
			
		// time series chart
		if(timeCol != null && nc != null) {
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			RegularTimePeriod[] timePeriods = timeCol.getRegularTimePeriods();
    	    	TimeSeries tSeries = new TimeSeries("" + nc.getLabel());
    	    	int row = 0;
		        for(double d : nc.getDoubles()) {
		        	tSeries.addOrUpdate(timePeriods[row++], d);
		        }
	    		if(!tSeries.isEmpty()) {
	    			dataset.addSeries(tSeries);
	    		}
	    		return dataset;
    	}
		return null;
	}

	/**
	 * Create a dataset from the given result set or return null if not possible
	 * @throws ChartFormatException 
	 */
	private static OHLCDataset createOHLCDataset(ChartResultSet chartResultSet) 
			throws ChartFormatException {

		TimeCol timeCol = chartResultSet.getTimeCol();
		if (timeCol == null) {
			throw new ChartFormatException("No Time column found.");
		}

		NumericCol[] hlocvIndices = new NumericCol[4];
		int i = 0;
		for (String columnLabel : COL_TITLES) {
			hlocvIndices[i++] = chartResultSet.getNumericalColumn(columnLabel);
		}
		
		if(hlocvIndices[0] == null || hlocvIndices[1] == null) {
			throw new ChartFormatException("Candlestick requires atleast time/high/low columns.");
		}

		// make only having time and high/low compulsory
		boolean noOpen = hlocvIndices[2] == null;
		boolean noClose = hlocvIndices[3] == null;
		if(noOpen && noClose) { 
			// no open/close, set both to high
			hlocvIndices[2] = hlocvIndices[0];
			hlocvIndices[3] = hlocvIndices[0];
		} else if(noOpen) {
			hlocvIndices[2] = hlocvIndices[3]; // open = close
		} else if(noClose) {
			hlocvIndices[3] = hlocvIndices[2]; // close = open
		}

		// have time column and OHLC cur
		double[][] doubArray = new double[COL_TITLES.length][];
		for (int j = 0; j < COL_TITLES.length; j++) {
			if(hlocvIndices[j] != null) {
				doubArray[j] = hlocvIndices[j].getDoubles();
			} 
		}
		
		

		// one off conversion of timeseries to chart compatible format
		Date[] arrayOfDate = timeCol.getDates();
		double[] vol = new double[chartResultSet.getRowCount()];
		return new DefaultHighLowDataset("Series 1", arrayOfDate, doubArray[0], doubArray[1], doubArray[2],
				doubArray[3], vol);

	}


	@Override public String getDescription() { return "Candlestick"; }

	@Override public Icon getIcon() { return DBIcons.CHART_CANDLESTICK; }

	@Override
	public String toString() {
		return CandleStickViewStrategy.class.getSimpleName() + "["
				+ getDescription() + "]";
	}

	@Override
	public List<ExampleView> getExamples() {
		
		String description = "A Candlestick showing price movements and fluctuating volume over a period of 6 weeks";
		String name = "Prices going up";
		double[] close = add(mul(til(30), 2),55);
		double[] open = add(til(30),60);
		double[] high = add(close, 30);
		double[] low = add(close, -20);
		Date[] date = ExampleTestCases.getWeekDays(2014, 3, 17, 30);
		
		double[] volume = new double[] { 3,9,6,5,4,7,8,2,13,3,9,6,5,4,7,8,2,13,3,9,6,5,4,7,8,2,13,3,9,6 };
		
		ResultSet resultSet = new SimpleResultSet(
				new String[] { "t", "high", "low", "open", "close", "volume" }, 
				new Object[] { date, high, low, open, close, volume });
		TestCase testCase = new TestCase(name, resultSet, queryTHLOCV + END);
		ExampleView fullColEV = new ExampleView(name , description, testCase);

		// without a volume column
		ResultSet resultSetNoVol = new SimpleResultSet(
				new String[] { "t", "high", "low", "open", "close"}, 
				new Object[] { date, high, low, open, close });
		name = "Rising Prices, No Volume";
		description = "A candlestick showing only price movements, no volume column.";
		testCase = new TestCase(name, resultSetNoVol, queryTHLOC + END);
		ExampleView noVolColEV = new ExampleView(name , description, testCase);
		
		// only high and low columns
		ResultSet resultSetOnlyHighLow = new SimpleResultSet(
				new String[] { "t", "high", "low"},  
				new Object[] { date, high, low });
		name = "Rising Prices, Only High Low Columns Shown";
		description = "A candlestick showing only high low prices.";
		testCase = new TestCase(name, resultSetOnlyHighLow, queryTHL + END);
		ExampleView onlyHighLowEV = new ExampleView(name , description, testCase);
		
		
		return ImmutableList.of(fullColEV, noVolColEV, onlyHighLowEV);
	}

	@Override public String getQueryEg(JdbcTypes jdbcType) { 
		return jdbcType.equals(JdbcTypes.KDB) ? (queryTHLOCV+END) : null; 
	}

	@Override public String getFormatExplainationHtml() {
		return "<ol><li>" + Joiner.on("</li><li>").join(FORMATA).replace("<br/>", "") + "</li></ol>";
	}
	
	@Override public String getFormatExplaination() {
		return Joiner.on("\r\n").join(FORMATA);
	}

	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int numColumnCount) {
		return rowCount < 22_000; // 1 seconds on Ryans PC
	}

	@Override public String getPulseName() { return "candle"; }
}
