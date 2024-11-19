package com.timestored.sqldash;

import java.util.Collection;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;


/**
 * Represents a stand-alone GUI element within a multi-windowed system.
 * It provides it's own editor and querables that represent the
 * server/sql queries to retrieve data to update the panel view.
 */
public interface Widget extends QueryEngineListener {

	/** Listen for changes to the config. */
	public static interface Listener {
		/** Config was changed */
		public void configChanged(Widget widget);
	}

	public String getTitle();
	public void setTitle(String title);

	/** @return unique ID amongst all Widgets created. */
	public int getId();

	public void addListener(Widget.Listener widgetListener);
	public boolean removeListener(Widget.Listener widgetListener);
	
	/** @return the Panel that will display this Widget **/
	public JPanel getPanel();
	
	/** 
	 * Widgets cache their panels to maintain the same one between changes.
	 * Call this method to invalidate the panels, have them destroyed.
	 */
	public void invalidatePanelCache();
	
	/** @return The collection of database queries that this widget contains */
	public Collection<Queryable> getQueryables();

	/** @return actions that can be performed on this particular widget */
	public Collection<Action> getActions();
	
	public Icon getIcon();
	
	public boolean isRenderLargeDataSets();
}
