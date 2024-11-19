/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2024 TimeStored
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

package com.timestored.connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Server;

/**
 * handy test helper for starting/stopping an in-memory H2 database. 
 */
class H2DBTestRunner implements DBTestRunner {

	private static final int PORT = 8000;
	private static ConnectionManager csManager;
	private static Server server;
	private static ServerConfig serverConfig;
	
	
	private H2DBTestRunner() {}
	
	public static DBTestRunner getInstance() {
		return new H2DBTestRunner();
	}
	
	public ConnectionManager start() throws SQLException {
	    try {
			Class.forName("org.h2.Driver");
		    Connection memConn = DriverManager.getConnection("jdbc:h2:mem:db1", "", "");
		    Thread.sleep(2000);
			server = Server.createTcpServer(
				     new String[] { "-tcpPort", ""+PORT, "-tcpAllowOthers" }).start();
			csManager = ConnectionManager.newInstance();
			serverConfig = new ServerConfigBuilder(new ServerConfig("localhost", PORT))
				.setName("h2Server")
				.setJdbcType(JdbcTypes.H2)
				.setDatabase("mem:db1").build();
			csManager.addServer(serverConfig);
			return csManager;
		} catch (ClassNotFoundException | InterruptedException e) {
			throw new SQLException(e);
		}
	}
	
	public void stop() {
		server.stop();
	}
	
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
}