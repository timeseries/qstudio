package com.timestored.sqldash.exampledb;

import com.google.common.base.Preconditions;
import com.timestored.sqldash.chart.ViewStrategy;

class MyExampleChartQuery implements ExampleChartQuery {

	private final ViewStrategy vs;
	private final String sqlQuery;
	private final String name;
	private final String description;
	
	public MyExampleChartQuery(String name, ViewStrategy vs, String sqlQuery, String description) {
		this.vs = Preconditions.checkNotNull(vs);
		this.sqlQuery = Preconditions.checkNotNull(sqlQuery);
		this.name = Preconditions.checkNotNull(name);
		this.description = description == null ? "" : description;
	}

	public MyExampleChartQuery(String name, ViewStrategy vs, String sqlQuery) {
		this(name, vs, sqlQuery, null);
	}

	@Override public ViewStrategy getSupportedViewStrategy() { return vs; }
	@Override public String getSqlQuery() { return sqlQuery; }
	@Override public String getName() { return name; }
	@Override public String getDescription() { return description; }
}