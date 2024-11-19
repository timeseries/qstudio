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
package com.timestored.plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timestored.connections.JdbcTypes;
import com.timestored.misc.DynamicClassLoader;

import lombok.extern.java.Log;

/**
 * THIS FILE IS ALSO USED BY PULSE. Keep it in sync.
 * THIS FILE IS ALSO USED BY PULSE. Keep it in sync.
 * THIS FILE IS ALSO USED BY PULSE. Keep it in sync.
 */


/**
 * Checks for plugins and loads them then sets properties to load them.
 */
@Log public class PluginLoader {

	private static DatabaseAuthenticationService databaseAuthenticationService;
	
	private PluginLoader() {}

	public static List<File> getDirectories(String appname) {
		File userD = new File(System.getProperty("user.home") + File.separator + appname + File.separator + "libs");
        File curD = new File(System.getProperty("user.dir") + File.separator + "libs");
        List<File> d = new ArrayList<>();
        d.add(userD);
        d.add(curD);
        return d; 
	}

	private static Map<List<URL>,URLClassLoader> urlsToClassLoaders = new HashMap<>();
	private static URLClassLoader latestLoader; 

	private synchronized static URLClassLoader getMyLoaderFromHardRefresh(String appname) {
		List<URL> l = new ArrayList<>();
		getJars(getDirectories(appname)).forEach(f -> {
			try {
				l.add(f.toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});
		Collections.sort(l, (a,b) -> a.toString().compareTo(b.toString()));
		URLClassLoader r = urlsToClassLoaders.get(l);
		if(r != null) { // If jars have not changed, no need to reload
			return r;
		}
		URLClassLoader cl = new URLClassLoader(l.toArray(new URL[] {}), PluginLoader.class.getClassLoader());
		latestLoader = cl;
		urlsToClassLoaders.put(l, cl);
		return cl;
	}
    
	private static List<File> getJars(List<File> directories) {
		List<File> r = new ArrayList<>();
		for(File d : directories) {
			if(d.exists() && d.isDirectory()) {
				for(File f : d.listFiles()) {
					if(f.exists() && f.getName().endsWith(".jar")) {
						r.add(f);
					}
				}
			}
		}
		return r;
	}
	
	/**
	 * Attempts to find the first {@link DatabaseAuthenticationService} in the class path and use it as authenticator.
	 * Unless otherwise specified the JDBC driver used is kdb.
	 * @return true if plugins were found otherwise false.
	 */
	public static boolean loadPlugins(String appname) {
		List<DatabaseAuthenticationService> instances = new ArrayList<>();
		for(File dir : getDirectories(appname)) {
			if(dir.exists() && dir.isDirectory()) {
		        instances.addAll(DynamicClassLoader.loadInstances(dir, DatabaseAuthenticationService.class, true));
		        List<Driver> jdbcIntances = DynamicClassLoader.loadInstances(dir, java.sql.Driver.class, true);
			}
		}
        
		if(!instances.isEmpty()) {
			databaseAuthenticationService = instances.get(0);
		}

		// If we are using an authentication plugin, make assumption that it's kdb
		if(databaseAuthenticationService != null) {
			writePropIfNotExist("jdbc.isKDB", "true");
			writePropIfNotExist("jdbc.dbRequired", "false");
			writePropIfNotExist("jdbc.driver", "kx.jdbc");
			writePropIfNotExist("jdbc.urlFormat", "jdbc:q:@HOST@:@PORT@");
			writePropIfNotExist("jdbc.niceName", "Kdb with " + databaseAuthenticationService.getName());
			writePropIfNotExist("jdbc.authenticator", databaseAuthenticationService.getClass().getCanonicalName());
		}
		
		return databaseAuthenticationService != null;
	}

	private static void writePropIfNotExist(String key, String value) {
		if(System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}

	/**
	 * Using all the plugin directories associated with this appname, Try to load a className from those various jars.
	 */
	public static Class<?> getCClass(String appname, String className) throws ClassNotFoundException {
		// iterating / looping files and jars is costly on each connection / driver load.
		// Try to avoid that if class can be cheaply loaded. This makes testing difficult as we 
		// will need to 1. Download driver A    2.Query A    3. Download Driver B     4. Query B   - TO test all code loops
		try {
			return Class.forName(className);
		} catch(ClassNotFoundException e1) {
			try {
				if(latestLoader != null) {
					return latestLoader.loadClass(className);
				}
			} catch(ClassNotFoundException e2) {
			}
		}
		log.info("Could not find class already loaded. Refreshing from folders / jars.");
		return getMyLoaderFromHardRefresh(appname).loadClass(className);
	}

	public static File installDriver(String appname, JdbcTypes jdbcTypes) throws IOException {
		try {
			Class<?> c = getCClass(appname, jdbcTypes.getDriver());
			if(c != null) {
				log.info("installDriver skipped as class already loaded.");
				return null;
			}
		} catch(ClassNotFoundException c) {}
		Exception e = null;
		for(File installDir : PluginLoader.getDirectories(appname.toLowerCase())) {
			for(String u : jdbcTypes.getDownloadURLs()) {
				try {
					String filename = u.substring(u.lastIndexOf('/')+1);
					File f = new File(installDir, filename);
					Curler.downloadFileTo(u, f); 
					log.info("File downloaded: " + f.getAbsolutePath());
					PluginLoader.getCClass(appname, jdbcTypes.getDriver());
					return f;
				} catch(Exception e1) {
					e = e1;
				}
			}
		}
		throw new IOException(e);
	}
}
