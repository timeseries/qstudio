package com.timestored.kdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import kx.c.Dict;
import kx.c.Flip;
import kx.c.KException;

/**
 * Represents a connection to a single KDB server and allows
 * sending / retrieving objects. 
 */
public interface KdbConnection {

	/**
	 * @return the host and port of this connection.
	 */
	public abstract String getName();
	
	/** 
	 * Close the socket connection. 
	 * All future requests will cause {@link IOException}. 
	 */
	public abstract void close() throws IOException;

	/**
	 * Send a query that will return an unkeyed table.
	 * @throws IOException if the connection has been closed etc.
	 * @throws KException if there is a problem with the query
	 * @throws UnsupportedOperationException If the result is not a Flip.
	 */
	public abstract Flip queryFlip(String query) throws IOException, KException;

	/**
	 * Send a query that will return an dictionary or keyed table.
	 * @throws IOException if the connection has been closed etc.
	 * @throws KException if there is a problem with the query
	 * throws UnsupportedDataTypeException If the result is not a Dict.
	 */
	public abstract Dict queryDict(String query) throws IOException, KException;

	/**
	 * Send a query that will return whatever the result is.
	 * @throws IOException if the connection has been closed etc.
	 * @throws KException if there is a problem with the query.
	 */
	public abstract Object query(String query) throws IOException, KException;


	/**
	 * Send a query asynchronously, no result is returned.
	 * Commonly used for setting values.
	 * @throws IOException if the connection has been closed etc.
	 */
	public abstract void send(String query) throws IOException;

	/**
	 * Send a object asynchronously, no result is returned.
	 * Commonly used for setting values.
	 * @throws IOException if the connection has been closed etc.
	 */
	public abstract void send(Object o) throws IOException;
	
	/**
	 * @return trtue if this connection is actually connected, you can still
	 * try to query even if it isn't but it may throw an exception.
	 */
	public abstract boolean isConnected();
	
	/** Direct call to underlying c.k() to allow ubscriptions **/
	public Object k() throws UnsupportedEncodingException, KException, IOException;
}
