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

import javax.swing.JWindow;

/** Record for one message shown */
class DisplayedItem {
	
	public final Growl message;
	public final JWindow frame;
	/** ratio between 0-1 for life remaining */
	public float lifeLeft;
	
	public DisplayedItem(JWindow frame, Growl message) {
		this.frame = frame;
		this.message = message;
		this.lifeLeft = 1.0f;
	}
}
