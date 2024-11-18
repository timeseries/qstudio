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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.timestored.TimeStored;
import com.timestored.command.Command;
import com.timestored.command.CommandProvider;
import com.timestored.misc.HtmlUtils;
import com.timestored.qdoc.BuiltinDocumentedEntities;
import com.timestored.qdoc.DocumentationDialog;
import com.timestored.qdoc.DocumentedEntity;
import com.timestored.qdoc.DocumentedEntityDocCommand;
import com.timestored.qdoc.DocumentedMatcher;
import com.timestored.theme.Theme;

/**
 * Takes a documentation provider and wraps it to provide commands 
 * that allow looking at the javadoc. For builtin kdb commands
 * this takes you to the website, otherwise it shows the qDoc.
 */
class DisplayQDocCommandProvider implements CommandProvider {

	private final KDBResultPanel kdbResultPanel;
	private final DocumentedMatcher documentedMatcher;

	DisplayQDocCommandProvider(DocumentedMatcher documentedMatcher, 
			KDBResultPanel kdbResultPanel) {

		this.documentedMatcher = documentedMatcher;
		this.kdbResultPanel = kdbResultPanel;
	}
	
	@Override public Collection<Command> getCommands() {

		List<? extends DocumentedEntity> docs = documentedMatcher.getDocs();
		List<Command> r = new ArrayList<Command>();
		
		// option to display documentation
		for (final DocumentedEntity de : docs) {
			if(!de.getHtmlDoc(false).trim().isEmpty()) {
				DocumentedEntityDocCommand docCommand;
				docCommand = new DocumentedEntityDocCommand(de, true, "Display qDoc: ") {
					@Override public void perform() {
						if(de instanceof BuiltinDocumentedEntities && HtmlUtils.isBrowseSupported()) {
							String url = ((BuiltinDocumentedEntities)de).getLink();
							HtmlUtils.browse(TimeStored.getRedirectPage(url, "qdoc"));
						} else {
							JPanel p = DocumentationDialog.getDocPanel(de);
							kdbResultPanel.clearAndSetContent(p);
						}
					}
					
					@Override public ImageIcon getIcon() {
						return Theme.CIcon.TEXT_HTML.get16();
					}
				};
				r.add(docCommand);
			}
		}
		return r;
	}

}
