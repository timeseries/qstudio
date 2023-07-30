package com.timestored.qstudio.model;

import java.util.List;

import com.timestored.connections.ServerConfig;

/**
 * Allows listenering for SQL queries being sent / received back.
 * 
 * #sendingQuery fires when a query is sent, it is then guaranteed
 * that queryResultReturned will be called.
 */
public interface QueryListener {
	
	/**
	 * An SQL query is about to be sent.
	 * @param query the SQL query about to be sent.
	 */
	public void sendingQuery(ServerConfig sc, String query);

	/**
	 * A query has returned, the result set is either returned or an exception is.
	 * @param queryResult the result of the SQL query
	 */
	public void queryResultReturned(ServerConfig sc, QueryResult queryResult);

	
	/** Watched expression was modified, ie. query was changed. */
	public void watchedExpressionsModified();

	/** The last result of the watched expressions have been refreshed. */
	public void watchedExpressionsRefreshed();
	
	/** 
	 * The server that queries are ran against was changed
	 * @param server The newly selected server or null if none is selected. 
	 */
	public void selectedServerChanged(String server);
	
	/** 
	 * The list of possible servers changed.
	 * @param serverNames list of serverNames, where those at tail of 
	 * 			list are the most recently updated entries.
	 */
	public void serverListingChanged(List<String> serverNames);
}