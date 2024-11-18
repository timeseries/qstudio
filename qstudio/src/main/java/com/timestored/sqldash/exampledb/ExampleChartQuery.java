package com.timestored.sqldash.exampledb;

import com.timestored.sqldash.chart.ViewStrategy;

/** An SQL query that demonstrates a {@link ViewStrategy}s */
public interface ExampleChartQuery {
	
	public String getName();
	public String getDescription();
	public String getSqlQuery();
	public ViewStrategy getSupportedViewStrategy();
	
}