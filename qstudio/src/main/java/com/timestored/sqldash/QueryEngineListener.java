package com.timestored.sqldash;

import java.sql.ResultSet;

/**
 * Listen for changes to the query/view/qtab/config.
 */
public interface QueryEngineListener {
	
	/** 
	 * Called if the qtab and subsequently therefore the view changed
	 * @param qTab the {@link ResultSet} for the query if there is one, otherwise null.
	 */
	public void tabChanged(final Queryable queryable, final ResultSet qTab);

	/**
	 * Called if there was an error while trying to retrieve resultset for that apps query
	 * @param queryable The query that caused a problem
	 * @param e The exception that caused the problem.
	 */
	public void queryError(final Queryable queryable, Exception e);

}