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

import com.timestored.AppDescription;
import com.timestored.TimeStored;
import com.timestored.TimeStored.Page;
import com.timestored.sqldash.SqlDashFrame;
import com.timestored.theme.Icon;
import com.timestored.theme.Theme;


public class QStudioAppDescription implements AppDescription {

	public static final AppDescription INSTANCE = new QStudioAppDescription();
	public static final String APP_TITLE = "qStudio";
	public static final String VERSION = SqlDashFrame.VERSION;
	public static final String ERROR_URL = TimeStored.getContactUrl("qStudio Error Report");

	private QStudioAppDescription() {};
	public static AppDescription getInstance() { return INSTANCE; }
	
	@Override public String getAppTitle() { return APP_TITLE; }
	@Override public String getVersion() { return VERSION; }
	@Override public String getAppURL() { return Page.QSTUDIO.url(); }
	@Override public String getHelpURL() { return Page.QSTUDIO_HELP.url(); }
	@Override public String getErrorURL() { return ERROR_URL; }
	
	@Override public Icon getIcon() { return Theme.CIcon.SQLDASH_LOGO; }
	@Override public String getHtmlTitle() { return "<h1><font color='#2580A2'>q</font><font color='#25A230'>Studio</font></h1>"; }
	@Override public String getTechEmail() { return TimeStored.TECH_EMAIL_ADDRESS; };
}
