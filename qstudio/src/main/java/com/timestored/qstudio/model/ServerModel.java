package com.timestored.qstudio.model;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import kx.c.KException;

import com.google.common.base.MoreObjects;
import com.timestored.connections.ConnectionManager;
import com.timestored.connections.ServerConfig;
import com.timestored.kdb.KdbConnection;

/**
 * Represents a single KDB server, allows accessing its {@link ServerObjectTree},
 * {@link ServerReport}, etc. and can be subscribed to, to receive notification
 * of changes / disconnects.
 */
public class ServerModel {

	private static final Logger LOG = Logger.getLogger(ServerModel.class.getName());
	private final  ServerConfig serverConfig;
	
	private ServerObjectTree serverObjectTree;
	private ServerReport serverReport;
	private ServerSlashConfig serverSlashConfig;
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	private final ConnectionManager connectionManager;
	
	public ServerModel(ConnectionManager connectionManager, ServerConfig serverConfig) {
		this.connectionManager = connectionManager;
		this.serverConfig = serverConfig;
	}

	public static interface Listener {
		public void changeOccurred();
	}

	void refresh() {
		
		serverObjectTree = new ServerObjectTree(connectionManager, serverConfig);
		
		KdbConnection kdbConnection = connectionManager.getKdbConnection(serverConfig);
		if(kdbConnection!= null) {
			
			try {
				try {
					serverReport = new ServerReport(kdbConnection);
					serverSlashConfig = new ServerSlashConfig(kdbConnection);
				} catch(Exception e) {
					LOG.log(Level.WARNING, "Error retrieving Server Properties.", e);
				}
			} finally {
				try {
					kdbConnection.close();
				} catch (IOException e) {
					LOG.log(Level.WARNING, "problem closing kdb connection", e);
				}
			}
			for(Listener l : listeners) {
				l.changeOccurred();
			}
		} 
	}
	
	public KdbConnection getConnection() {
		return connectionManager.getKdbConnection(serverConfig);
	}
	

	/**  @return The last tree obtained from the server otherwise null.  */
	public ServerObjectTree getServerObjectTree() {
		return serverObjectTree;
	}

	/**  @return The last report obtained from the server otherwise null.  */
	public ServerReport getServerReport() {
		return serverReport;
	}


	/**  @return The last config obtained from the server otherwise null.  */
	public ServerSlashConfig getSlashConfig() {
		return serverSlashConfig;
	}


	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	/** 
	 * Run a set of benchmark tests on this server and return the results.
	 * @return Results of running benchmark on server.
	 * @throws IOException If trouble running the benchmark on the server.
	 */
	public BenchmarkReport runBenchmark() throws IOException {
		
		KdbConnection kdbConnection = connectionManager.getKdbConnection(serverConfig);
		if(kdbConnection!= null) {
			try {
				return new BenchmarkReport(kdbConnection);
			} catch (KException e) {
				throw new IOException(e);
			} finally {
				try {
					kdbConnection.close();
				} catch (IOException e) {
					LOG.log(Level.WARNING, "problem closing kdb connection", e);
				}
			}
		}
		throw new IOException("Could not get connection");
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	
	public boolean isConnected() {
		return connectionManager.isConnected(serverConfig);
	}
	
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	/** Get the server identifier */
	public String getName() {
		return serverConfig.getName();
	}


	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("serverConfig", serverConfig)
			.toString();
	}

	
	
}
