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
package com.timestored.command;

import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * A command, represents a task that a user could do within an AI. 
 * For example select a menu item, open a file, show documentation
 * By wrapping them as a Command we can place them within a command palette. 
 */
public interface Command {

	/** @return Icon for this entity or null if none set. */
	public Icon getIcon();

	/** @return Title of this command */
	public String getTitle();

	/** @return Title of this command or null if none set */
	public String getDetailHtml();
	
	/** @return The shortcut keystrokes that would run this command or null if none set. */
	public KeyStroke getKeyStroke();
	
	/** Run the actual command */
	public void perform();

	/** 
 	 * @return null or Additional details that can be shown in the title, should be less than 60 characters.
 	 **/
	public String getTitleAdditional();
	
	
}
