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
import javax.swing.JWindow;

/**
 * Controls appearance of Growl messages and how quickly they fade etc.
 */
public interface Theme {

	/** milliseconds between timer firings */
	public abstract long getFadeTimerDelay();

	/** vertical pixels between items */
	public abstract int getSpaceBetweenItems();

	/** the pixels moved per getFadeTimerDelay */
	public abstract int getMoveSpeed();

	/** alpha reduction per timer firing */
	public abstract float getFadeRate();

	/** pixel range from top, where messages start to fade */
	public abstract int getFadeRangeMinimum();

	/** @return window appropriate for a given message */
	public abstract JWindow getWindow(final Growl message, JFrame parent);
	
	/** @return x cooridinate for left hand edge of message windows */
	public abstract int getLeftRuler(JFrame parentFrame);

	/** space between top ofparentFrame and top of first message */
	public abstract int getTopSpacer();

}