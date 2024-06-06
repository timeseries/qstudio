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

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Provide information dialoges for displaying messages to the user, 
 * interface intentionally similar to {@link JOptionPane#showMessageDialog} as standard
 * growlers are intended as a less intrusive replacement that fade after time.
 */
public interface Growler {

	/** Show a message to the user with a given title/message/icon. */
	public abstract void show(String message, String title, ImageIcon imageIcon);

	/** Show a message to the user with a given title/message. */
	public abstract void show(String message, String title);

	public abstract void show(String message);

	/** Display an informational message.  */
	public abstract void showInfo(String message, String title);

	/** Display an warning message.  */
	public abstract void showWarning(String message, String title);

	/** Display an warning message.  */
	public abstract void showSevere(String message, String title);

	/** Display a message.  */
	public abstract void show(Level level, String message, String title);

	/** Display a message.  */
	public abstract void show(Level level, String message);

	/** Show a message to the user with a given title/message/icon. */
	void show(Level logLevel, String message, String title, ImageIcon imageIcon, boolean sticky);

}