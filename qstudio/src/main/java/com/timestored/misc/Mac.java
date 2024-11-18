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
package com.timestored.misc;

import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

import com.timestored.theme.Icon;


public class Mac {
	public static void configureIfMac(ApplicationListener applicationListener, Icon icon) {
    	// set mac properties to customize menu bars icons etc
		if(System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
			// In java 9 the underlying API calls disappeared. So run protectively.https://bugs.openjdk.java.net/browse/JDK-8160437
			try {
				Application macApplication = new DefaultApplication();
				try {
					macApplication.addApplicationListener(applicationListener);
				} catch(RuntimeException e) { }
				macApplication.setApplicationIconImage(icon.getBufferedImage());
			} catch(RuntimeException e) { }
		}
	}
}
