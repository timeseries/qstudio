/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2023 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
