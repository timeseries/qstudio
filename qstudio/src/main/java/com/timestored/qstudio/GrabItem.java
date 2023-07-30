package com.timestored.qstudio;

import java.awt.Component;

import com.google.common.base.Preconditions;
import com.timestored.theme.Icon;

/**
 * Represents one panel along with it's title.
 * Useful for allowing grabbing a panel from somewhere for display elsewhere.
 */
class GrabItem {

	private final Component component;
	private final String title;
	private final Icon icon;
	
	public GrabItem(Component component, String title, Icon icon) {
		this.component = Preconditions.checkNotNull(component);
		this.title = Preconditions.checkNotNull(title);
		this.icon = icon;
	}

	public GrabItem(java.awt.Component component, String title) {
		this(component, title, null);
	}
	
	Component getComponent() { return component; }
	String getTitle() { return title; }

	/** @return icon if one is set otherwise null **/
	Icon getIcon() { return icon; }

	
}
