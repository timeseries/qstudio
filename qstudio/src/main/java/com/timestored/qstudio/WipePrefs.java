/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2023 TimeStored
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
package com.timestored.qstudio;

import java.util.prefs.BackingStoreException;

import javax.swing.JOptionPane;

import com.timestored.connections.ConnectionManager;

/** Wipes qStudio preferences and resets defaults to allow testing newly installed behaviour. */
public class WipePrefs {

	public static void main(String... args) throws BackingStoreException {
		
		String message = "First make sure qStudio is closed!" +
				"\r\n\r\nThen clicking yes below will remove:" +
				"\r\n- all saved connections" +
				"\r\n- stored settings" +
				"\r\n- open file history. \r\nAre you sure you want to continue?";
		int choice = JOptionPane.showConfirmDialog(null, message, 
				"Delete all settings?", JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		
		if(choice == JOptionPane.YES_OPTION) {
			try {
				QStudioFrame.resetDefaults(false);
				ConnectionManager.wipePreferences(Persistance.INSTANCE.getPref(), Persistance.Key.CONNECTIONS.name());
			} catch (BackingStoreException e1) {
				String errMsg = "Problem accessing registry, please report as bug";
				JOptionPane.showMessageDialog(null, errMsg);
			}
		} else if(choice == JOptionPane.NO_OPTION) {
			System.out.println("no");
		}
		
	}
}
