package com.timestored.qstudio.model;

import java.sql.ResultSet;

import kx.c;
import kx.c.KException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Immutable data container to hold result of user ending a KDB query.
 * Four possible results:<ul><li>
 *  query threw an exception (java, cancelled or  kdb {@link c.KException}</li><li>
 *  query was successful and returned a result</li><li>
 *  query was successful but result was too large and query wrap returned only console view.</li></ul>
 *  
 *  A cancel query may or may not contain an actual result.
 * 
 */
public class QueryResult {

	public final String query;
	public final Object k;
	public final ResultSet rs;
	public final Exception e;
	private final String consoleView;
	private final Type type;
	
	enum Type { Exception, Cancel, Success };

	
	public static QueryResult exceptionResult(String query, Exception e) {
		String cView = (e!=null ? e.getMessage() : "exception");
		return new QueryResult(query, null, null, cView, e, false);
	}

	public static QueryResult cancelledResult(String query) {
		String cView = "Query Cancelled";
		return new QueryResult(query, null, null, cView, null, true);
	}

	public static QueryResult successfulResult(String query, Object k, 
			ResultSet rs, String consoleView) {
		return new QueryResult(query, k, rs, consoleView, null, false);
	}
	
	public boolean isCancelled() { return type.equals(Type.Cancel);	}
	public boolean isException() { return type.equals(Type.Exception);	}
	
	
	private QueryResult(String query, Object k, ResultSet rs, 
			String consoleView, Exception e, boolean cancelled) {
		
		if(cancelled) {
			type = Type.Cancel;
		} else if(e != null) {
			type = Type.Exception;
		} else {
			type = Type.Success;
		}
		
		this.query = Preconditions.checkNotNull(query);
		this.k = k;
		this.rs = rs;
		this.e = e;
		this.consoleView = (e instanceof KException) ? "'"+e.getMessage() : consoleView;
	}
	
	Type getType() { return type; }
	
	/** Short text description describing if success, fail, exception **/
	public String getResultType() {
		return type.toString();
	}
	
	/**
	 * The query result may not contain the actual k result if it was over a certain size,
	 * in this case it will just have the console view.
	 * @return true if no k object was returned as too large, instead consoleView is available.
	 */
	public boolean isKResultTooLarge() {
		return k==null && consoleView!=null && consoleView.length()>0;
	}
	
	/**
	 * @return Result as displayed in q console (never null)
	 */
	public String getConsoleView() {
		if(isCancelled()) {
			return "'stop - query cancelled";
		} else if(k==null && consoleView==null || consoleView.isEmpty()) {
			return "::";
		}
		return consoleView;
	}

	public String getQuery() { return query; }
	
	@Override public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("query", query)
				.add("wasResult", k!=null)
				.add("wasException", e!=null)
			.toString();
	}
	
}