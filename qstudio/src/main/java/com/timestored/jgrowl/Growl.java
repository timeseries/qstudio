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
package com.timestored.jgrowl;

import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JPanel;

import lombok.Data;

/**
 * A single message, can be in varying states of aliveness but once dead
 * is dead forever.
 */
@Data class Growl {
	
	private final JPanel messagePanel; 
	private final String message; 
	private final String title;
	private final Icon imageIcon;
	private final boolean sticky;
	private final Level logLevel;
	
	public Growl(String message, String title, Icon imageIcon, 
			boolean sticky, Level logLevel) {
		super();
		if(message == null) {
			throw new IllegalArgumentException("message cannot be null");
		}
		this.messagePanel = null;
		this.message = message;
		this.title = title;
		this.imageIcon = imageIcon;
		this.sticky = sticky;
		this.logLevel = logLevel;
	}

	public Growl(JPanel messagePanel, String title, Icon imageIcon, 
			boolean sticky, Level logLevel) {
		super();
		this.messagePanel = messagePanel;
		this.message = null;
		this.title = title;
		this.imageIcon = imageIcon;
		this.sticky = sticky;
		this.logLevel = logLevel;
	}
	
}
