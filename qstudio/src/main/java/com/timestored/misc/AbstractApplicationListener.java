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

import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;


/**
 * Implements the commonly unused methods and leaves important ones unimplmented.
 *
 */
public abstract class AbstractApplicationListener implements ApplicationListener {
	
	private final JFrame frame;

	public AbstractApplicationListener(JFrame frame) {
		this.frame = frame;
	}
	
	@Override public void handleReOpenApplication(ApplicationEvent event) { }
	@Override public void handleOpenApplication(ApplicationEvent event) { }
	
	@Override public void handleQuit(ApplicationEvent event) {
    	  WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
          Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}
	
	@Override public void handlePrintFile(ApplicationEvent event) {
        JOptionPane.showMessageDialog(frame, "Sorry, printing not implemented"); 
    }
	
	@Override public void handlePreferences(ApplicationEvent event) {
        JOptionPane.showMessageDialog(frame, "No settings necessary"); 
	}

	@Override public void handleAbout(ApplicationEvent event) {
		// do nothing
	}
}
