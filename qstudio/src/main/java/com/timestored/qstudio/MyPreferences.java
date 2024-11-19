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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.timestored.connections.PreferenceHelper;

/**
 * Central store for user preferences of the application, listeners are only
 * notified of changes when explicitly requested.
 */
enum MyPreferences {
	INSTANCE;

	private static final Logger LOG = Logger.getLogger(MyPreferences.class.getName());
	
	private enum Key {
		HID_NS, MAX_RET, MAX_ROWS_SHOWN,MAX_CONSOLE_LENGTH, CODE_FONT_SIZE, 
		QUERY_WRAPPED, CODE_FONT, QUERY_WRAP_PRE, QUERY_WRAP_POST, CONNECTION_PERSISTENT,
		FRACTION_DIGITS, QUERY_LOGGING, QUERY_LOGGING_FOLDER, CODE_THEME, LOGIN_USERNAME, LOGIN_PASSWORD, 
		CRITICAL_KEYWORDS, CRITICAL_COLOR, SAVE_WITH_WINDOWS_LINE_ENDINGS, IGNORE_FOLDER_REGEX, OPENAI_KEY,
		SEND_TELEMETRY;
	}
	
	private static final Preferences PREF = Preferences.userNodeForPackage(MyPreferences.class);
	
	private static final String DEFAULT_HID_NS = ".Q .q .h .o .j";
	private static final long DEFAULT_MAX_RET = 10*1024*1024; // 10 MB
	private static final boolean DEFAULT_QUERY_WRAPPED = true;
	private static final boolean DEFAULT_SAVE_WITH_WINDOWS_LINE_ENDINGS = isWindows();
	private static final boolean DEFAULT_SEND_TELEMETRY = true;
	private static final int DEFAULT_MAX_ROWS_SHOWN = 10000;
	private static final int DEFAULT_CONSOLE_LENGTH = 16000;
	private static final int KDB_IPC_LIMIT_MB = 2000;
	private static final int DEFAULT_CODE_FONT_SIZE = 13;
	private static final String DEFAULT_CODE_FONT = "JetBrains Mono";
	private static final String DEFAULT_CODE_THEME = "Darcula";
	private static final boolean DEFAULT_CONNECTION_PERSISTENT = true;
	private static final int DEFAULT_FRACTION_DIGITS = 7;
	private static final String DEFAULT_CRITICAL_KEYWORDS = "prod,PROD,preprod,preprd";
	private static final String DEFAULT_IGNORE_FOLDER_REGEX = "^\\..*|^target$";
	
	private static final int DEFAULT_CRITICAL_COLOR = Color.RED.getRGB();
	

	private static final boolean DEFAULT_QUERY_LOGGING = false;
	private static final String DEFAULT_QUERY_LOGGING_FOLDER = System.getProperty("user.home");
	
	
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
		
	public void resetDefaults() throws BackingStoreException {
		for(Key k : Key.values()) {
			PREF.remove(k.name());
		}
	}
	
	/** @return max number rows that user should see, 0 means unlimited */
	public int getMaxRowsShown() {
		return PREF.getInt(Key.MAX_ROWS_SHOWN.toString(), DEFAULT_MAX_ROWS_SHOWN);
	}
	
	/**
	 * Set max number of rows shown in the results panel.
	 * @param maxRowsShown rows to show, 0 means no limit.
	 */
	public void setMaxRowsShown(int maxRowsShown) {
		Preconditions.checkArgument(maxRowsShown >= 0);
		PREF.putInt(Key.MAX_ROWS_SHOWN.toString(), maxRowsShown);
	}
	
	/** @return font size used for code. */
	public int getCodeFontSize() {
		return PREF.getInt(Key.CODE_FONT_SIZE.toString(), DEFAULT_CODE_FONT_SIZE);
	}
	
	/** 
	 * Set font size used for code.
	 * @param fontSize Font size to use, only values between 1-50 are permitted,
	 * 	no error is thrown but bounds are enforced.
	 */
	public void setCodeFontSize(int fontSize) {
		if(fontSize<1) {
			fontSize = 1;
		} else if(fontSize > 50) {
			fontSize = 50;
		}
		PREF.putInt(Key.CODE_FONT_SIZE.toString(), fontSize);
	}

	
	/** @return font used for code. */
	public String getCodeFont() {
		return PREF.get(Key.CODE_FONT.toString(), DEFAULT_CODE_FONT);
	}

	/** @return font theme for code. */
	public String getCodeTheme() {
		return PREF.get(Key.CODE_THEME.toString(), DEFAULT_CODE_THEME);
	}
	
	public void setCodeTheme(String codeTheme) {
		PREF.put(Key.CODE_THEME.toString(), codeTheme);
	}

	/** 
	 * Set font used for code.
	 */
	public void setCodeFont(String font) {
		String[] f = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		if(Arrays.asList(f).contains(font)) {
			PREF.put(Key.CODE_FONT.toString(), font);
		}
	}
	
	/** @return The prefix placed before all kdb queries */
	public String getQueryWrapPre() {
		return PREF.get(Key.QUERY_WRAP_PRE.toString(), "");
	}

