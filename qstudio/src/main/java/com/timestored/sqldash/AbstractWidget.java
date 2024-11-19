package com.timestored.sqldash;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.Action;

/**
 * A reusable graphical element that represents one panel 
 * IT has a title, a unique id and allows listeners to hear changes.
 */
abstract class AbstractWidget implements Widget {

	private static final Logger LOG = Logger.getLogger(AbstractWidget.class.getName());
	private static int count = 0;

   	transient private final List<Widget.Listener> listeners = new CopyOnWriteArrayList<Widget.Listener>();
   	protected String title = "untitled";
	private final int id;

	public AbstractWidget(int id) {
		synchronized (this) {
			this.id = id;
			count = id+1;
		}
	}
	
	public AbstractWidget(String title) {
		synchronized (this) {
			id = count++;
		}
		this.title = title;
	}

	public AbstractWidget() {
		this("");
	}

	public AbstractWidget(AbstractWidget app) {
		this(app.getTitle());
	}

	public String getTitle() { return title; }
	
	public void setTitle(String title) {
		this.title = title;
		configChanged();
	}

	protected void configChanged() {
		LOG.info("Widget " + this.getId() + " configChanged");
		for(Widget.Listener l : listeners) {
			l.configChanged(this);
		}
	}
	
	public void addListener(Widget.Listener widgetListener) {
		listeners.add(widgetListener);
	}
	
	public boolean removeListener(Widget.Listener widgetListener) {
		return listeners.remove(widgetListener);
	}

	/** @return unique ID amongst all Widgets created. */
	public int getId() {
		return id;
	}

	/** For all queries that are part of this widget, set the servername  */
	public AbstractWidget setServerName(String serverName) {
		for(Queryable q : getQueryables()) {
			q.setServerName(serverName);
		}
		return this;
	}

	@Override public String toString() {
		return "Widget [title=" + title + ", id=" + id + "]";
	}
	
	@Override public Collection<Action> getActions() {
		return Collections.emptyList();
	}
}
