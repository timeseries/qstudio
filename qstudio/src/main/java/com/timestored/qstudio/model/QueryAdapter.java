package com.timestored.qstudio.model;

import java.util.List;

import com.timestored.connections.ServerConfig;

/**
 * Convenience abstract class to allow easily implementing {@link QueryListener}.
 */
public abstract class QueryAdapter implements QueryListener {

	@Override public void sendingQuery(ServerConfig sc, String query) { }
	
	@Override public void queryResultReturned(ServerConfig sc, QueryResult queryResult) { }
	
	@Override public void watchedExpressionsModified() { }
	@Override public void watchedExpressionsRefreshed() { }
	@Override public void selectedServerChanged(String server) {	}
	@Override public void serverListingChanged(List<String> serverNames) {}
}