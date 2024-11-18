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

public abstract class AbstractGrowler  implements Growler {


	/** {@inheritDoc} */	@Override
	public void show(String message, String title, ImageIcon imageIcon) {
		show(Level.INFO, message, title, false, imageIcon);
	}
	
	/** {@inheritDoc} */	@Override
	public void show(String message, String title) {
		show(Level.INFO, message, title);
	}
	
	/** {@inheritDoc} */	@Override
	public void show(String message) {
		show(Level.INFO, message);
	}
	
	/** {@inheritDoc} */	@Override
	public void showInfo(String message, String title) {
		show(Level.INFO, message, title);
	}

	/** {@inheritDoc} */	@Override
	public void showWarning(String message, String title) {
		show(Level.WARNING, message, title);
	}

	/** {@inheritDoc} */	@Override
	public void showSevere(String message, String title) {
		show(Level.SEVERE, message, title);
	}

	/** {@inheritDoc} */	@Override
	public void show(Level level, String message) {
		show(level, message, null);
	}

	/** {@inheritDoc} */	@Override
	public void show(Level level, String message, String title) {
		show(level, message, title, false, null);
	}
}
