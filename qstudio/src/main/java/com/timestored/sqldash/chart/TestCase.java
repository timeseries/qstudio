package com.timestored.sqldash.chart;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.MoreObjects;

/**
 * Named example of {@link ResultSet} with corresponding KDB query to generate that ResultSet.
 */
class TestCase {

	private final String kdbQuery;
	private final String name;
	private final ResultSet resultSet;

	TestCase(String name, ResultSet resultSet, String kdbQuery) {
		this.kdbQuery = kdbQuery;
		this.resultSet = resultSet;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("kdbQuery", kdbQuery)
			.add("name", name)
			.add("resultSet", resultSet)
			.toString();
	}

	/**
	 * @return an easily memorable unique name.
	 */
	public String getName() {
		return name;
	}
	
	/** get the KDB query which will create the example */
	public String getKdbQuery() {
		return kdbQuery;
	}

	/** get the resultSet which will create the example if possible otherwise return null */
	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public ChartResultSet getColResultSet() throws SQLException {
		return ChartResultSet.getInstance(resultSet);
	}

}
