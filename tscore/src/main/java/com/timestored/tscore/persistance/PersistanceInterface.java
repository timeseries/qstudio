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
package com.timestored.tscore.persistance;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public interface PersistanceInterface {

	//@TODO ideally this would use KeyedPrefs
	public static final String PATH_SPLIT = ";";
	
	/**
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	void put(KeyInterface key, String value);

	/**
	 * Return the value associate with a key or default value otherwise.
	 * @param key key whose associated value is to be returned.
	 * @param def the value to be returned in the event that this preference node has no value associated with key. 
	 */
	String get(KeyInterface key, String def);

	/**
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	void putBoolean(KeyInterface key, boolean value);

	/**
	 * Return the value associate with a key or default value otherwise.
	 * @param key key whose associated value is to be returned.
	 * @param def the value to be returned in the event that this preference node has no value associated with key. 
	 */
	boolean getBoolean(KeyInterface key, boolean def);

	/**
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	void putInt(KeyInterface key, int value);

	/**
	 * Return the value associate with a key or default value otherwise.
	 * @param key key whose associated value is to be returned.
	 * @param def the value to be returned in the event that this preference node has no value associated with key. 
	 */
	int getInt(KeyInterface key, int def);

	void putLong(KeyInterface key, long val);

	long getLong(KeyInterface key, long def);

	/** 
	 * Removes all persisted values. (except FERD)
	 * @param wipeLicense wipes everything, license and first ever run date.
	 * 	meaning the software would revert to a free trial.
	 */
	void clear(boolean wipeLicense) throws BackingStoreException;

	/**
	 * @return underlying preference store. Not recommended for common use as
	 * it gives much rawer access without key checking etc.
	 */
	Preferences getPref();

}