	/** 
	 * Set the postFix used after any kdb queries
	 */
	public void setQueryWrapPost(String queryWrapPost) {
		if(queryWrapPost!=null) {
			PREF.put(Key.QUERY_WRAP_POST.toString(), queryWrapPost);
		}
	}
	
	/** @return The postfix placed after all kdb queries */
	public String getQueryWrapPost() {
		return PREF.get(Key.QUERY_WRAP_POST.toString(), "");
	}

	/** 
	 * Set the postFix used before any kdb queries
	 */
	public void setQueryWrapPre(String queryWrapPre) {
		if(queryWrapPre!=null) {
			PREF.put(Key.QUERY_WRAP_PRE.toString(), queryWrapPre);
		}
	}
	
	public void setDefaultLoginUsername(String username) {
		if(username!=null) {
			PREF.put(Key.LOGIN_USERNAME.toString(), PreferenceHelper.encode(username));
		}
	}
	
	/** @return The postfix placed after all kdb queries */
	public String getDefaultLoginUsername() {
		try {
			return PreferenceHelper.decode(PREF.get(Key.LOGIN_USERNAME.toString(), null));
		} catch (IOException e) {
			return "";
		}
	}
	
	public void setDefaultLoginPassword(String password) {
		if(password!=null) {
			PREF.put(Key.LOGIN_PASSWORD.toString(), PreferenceHelper.encode(password));
		}
	}
	
	/** @return The postfix placed after all kdb queries */
	public String getDefaultLoginPassword() {
		try {
			return PreferenceHelper.decode(PREF.get(Key.LOGIN_PASSWORD.toString(), null));
		} catch (IOException e) {
			return "";
		}
	}

	public void setCriticalServerKeywords(String criticalKeywordsCommaSeparated) {
		if(criticalKeywordsCommaSeparated!=null) {
			PREF.put(Key.CRITICAL_KEYWORDS.toString(), criticalKeywordsCommaSeparated);
		}
	}

	/** @return Comma separated list of keywords that if they match a server name cause it to be colored */
	public String getCriticalServerKeywords() {
		return PREF.get(Key.CRITICAL_KEYWORDS.toString(), DEFAULT_CRITICAL_KEYWORDS);
	}

	public void setIgnoreFilterRegex(String ignoreFilterRegex) {
		if(ignoreFilterRegex!=null) {
			PREF.put(Key.IGNORE_FOLDER_REGEX.toString(), ignoreFilterRegex);
		}
	}

	/** @return Comma separated list of keywords that if they match a server name cause it to be colored */
	public String getIgnoreFilterRegex() {
		return PREF.get(Key.IGNORE_FOLDER_REGEX.toString(), DEFAULT_IGNORE_FOLDER_REGEX);
	}

	void setOpenAIkey(String openAIkey) {
		if(openAIkey!=null) {
			PREF.put(Key.OPENAI_KEY.toString(), openAIkey);
		}
	}

	public String getOpenAIkey() {
		return PREF.get(Key.OPENAI_KEY.toString(), "");
	}
	
	public void setCriticalServerColor(Color color) {
		if(color!=null) {
			PREF.putInt(Key.CRITICAL_COLOR.toString(), color.getRGB());
		}
	}

	/** @return Comma separated list of keywords that if they match a server name cause it to be colored */
	public Color getCriticalServerColor() {
		int c =  PREF.getInt(Key.CRITICAL_COLOR.toString(), DEFAULT_CRITICAL_COLOR);
		return new Color(c);
	}

	/** @return true if qStudio should try to maintain a connection for subsequent queries */
	public boolean isConnectionPersistent() {
		return PREF.getBoolean(Key.CONNECTION_PERSISTENT.toString(), DEFAULT_CONNECTION_PERSISTENT);
	}

	/** true if qStudio should try to maintain a connection for subsequent queries */
	public void setConnectionPersistent(boolean connectionPersistent) {
		PREF.putBoolean(Key.CONNECTION_PERSISTENT.toString(), connectionPersistent);
	}
	
	/** 
	 * Set max number of characters that should be shown in the console.
	 * @param maxLength maximu length of console, must be >= 1000.  
	 */
	public void setMaxConsoleLength(int maxLength) {
		Preconditions.checkArgument(maxLength >= 1000);
		PREF.putInt(Key.MAX_CONSOLE_LENGTH.toString(), maxLength);
	}
	
	/** @return max number of characters that should be shown in the console. */
	public int getMaxConsoleLength() {
		return PREF.getInt(Key.MAX_CONSOLE_LENGTH.toString(), DEFAULT_CONSOLE_LENGTH);
	}
	
	
	/**
	 * @return KDB Namespaces which should not be displayed to the user.
	 */
	public Set<String> getHiddenNamespaces() {
		String[] ns = PREF.get(Key.HID_NS.toString(), DEFAULT_HID_NS).split(" ");
		return new HashSet<String>(Arrays.asList(ns));
	}
	
