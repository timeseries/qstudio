package com.timestored.sqldash.chart;

import java.awt.Color;

import org.jfree.chart.JFreeChart;


/**
 * Dictates the appearance of {@link ViewStrategy}, colours/size etc.
 */
public interface ChartTheme {

	public abstract JFreeChart apply(JFreeChart chart);
	
	public boolean showChartLegend();

	public abstract String getDescription();

	public abstract String getTitle();

	public abstract Color getForegroundColor();

	public abstract Color getBackgroundColor();

	public abstract Color getAltBackgroundColor();

	public abstract Color getSelectedBackgroundColor();
}
