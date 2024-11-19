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
package com.timestored.qdoc;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import com.timestored.command.Command;

/**
 * Command for autocompleting a piece of code, shows qdoc's as preview. 
 */
public abstract class DocumentedEntityDocCommand implements Command {

	private final DocumentedEntity de;
	private final boolean showSource;
	private final String commandPrefix;
	
	public DocumentedEntityDocCommand(DocumentedEntity documentedEntity, 
			boolean showSource, String commandPrefix) {
		this.de = documentedEntity;
		this.showSource = showSource;
		this.commandPrefix = commandPrefix;
	}

	@Override public String getTitle() {
		return commandPrefix + de.getDocName(); 
	}

	@Override public String getTitleAdditional() {
		return (showSource && de.getSource().length() > 0) ? de.getSource() : null;
	}

	@Override public String getDetailHtml() { return de.getHtmlDoc(false); }
	@Override public KeyStroke getKeyStroke() { return null; }
	@Override public ImageIcon getIcon() { return de.getIcon(); }

}
