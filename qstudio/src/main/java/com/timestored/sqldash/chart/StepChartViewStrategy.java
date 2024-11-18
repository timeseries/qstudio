package com.timestored.sqldash.chart;

import java.awt.Component;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.Log;

import com.google.common.base.Preconditions;
import com.timestored.connections.JdbcTypes;
import com.timestored.sqldash.theme.DBIcons;
import com.timestored.theme.Icon;



/**
 * Strategy for displaying {@link ResultSet}'s as bar charts. 
 * See {@link #getFormatExplainationHtml()} for details.
 */
@Immutable public class StepChartViewStrategy implements ViewStrategy {

	public static final ViewStrategy INSTANCE = new StepChartViewStrategy();


	@Override public UpdateableView getView(final ChartTheme theme) {
		Preconditions.checkNotNull(theme);
		
		return new HardRefreshUpdateableView(new HardRefreshUpdateableView.ViewGetter() {

			@Override public Component getView(ResultSet resultSet, ChartResultSet colResultSet) 
					throws ChartFormatException {

		        if(colResultSet == null) {
		        	throw new ChartFormatException("Could not create Result Set.");
		        }

		        XYDataset dataset = null;
		        try {
		        	dataset = TimeseriesViewStrategy.generateTimeSeries(colResultSet);
		        } catch(ChartFormatException cfe) {
		        	dataset = ScatterPlotViewStrategy.createXYDataset(colResultSet);
		        }
				JFreeChart chart = ChartFactory.createXYStepChart("", "", "", dataset,
						PlotOrientation.VERTICAL, theme.showChartLegend(), true, false);
				
				// StepChart by default starts at 0. Set it to auto range.
				try {
					if(chart.getPlot() instanceof XYPlot) {
						if(chart.getPlot() instanceof XYPlot) {
							XYPlot xyPlot = (XYPlot) chart.getPlot();
							if(xyPlot.getRangeAxis() instanceof NumberAxis) {
								NumberAxis na = ((NumberAxis) xyPlot.getRangeAxis());
								na.setAxisLineVisible(false);
								na.setAutoRangeIncludesZero(false);
								// Plotting of very small numbers (common in FX trading) wasn't showing any axis labels.
								// This is a hacky solution to try and show something
								// Clear user feedback on a number of ranges will be required.
								// Based on workaround from jfree forum: https://www.jfree.org/forum/viewtopic.php?t=26056
								if(na.getRange().getLength() < 0.01) {
									na.setStandardTickUnits(new StandardTickUnitSource());
									DecimalFormat df=new DecimalFormat();
									df.applyPattern("##0.#######");
									na.setNumberFormatOverride(df);
								}
							}
						}
					}
				} catch(RuntimeException e) {
					Log.debug(e);
				}
				
				if(colResultSet.getTimeCol() != null) {
					XYItemRenderer renderer = chart.getXYPlot().getRenderer();
					TimeseriesViewStrategy.setTimeTooltipRenderer(colResultSet, renderer);
				}
				
				return new ChartPanel(theme.apply(chart), false, true, true, false, true);
			}
		});
		
	}

	@Override public String getDescription() { return "Step Plot"; }

	@Override public Icon getIcon() { return DBIcons.CHART_LINE; }
	
	@Override public String getQueryEg(JdbcTypes jdbcType) {
		return TimeseriesViewStrategy.INSTANCE.getQueryEg(jdbcType); 
	}
	

	@Override
	public String toString() {
		return ScatterPlotViewStrategy.class.getSimpleName() 
				+ "[" + getDescription() + "]";
	}

	@Override
	public List<ExampleView> getExamples() {
		return TimeseriesViewStrategy.INSTANCE.getExamples();	
	}

	@Override public String getFormatExplainationHtml() {
		return TimeseriesViewStrategy.INSTANCE.getFormatExplainationHtml();
	}
	
	@Override public String getFormatExplaination() {
		return TimeseriesViewStrategy.INSTANCE.getFormatExplaination();
	}

	@Override public boolean isQuickToRender(ResultSet rs, int rowCount, int columnCount) {
		return rowCount < 211_000; // 2 seconds on Ryans PC
	}
	
	@Override public String getPulseName() { return "timeseries"; }
}

