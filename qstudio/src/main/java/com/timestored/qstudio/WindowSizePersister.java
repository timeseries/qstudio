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

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;

import com.timestored.tscore.persistance.PersistanceInterface;

/**
 * Monitor the size of a window and save its size to a certain persistance key.
 */
class WindowSizePersister {

	private final PersistanceInterface persistance;
	private final Persistance.Key key;
	
	WindowSizePersister(final JDialog dialog, 
			final PersistanceInterface persistance, 
			final Persistance.Key key) {

		this.persistance = persistance;
		this.key = key;
		
		dialog.addComponentListener(new ComponentAdapter() {
			@Override public void componentResized(ComponentEvent e) {
				persistance.put(key, convertToString(dialog.getSize()));
			}
		});
	}

	private String convertToString(Dimension size) {
		return size.width + "," + size.height;
	}

	private Dimension convertToDimension(String val) {
		String[] r = val.split(",");
		if(r.length!=2) {
			throw new IllegalArgumentException("can't read dimensions");
		}
		return new Dimension(Integer.parseInt(r[0]), Integer.parseInt(r[1]));
	}
	
	public Dimension getDimension(Dimension defaultDim) {
		String d = persistance.get(key, "");
		if(!d.equals("")) {
			try {
				return convertToDimension(d);
			} catch (Exception e) {
				// ignore and fall through
			}
		}
		return defaultDim; 
	}
}
