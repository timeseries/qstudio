package com.timestored.plugins;

/** Details relevant to a single database server connection. */
public class ConnectionDetails {
	
	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;

	public ConnectionDetails(String host, int port, String database, String username, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	/** @return username for logging into database if one is set else empty string  **/
	public String getUsername() { return username == null ? "" : username; }

	/** @return password for logging into database if one is set else empty string  **/
	public String getPassword() { return password == null ? "" : password; }

	/** @return hostname that the database server is running on **/
	public String getHost() { return host; }

	/** @return tcp port that the database server is running on **/
	public int getPort() { return port; }

	/** @return database that will be connected to if one is set else empty string  **/
	public String getDatabase() { return database == null ? "" : database; }
}