	/**
	 * Set which namespaces will not be shown to the user.
	 * @param hiddenNamespaces namespaces including the dot before their name.
	 */
	public void setHiddenNamespaces(String[] hiddenNamespaces) {
		Preconditions.checkNotNull(hiddenNamespaces);
		PREF.put(Key.HID_NS.toString(), Joiner.on(' ').join(hiddenNamespaces));
	}

	/**
	 * Set maximum size of object that queries will return to the client.
	 * @param maxSize Maximum size in Megabytes, 0 means infinite.
	 */
	public void setMaxReturnSizeMB(int maxSizeMB) {
		long l = maxSizeMB<KDB_IPC_LIMIT_MB ? ((long)maxSizeMB)*1024*1024 : 0;
		setMaxReturnSize(l);
	}
	
	/**
	 * Set maximum size of object that queries will return to the client.
	 * @param maxSize Maximum size in bytes, 0 means infinite.
	 */
	public void setMaxReturnSize(long maxSize) {
		Preconditions.checkArgument(maxSize >= 0);
		PREF.putLong(Key.MAX_RET.toString(), maxSize);
	}
	
	/**  @return Maximum object size in bytes that queries will return to client. */
	public long getMaxReturnSize() {
		return PREF.getLong(Key.MAX_RET.toString(), DEFAULT_MAX_RET);
	}

	
	/**
	 * Set the maximum number of decimal places that should be displayed;
	 * if less than zero, then zero is used.
	 */
	public void setMaximumFractionDigits(int decimalPlaces) {
		if(decimalPlaces < 0) {
			decimalPlaces = 0;
		}
		PREF.putInt(Key.FRACTION_DIGITS.toString(), decimalPlaces);
	}
	
	/**  @return the maximum number of decimal places that should be displayed. */
	public int getMaximumFractionDigits() {
		return PREF.getInt(Key.FRACTION_DIGITS.toString(), DEFAULT_FRACTION_DIGITS);
	}

	/** If true, all user queries will be logged a folder/file. */
	public void setQueryLogging(boolean on) {
		PREF.putBoolean(Key.QUERY_LOGGING.toString(), on);
	}
	
	/**  @return true, all user queries will be logged a folder/file. */
	public boolean isQueryLogging() {
		return PREF.getBoolean(Key.QUERY_LOGGING.toString(), DEFAULT_QUERY_LOGGING);
	}

	/** Set the folder that user queries will be logged to. */
	public void setQueryLoggingFolder(String filepath) {
		PREF.put(Key.QUERY_LOGGING_FOLDER.toString(), filepath);
	}
	
	/**  @return the folder that user queries will be logged to. */
	public String getQueryLoggingFolder() {
		return PREF.get(Key.QUERY_LOGGING_FOLDER.toString(), DEFAULT_QUERY_LOGGING_FOLDER);
	}
	
	/** 
	 * @return true if the query sent to the server is wrapped to 
	 * get nice console output and protect against large sizes.
	 */
	public boolean isQueryWrapped() {
		return PREF.getBoolean(Key.QUERY_WRAPPED.toString(), DEFAULT_QUERY_WRAPPED);
	}


	/** 
	 * @return true if documents should be saved with windows line endings \\r\\n,
	 * 			else linux endings \\n will be used.
	 */
	public boolean isSaveWithWindowsLineEndings() {
		return PREF.getBoolean(Key.SAVE_WITH_WINDOWS_LINE_ENDINGS.toString(), DEFAULT_SAVE_WITH_WINDOWS_LINE_ENDINGS);
	}
	
	public boolean isSendTelemetry() {
		return PREF.getBoolean(Key.SEND_TELEMETRY.toString(), DEFAULT_SEND_TELEMETRY);
	}
	
	public void setSendTelemetry(boolean isSendTelemetry) {
		PREF.putBoolean(Key.SEND_TELEMETRY.toString(), isSendTelemetry);
	}
	
	
	
	/** 
	 * true if the query sent to the server is wrapped to 
	 * get nice console output and protect against large sizes.
	 */
	public void setQueryWrapped(boolean queryWrapped) {
		PREF.putBoolean(Key.QUERY_WRAPPED.toString(), queryWrapped);
	}

	/** 
	 * @param saveWithWindowsLineEndings True if documents should be saved with windows line endings \\r\\n,
	 * 			else linux endings \\n will be used.
	 */
	public void setSaveWithWindowsLineEndings(boolean saveWithWindowsLineEndings) {
		PREF.putBoolean(Key.SAVE_WITH_WINDOWS_LINE_ENDINGS.toString(), saveWithWindowsLineEndings);
	}
	
	/** 
	 * @return Maximum object size in Megabytes that queries will return to client.
	 */
	public int getMaxReturnSizeMB() {
		return (int) (getMaxReturnSize()/(1024*1024));
	}

	/**
	 * @return A copy of these preferences in an XML format, or null if not possible.
	 */
	public String toXML() {		
		ByteArrayOutputStream baOS = new ByteArrayOutputStream();
		String s = null;
		try {
			PREF.exportSubtree(baOS);
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
	
	static interface Listener {
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

	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
	}
}
