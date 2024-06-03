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
package com.timestored.command;

import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * Wraps an {@link Action} to convert it into a command. 
 */
class ActionCommand implements Command {
	private final Action a;
	
	public ActionCommand(Action a) { this.a = a; }
	@Override public javax.swing.Icon getIcon() { return (javax.swing.Icon) a.getValue(Action.SMALL_ICON); }
	@Override public String getTitle() { return (String) a.getValue(Action.NAME); }
	@Override public String getDetailHtml() { return (String) a.getValue(Action.SHORT_DESCRIPTION); }
	@Override public KeyStroke getKeyStroke() { return (KeyStroke) a.getValue(Action.ACCELERATOR_KEY); }
	@Override public void perform() { a.actionPerformed(null); }
	@Override public String toString() { return getTitle(); };
	@Override public String getTitleAdditional() { return ""; }
}