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
package com.timestored.connections;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/** Provides locale specific i18n test. Allows translated GUIs. */
class Msg {

	private static final String BUNDLE_NAME = "com.timestored.connections.messages";
	private static ResourceBundle rb;

	static { setLocale(Locale.getDefault()); }
	
	public static enum Key {
		HOST, DATABASE,  USERNAME, PASSWORD, ADD, SAVE, DELETE, CANCEL, PORT;
		
		private static final Map<String, Key> lookup = Maps.newHashMap();
		
		 static {
			for(Key k : Key.values()) { lookup.put(k.toString(), k); }
	     }
		 
		 public static Key get(String k) { return lookup.get(k); }
	}
	
	public static String get(Key key) { return rb.getString(key.toString()); }
	
    public static void setLocale(Locale locale){
        rb = ResourceBundle.getBundle(BUNDLE_NAME,locale);
    }
    
    /**
     * @return True if all possible keys are specified in the current resource bundle
     */
    static boolean checkAllKeysSpecified() {
    	for(Key k : Key.values()) {
    		if(get(k)==null || get(k).trim().length()<1) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * @return Any keys only defined in the resource bundle but not the enumeration
     */
    static Set<String> getSuperfluosResourceBundleEntries() {
    	
    	Set<String> extraKeys = Sets.newHashSet();
    	Enumeration<String> keys = rb.getKeys();
    	while(keys.hasMoreElements()) {
    		String k = keys.nextElement();
    		if(Key.get(k)==null) {
    			extraKeys.add(k);
    		}
    	}
    	return extraKeys;
    }
}
