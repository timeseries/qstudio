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
