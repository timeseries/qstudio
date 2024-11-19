package com.timestored.sqldash;

import com.timestored.sqldash.chart.ChartTheme;

/**
 * Container class that dictates the flexible appearance of the entire app.
 */
interface StyleScheme {

	public abstract ChartTheme getViewStrategyTheme();

}