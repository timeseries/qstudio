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
package com.timestored.connections;

import java.awt.Color;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/** Data Transfer Object for persisting {@link ServerConfig}. */
@XStreamAlias("ServerConfig")
public class ServerConfigDTO {

   	@XStreamAsAttribute private final String name;
   	@XStreamAsAttribute private final String username;
   	@XStreamAsAttribute private final String password;
   	@XStreamAsAttribute private final String host;
   	@XStreamAsAttribute private final int port;
   	@XStreamAsAttribute private final String database;
   	@XStreamAsAttribute private final JdbcTypes jdbcType;
   	
   	@XStreamAsAttribute private Integer clr;
    // deprecated, needs to stay in to prevent xml xstream read errors
   	@SuppressWarnings("unused")
	transient private Color color = null;
   	
    // deprecated, for now I read in but do not write out, later mark transient
   	@XStreamAsAttribute private String folder;
   	
   	/** 
   	 * Zero-arg constructor to make xstream work? 
   	 * https://stackoverflow.com/questions/25661763/xstream-exception
   	 **/
   	public ServerConfigDTO() {
   		name = "";
		password = "";
		username = "";
		host = "";
		port = 0;
		database = "";
		jdbcType = JdbcTypes.KDB;
	}
   	
	ServerConfigDTO(ServerConfig sc, boolean removeLogins) {
		this.name = sc.getName();
		if(removeLogins) {
			this.password = "";
			this.username = "";
		} else {
			this.password = sc.getPassword();
			this.username = sc.getUsername();
		}
		this.host = sc.getHost();
		this.port = sc.getPort();
		this.database = sc.getDatabase();
		this.jdbcType = sc.getJdbcType();
//		this.folder = sc.getFolder();
		this.clr = sc.getColor().getRGB();
	}
	
	ServerConfig getInstance() {
		Color c = null;
		if(this.clr != null) {
			c = new Color(clr);
		}
		// this fixes reading in old configs where folder was stored separately
		String n = name;
		if(this.folder != null && this.folder.length()>0) {
			n = folder + "/" + name;
		}
		return new ServerConfig(host, port, username, password, n, jdbcType, c, database);
	}
}
