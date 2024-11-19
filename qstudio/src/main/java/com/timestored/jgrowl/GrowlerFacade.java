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
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Wraps a {@link FadingGrowler} in order to give the full Growler interface. 
 */
class GrowlerFacade extends AbstractGrowler {

	private static final Icon INFO_ICON;
	private static final Icon WARNING_ICON;
	private static final Icon ERROR_ICON;
	private final FadingGrowler gc;

	private static final Logger LOG = Logger.getLogger(GrowlerFacade.class.getName());
	
	static {
		Icon ii = null;
		Icon wi = null;
		Icon ei = null;
		try {
			ii =  (Icon) UIManager.getIcon("OptionPane.informationIcon");
			wi =  (Icon) UIManager.getIcon("OptionPane.warningIcon");
			ei =  (Icon) UIManager.getIcon("OptionPane.errorIcon");
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Could not get builtin icons for growlers", e);
		}
		
		try {
			if(ii==null) {
				ii = StandardTheme.Icon.INFO.get();	
			}
			if(ei==null) {
				ei = StandardTheme.Icon.SEVERE.get();	
			}
			if(wi==null) {
				wi = StandardTheme.Icon.WARNING.get();	
			}
		} catch(Exception e) {
			LOG.log(Level.FINE, "Could not get user createed icons for growlers", e);
		}
		INFO_ICON = ii;
		WARNING_ICON = wi;
		ERROR_ICON = ei;
	}

	
	public GrowlerFacade(FadingGrowler growlerCore) {
		this.gc = growlerCore;
	}
	

	public void show(Level logLevel, String message, String title, boolean sticky, ImageIcon imageIcon) {
		
		Icon ii = null;
		if(logLevel.equals(Level.SEVERE)) {
			ii = ERROR_ICON;
		} else if(logLevel.equals(Level.WARNING)) {
			ii = WARNING_ICON;
		} else if(logLevel.equals(Level.INFO)) {
			ii = INFO_ICON;
		}
		
		gc.show(message, title, ii, sticky, logLevel);
	}


	@Override public void show(Level logLevel, JPanel messagePanel, String title, boolean sticky) {
		gc.show(logLevel, messagePanel, title, sticky);
	}
	
	
}
