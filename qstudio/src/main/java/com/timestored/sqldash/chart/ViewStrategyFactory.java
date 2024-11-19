package com.timestored.sqldash.chart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides access to all charting strategies, themes and {@link UpdateableView} 's.
 */
public class ViewStrategyFactory {
	
	private static final List<ViewStrategy> STRATEGIES
		= Collections.unmodifiableList(Arrays.asList(new ViewStrategy[] {
				NoRedrawViewStrategy.INSTANCE,
				AreaChartViewStrategy.INSTANCE,
				BarChartViewStrategy.INSTANCE,
				StackedBarChartViewStrategy.INSTANCE,
				BubbleChartViewStrategy.INSTANCE,
				CandleStickViewStrategy.INSTANCE,
				DataTableViewStrategy.getInstance(false),
				HeatMapViewStrategy.INSTANCE,
				HistogramViewStrategy.INSTANCE,
				LineChartViewStrategy.INSTANCE,
				PieChartViewStrategy.INSTANCE,
				ScatterPlotViewStrategy.INSTANCE,
				TimeseriesViewStrategy.INSTANCE,
				StepChartViewStrategy.INSTANCE,
				DotViewStrategy.INSTANCE
		}));


	public static final ChartTheme LIGHT_THEME = DefaultTheme.getInstance(new LightColorScheme(), "Light", "Primary colours on a white background");
	public static final ChartTheme DARK_THEME = DefaultTheme.getInstance(new DarkColorScheme(), "Dark", "Primary colours on a black background");
	public static final ChartTheme PASTEL_THEME = DefaultTheme.getInstance(new PastelColorScheme(), "Pastel", "Pastel colours on a black background");


	private static final List<ChartTheme> THEMES
		= Collections.unmodifiableList(Arrays.asList(new ChartTheme[] {
				LIGHT_THEME,DARK_THEME,PASTEL_THEME	}));
	
	
	public static List<ViewStrategy> getStrategies() {
		return STRATEGIES;
	}

	public static List<ChartTheme> getThemes() {
		return THEMES;
	}
		
	public static JdbcChartPanel getJdbcChartpanel() {
		return new JdbcChartPanel(TimeseriesViewStrategy.INSTANCE, THEMES.get(0));
	}

	
	public static JdbcChartPanel getJdbcChartpanel(ViewStrategy vs) {
		return new JdbcChartPanel(vs, THEMES.get(0));
	}
	
}
