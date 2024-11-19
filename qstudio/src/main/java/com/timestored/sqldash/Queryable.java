package com.timestored.sqldash;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;

import lombok.ToString;

/**
 * Represents a single sql connection/query. 
 */
@ToString
public class Queryable {

	/** Listen for changes to the config. */
	public static interface Listener {
		/** Config was changed */
		public void configChanged(Queryable queryable);
	}

	private static final Logger LOG = Logger.getLogger(Queryable.class.getName());
	
   	transient private final List<Queryable.Listener> listeners = new CopyOnWriteArrayList<Queryable.Listener>();
   	
   	private String serverName = "localhost:5000";
    private String query = "";
    private int refreshPeriod;
	
	public Queryable() {}

	public Queryable(String serverName, String query, int refreshPeriod) {
		this.serverName = serverName;
		this.query = query;
		this.refreshPeriod = refreshPeriod;
	}
	
	public Queryable(String serverName, String query) {
		this(serverName, query, 0);
	}
	
	Queryable(Queryable app) {
		this.serverName = app.getServerName();
		this.query = app.getQuery();
		this.refreshPeriod = app.getRefreshPeriod();
	}

	public void setQuery(String query) {
		this.query = Preconditions.checkNotNull(query);    cc();
	}

	/**
	 * Set the servername uniquely identifying which server this app gets data from.
	 * @param serverName server that will be queried if it exists.
	 */
	public void setServerName(String serverName) {
		this.serverName = Preconditions.checkNotNull(serverName);    cc();
	}

	/**
	 * Requery the database every milliseconds, must be &gt;50,
	 * 0 means re-query as fast as possible. -1 Means don't re-query except on interaction.
	 * @param milliseconds How often the database will be queried.
	 */
	public void setRefreshPeriod(int milliseconds) {
		if(milliseconds < -1) {
			throw new IllegalArgumentException("refresh period must be >=-1");
		}
		this.refreshPeriod = milliseconds;
		cc();
	}
	
	public String getServerName() { return serverName; }
	public String getQuery() { return query; }

	/**
	 * @return milliseconds between which DB will be queried, 
	 * 0 means re-query as fast as possible. -1 Means don't re-query except on interaction. 
	 */
	public int getRefreshPeriod() { return refreshPeriod; }
	
	/** cc = configChanged so notify listeners **/
	private void cc() {
		LOG.info("Queryable configChanged");
		for(Queryable.Listener l : listeners) {
			l.configChanged(this);
		}
	}
	
	public boolean addListener(Queryable.Listener queryableListener) {
		return listeners.add(queryableListener);
	}
	public boolean removeListener(Queryable.Listener queryableListener) {
		return listeners.remove(queryableListener);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + refreshPeriod;
		result = prime * result + ((serverName == null) ? 0 : serverName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Queryable other = (Queryable) obj;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		if (refreshPeriod != other.refreshPeriod)
			return false;
		if (serverName == null) {
			if (other.serverName != null)
				return false;
		} else if (!serverName.equals(other.serverName))
			return false;
		return true;
	}
	

}
