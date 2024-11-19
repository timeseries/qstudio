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

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Factory class for getting a Growler.
 */
public class GrowlerFactory {

	static Growler getGrowler(JFrame parent, Theme theme) {
		return new GrowlerFacade(new FadingGrowler(parent, theme));
	}

	/**
	 * Get a growler which shows fading message boxes on the right hand side of the parent frame.
	 * THey will initially appear bottom right, move to top right, and then fade out.
	 * @param parent The parent frame that message boxes will be positioned relative to.
	 * @return The growler than you then call.
	 */
	public static Growler getGrowler(JFrame parent) {
		return getGrowler(parent, new StandardTheme());
	}
	

	
	public static JLabel getLabelWithFixedWidth(String msg, int adjustment) {
		return StandardTheme.getLabelWithFixedWidth(msg, adjustment);
	}
}
