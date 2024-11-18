package com.timestored.sqldash;

import com.timestored.sqldash.chart.ChartTheme;
import com.timestored.sqldash.chart.ViewStrategyFactory;

/**
 * Default java like light appearance.
 */
public class DefaultStyleScheme implements StyleScheme {
	
	private static final ChartTheme vsTheme 
		= ViewStrategyFactory.getThemes().get(0);
	private static final DefaultStyleScheme INSTANCE = new DefaultStyleScheme();
	
	private DefaultStyleScheme() {
	}

	@Override
	public ChartTheme getViewStrategyTheme() {
		return vsTheme;
	}
	
	public static DefaultStyleScheme getInstance() {
		return INSTANCE;
	}
}
