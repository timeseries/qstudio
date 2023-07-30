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
