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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;


/**
 * {@link Preferences} based data store that uses enums instad of strings for keys.
 * Ensuring key uniqueness and allowing easier searching from an IDE. 
 */
public class KeyedPrefs<Key extends Enum<Key>> {

	private static final Logger LOG = Logger.getLogger(KeyedPrefs.class.getName());
	private final Preferences pref;
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	
	public KeyedPrefs(String nodeName) {
		pref = Preferences.userRoot().node(nodeName);
	}

	/**
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	public void put(Key key, String value) {
		pref.put(key.name(), value);
	}

	/**
	 * Return the value associate with a key or default value otherwise.
	 * @param key key whose associated value is to be returned.
	 * @param def the value to be returned in the event that this preference node has no value associated with key. 
	 */
	public String get(Key key, String def) {
		return pref.get(key.name(), def);
	}

	/**
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	public void putBoolean(Key key, boolean value) {
		pref.putBoolean(key.name(), value);
	}
	
	/**
	 * Return the value associate with a key or default value otherwise.
	 * @param key key whose associated value is to be returned.
	 * @param def the value to be returned in the event that this preference node has no value associated with key. 
	 */
	public boolean getBoolean(Key key, boolean def) {
		return pref.getBoolean(key.name(), def);
	}
	


	/**
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	public void putInt(Key key, int value) {
		pref.putInt(key.name(), value);
	}
	
	/**
	 * Return the value associate with a key or default value otherwise.
	 * @param key key whose associated value is to be returned.
	 * @param def the value to be returned in the event that this preference node has no value associated with key. 
	 */
	public int getInt(Key key, int def) { return pref.getInt(key.name(), def); }

	public void putLong(Key key, long val) { pref.putLong(key.name(), val); }

	public long getLong(Key key, long def) { return pref.getLong(key.name(), def); }
	
	/**
	 * @return underlying preference store. Not recommended for common use as
	 * it gives much rawer access without key checking etc.
	 */
	public Preferences getPref() {
		return pref;
	}
	

	/**
	 * @return A copy of these preferences in an XML format, or null if not possible.
	 */
	public String toXML() {		
		ByteArrayOutputStream baOS = new ByteArrayOutputStream();
		String s = null;
		try {
			pref.exportSubtree(baOS);
			s = baOS.toString();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't convert preferences to xml", e);
		} catch (BackingStoreException e) {
			LOG.log(Level.WARNING, "Couldn't convert preferences to xml", e);
		}
		return s;
	}
	
	
	/**
	 * import settings from XML for this specified user, creating nodes
	 * if necessary.
	 */
	public static void importXML(String xmlSettings) {
		if(xmlSettings != null) {
			InputStream is = new ByteArrayInputStream(xmlSettings.getBytes());
			try {
				Preferences.importPreferences(is);
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't import xml preferences", e);
			} catch (InvalidPreferencesFormatException e) {
				LOG.log(Level.WARNING, "Couldn't import xml preferences", e);
			}
		}
	}
	
	//############# Listener related code #######################
	
	public static interface Listener {
		/** a change occurred you should reread the preferences */
		public void changeEvent();
	}
	
	public void addListener(Listener l) {
		listeners.add(l);
	}
	
	public void removeListener(Listener l) {
		listeners.remove(l);
	}
	
	/** send a change event to all listeners */
	public void notifyListeners() {
		for(Listener l : listeners) {
			l.changeEvent();
		}
	}
}
