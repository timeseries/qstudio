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

import java.sql.SQLException;

/**
 * Allows running a test database and getting its connection objects. 
 */
public interface DBTestRunner {

	/** Start the database and return a connection manager with the server in it **/
	public ConnectionManager start() throws SQLException;
	
	/** stop the database and remove all data **/
	public void stop();
	
	/** get the details of the test server **/
	public ServerConfig getServerConfig();
}