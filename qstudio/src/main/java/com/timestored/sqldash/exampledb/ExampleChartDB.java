package com.timestored.sqldash.exampledb;

import java.util.List;

import com.timestored.connections.JdbcTypes;

/**
 * Contains everything needed to create an example database and a number
 * of example queries demonstrating different chart types. 
 */
public interface ExampleChartDB {

	public String getName();
	
	/** A description of the database constructed, the tables/data it contains */
	public String getDescription();
	
	/** The SQL required to construct the database */
	public List<String> getInitSQL(boolean withComments);
	
	public JdbcTypes getDbType();
	
	public List<ExampleChartQuery> getQueries();
}
