package com.timestored.cstore;

import java.io.IOException;
import java.util.List;


/**
 * Represents a connection to a column oriented database and the minimum
 * functionality it should provide.
 */
public interface CStoreConnection {

	public abstract void close() throws IOException;

	public abstract CTable queryTable(String s) throws IOException;

	public abstract <T> List<T> queryList(String s, Class<T> c) throws IOException;

	public abstract void send(String s) throws IOException;

	/** @deprecated try not to use as its DB specific
	 */
	@Deprecated
	public abstract Object query(String query) throws IOException;

	public abstract Boolean queryBoolean(String string) throws IOException;

}